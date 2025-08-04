package com.sparky.inventory.data.remote

import com.sparky.inventory.config.ApiKeyManager
import com.sparky.inventory.domain.model.InventoryItem
import com.sparky.inventory.util.AppResult
import com.sparky.inventory.util.AppException
import com.sparky.inventory.util.ErrorType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class SheetsResponse(
    val values: List<List<String>>? = null
)

@Serializable
data class SheetsUpdateRequest(
    val values: List<List<String>>
)

class GoogleSheetsApi(
    private val apiKeyManager: ApiKeyManager
) {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        install(Logging) {
            level = LogLevel.INFO
            // Never log request/response bodies that might contain sensitive data
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000 // 30 seconds
            connectTimeoutMillis = 10_000 // 10 seconds
            socketTimeoutMillis = 30_000 // 30 seconds
        }
        
        // Add security headers
        defaultRequest {
            header(HttpHeaders.UserAgent, "SparkyInventory/1.0")
        }
    }
    
    init {
        // Validate configuration on initialization
        try {
            apiKeyManager.validateConfiguration()
        } catch (e: Exception) {
            throw AppException(
                "Failed to initialize GoogleSheetsApi: ${e.message}",
                ErrorType.CONFIGURATION,
                e
            )
        }
    }
    
    suspend fun getInventoryData(): AppResult<List<InventoryItem>> {
        return try {
            val inventorySheetId = apiKeyManager.getInventorySheetId()
            val apiKey = apiKeyManager.getGoogleSheetsApiKey()
            
            val response: SheetsResponse = client.get("https://sheets.googleapis.com/v4/spreadsheets/$inventorySheetId/values/Sheet1") {
                parameter("key", apiKey)
            }.body()
            
            val rows = response.values ?: return AppResult.success(emptyList())
            if (rows.isEmpty()) return AppResult.success(emptyList())
            
            val headers = rows[0].map { it.lowercase() }
            val items = rows.drop(1).mapNotNull { row ->
                parseInventoryRow(row, headers)
            }
            
            AppResult.success(items)
        } catch (e: ClientRequestException) {
            handleHttpError(e)
        } catch (e: Exception) {
            AppResult.error(
                AppException(
                    "Failed to fetch inventory data: ${e.message}",
                    ErrorType.NETWORK,
                    e
                )
            )
        }
    }
    
    private fun parseInventoryRow(row: List<String>, headers: List<String>): InventoryItem? {
        return if (row.isNotEmpty()) {
            try {
                val itemMap = mutableMapOf<String, String>()
                headers.forEachIndexed { index, header ->
                    itemMap[header] = row.getOrNull(index)?.trim() ?: ""
                }
                
                InventoryItem(
                    id = itemMap["id"] ?: "",
                    name = itemMap["name"] ?: "",
                    description = itemMap["description"] ?: "",
                    supplier = itemMap["supplier"] ?: "",
                    sku = itemMap["sku"] ?: "",
                    quantity = itemMap["quantity"]?.toIntOrNull() ?: 0,
                    imageUrl = itemMap["imageurl"] ?: "",
                    keywords = itemMap["keywords"] ?: ""
                )
            } catch (e: Exception) {
                // Log parsing error but don't fail entire operation
                println("Warning: Failed to parse inventory row: ${e.message}")
                null
            }
        } else null
    }
    
    private fun <T> handleHttpError(e: ClientRequestException): AppResult<T> {
        return when (e.response.status.value) {
            401 -> AppResult.error(
                AppException(
                    "Invalid API key. Please check your Google Sheets API configuration.",
                    ErrorType.AUTHENTICATION,
                    e
                )
            )
            403 -> AppResult.error(
                AppException(
                    "Access forbidden. Please check API permissions for Google Sheets.",
                    ErrorType.AUTHORIZATION,
                    e
                )
            )
            404 -> AppResult.error(
                AppException(
                    "Sheet not found. Please verify the sheet ID is correct.",
                    ErrorType.API_ERROR,
                    e
                )
            )
            429 -> AppResult.error(
                AppException(
                    "Rate limit exceeded. Please try again later.",
                    ErrorType.API_ERROR,
                    e
                )
            )
            else -> AppResult.error(
                AppException(
                    "API request failed: ${e.response.status}",
                    ErrorType.API_ERROR,
                    e
                )
            )
        }
    }
    
    suspend fun updateQuantity(sku: String, newQuantity: Int): AppResult<Unit> {
        return try {
            if (sku.isBlank()) {
                return AppResult.error("SKU cannot be blank", ErrorType.VALIDATION)
            }
            
            if (newQuantity < 0) {
                return AppResult.error("Quantity cannot be negative", ErrorType.VALIDATION)
            }
            
            // First get all data to find the row
            val itemsResult = getInventoryData()
            if (itemsResult.isError()) {
                return AppResult.error(
                    AppException(
                        "Failed to fetch inventory for update: ${itemsResult.getErrorOrNull()?.message}",
                        ErrorType.API_ERROR,
                        itemsResult.getErrorOrNull()
                    )
                )
            }
            
            val items = itemsResult.getDataOrNull() ?: emptyList()
            val itemIndex = items.indexOfFirst { it.sku == sku }
            
            if (itemIndex == -1) {
                return AppResult.error("Item with SKU '$sku' not found", ErrorType.VALIDATION)
            }
            
            val inventorySheetId = apiKeyManager.getInventorySheetId()
            val apiKey = apiKeyManager.getGoogleSheetsApiKey()
            
            // Update quantity (assuming quantity is in column E, index 4)
            val range = "Sheet1!E${itemIndex + 2}" // +2 because of header row and 1-based indexing
            
            client.put("https://sheets.googleapis.com/v4/spreadsheets/$inventorySheetId/values/$range") {
                parameter("key", apiKey)
                parameter("valueInputOption", "RAW")
                contentType(ContentType.Application.Json)
                setBody(SheetsUpdateRequest(listOf(listOf(newQuantity.toString()))))
            }
            
            AppResult.success(Unit)
        } catch (e: ClientRequestException) {
            handleHttpError(e)
        } catch (e: Exception) {
            AppResult.error(
                AppException(
                    "Failed to update quantity: ${e.message}",
                    ErrorType.API_ERROR,
                    e
                )
            )
        }
    }
    
    suspend fun addNewItem(item: InventoryItem): AppResult<Unit> {
        return try {
            validateInventoryItem(item)
            
            val inventorySheetId = apiKeyManager.getInventorySheetId()
            val apiKey = apiKeyManager.getGoogleSheetsApiKey()
            val range = "Sheet1!A:H"
            
            client.post("https://sheets.googleapis.com/v4/spreadsheets/$inventorySheetId/values/$range:append") {
                parameter("key", apiKey)
                parameter("valueInputOption", "RAW")
                contentType(ContentType.Application.Json)
                setBody(SheetsUpdateRequest(listOf(listOf(
                    item.id,
                    item.name,
                    item.description,
                    item.supplier,
                    item.sku,
                    item.quantity.toString(),
                    item.imageUrl,
                    item.keywords
                ))))
            }
            
            AppResult.success(Unit)
        } catch (e: ClientRequestException) {
            handleHttpError(e)
        } catch (e: Exception) {
            AppResult.error(
                AppException(
                    "Failed to add new item: ${e.message}",
                    ErrorType.API_ERROR,
                    e
                )
            )
        }
    }
    
    private fun validateInventoryItem(item: InventoryItem) {
        if (item.name.isBlank()) {
            throw AppException("Item name cannot be blank", ErrorType.VALIDATION)
        }
        
        if (item.sku.isBlank()) {
            throw AppException("Item SKU cannot be blank", ErrorType.VALIDATION)
        }
        
        if (item.quantity < 0) {
            throw AppException("Item quantity cannot be negative", ErrorType.VALIDATION)
        }
    }
    
    suspend fun checkApprovedEmail(email: String): AppResult<Boolean> {
        return try {
            if (email.isBlank()) {
                return AppResult.error("Email cannot be blank", ErrorType.VALIDATION)
            }
            
            if (!isValidEmail(email)) {
                return AppResult.error("Invalid email format", ErrorType.VALIDATION)
            }
            
            val approvedEmailsSheetId = apiKeyManager.getApprovedEmailsSheetId()
            val apiKey = apiKeyManager.getGoogleSheetsApiKey()
            
            val response: SheetsResponse = client.get("https://sheets.googleapis.com/v4/spreadsheets/$approvedEmailsSheetId/values/Sheet1") {
                parameter("key", apiKey)
            }.body()
            
            val emails = response.values?.flatten() ?: emptyList()
            val isApproved = emails.any { it.trim().equals(email.trim(), ignoreCase = true) }
            
            AppResult.success(isApproved)
        } catch (e: ClientRequestException) {
            handleHttpError(e)
        } catch (e: Exception) {
            AppResult.error(
                AppException(
                    "Failed to check approved email: ${e.message}",
                    ErrorType.API_ERROR,
                    e
                )
            )
        }
    }
    
    suspend fun searchItems(query: String): AppResult<List<InventoryItem>> {
        return try {
            if (query.isBlank()) {
                return AppResult.success(emptyList())
            }
            
            val itemsResult = getInventoryData()
            if (itemsResult.isError()) {
                return AppResult.error(
                    AppException(
                        "Failed to fetch inventory for search: ${itemsResult.getErrorOrNull()?.message}",
                        ErrorType.API_ERROR,
                        itemsResult.getErrorOrNull()
                    )
                )
            }
            
            val items = itemsResult.getDataOrNull() ?: emptyList()
            val lowerQuery = query.lowercase().trim()
            
            val filteredItems = items.filter { item ->
                searchInItem(item, lowerQuery)
            }
            
            AppResult.success(filteredItems)
        } catch (e: Exception) {
            AppResult.error(
                AppException(
                    "Failed to search items: ${e.message}",
                    ErrorType.UNKNOWN,
                    e
                )
            )
        }
    }
    
    private fun searchInItem(item: InventoryItem, query: String): Boolean {
        val searchableFields = listOf(
            item.name,
            item.description,
            item.supplier,
            item.keywords,
            item.sku
        )
        
        return searchableFields.any { field ->
            field.lowercase().trim().contains(query)
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.length > 5
    }
}