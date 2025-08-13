package com.sparkysballoons.invx.data

import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.sparkysballoons.invx.auth.GoogleAuthRepository
import com.sparkysballoons.invx.domain.ApiError
import com.sparkysballoons.invx.domain.DomainResult
import com.sparkysballoons.invx.domain.HttpError
import com.sparkysballoons.invx.domain.InventoryApi
import com.sparkysballoons.invx.domain.Product
import com.sparkysballoons.invx.domain.runMapCatch
import com.github.michaelbull.result.toResultOr
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.request.get
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class SheetsResponse(
    val values: List<List<String>>
)

class SheetsInventoryApi(private val googleOauthRepository: GoogleAuthRepository) : InventoryApi {
    private val sheetId = "11HdaY0ZoqDAPA7-fYAKl0GSp54mUMOAuTYVV4B91pwI"
    private val url = "https://sheets.googleapis.com/v4/spreadsheets/$sheetId/values/products"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
            sanitizeHeader { it == HttpHeaders.Authorization }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
        install(Auth) {
            bearer {
                loadTokens {
                    // TODO handle mangled token error
                    with (googleOauthRepository.getStoredToken()!!) {
                        BearerTokens(accessToken, refreshToken)
                    }
                }
            }
        }
    }

    private fun parseProductRow(row: List<String>, headers: List<String>): Product {
        val itemMap = headers.zip(row) { header, value -> header to (value?.trim() ?: "") }.toMap()
        return Product(
            uniqueIdSku = itemMap["unique_id_sku"]!!,
            productType = itemMap["product_type"]!!,
            manufacturerColor = itemMap["manufacturer_color"]!!,
            brand = itemMap["brand"]!!,
            size = itemMap["size"]!!.toDouble(),
            texture = itemMap["texture"]!!,
            bagQuantity = itemMap["bag_quantity"]!!.toInt(),
            shape = itemMap["shape"]!!,
            distributor = itemMap["distributor"]!!,
            occasion = itemMap["occasion"]!!,
            quantity = itemMap["quantity"]!!.toInt(),
        )
    }

    private fun mapThrowable(t: Throwable) = when (t) {
        is ClientRequestException -> HttpError(t)
        else -> ApiError(t.message ?: "Unknown error")
    }


    override suspend fun getProducts(): DomainResult<List<Product>> = runMapCatch(::mapThrowable) {
        val rows = client.get(url).body<SheetsResponse>().values
        rows.drop(1).mapNotNull { parseProductRow(it, rows[0].map { it.lowercase() }) }                
    }

    override suspend fun getProductById(id: String): DomainResult<Product> = coroutineBinding {
        getProducts()
            .bind()
            .find { it.uniqueIdSku == id }
            .toResultOr { ApiError("No product found with ID: $id") }
            .bind()
    }
}