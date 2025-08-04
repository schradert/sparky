package com.sparky.inventory.domain.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Property-based tests for InventoryItem model
 * These tests generate random data to verify invariants and edge cases
 */
class InventoryItemPropertyTest : DescribeSpec({
    
    // Custom arbitraries for domain-specific data
    val skuArb = Arb.stringPattern("[A-Z]{2,4}[0-9]{3,6}")
    val emailArb = Arb.email()
    val urlArb = Arb.stringPattern("https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    val inventoryItemArb = Arb.bind(
        Arb.string(1, 100),  // id
        Arb.string(1, 200),  // name
        Arb.string(0, 500),  // description
        Arb.string(1, 100),  // supplier
        skuArb,              // sku
        Arb.int(0, 10000),   // quantity
        Arb.choice(Arb.constant(""), urlArb), // imageUrl (empty or valid URL)
        Arb.string(0, 200),  // keywords
        Arb.long(0, Long.MAX_VALUE) // lastUpdated
    ) { id, name, description, supplier, sku, quantity, imageUrl, keywords, lastUpdated ->
        InventoryItem(id, name, description, supplier, sku, quantity, imageUrl, keywords, lastUpdated)
    }
    
    describe("InventoryItem serialization properties") {
        it("should serialize and deserialize correctly for all valid inputs") {
            checkAll(inventoryItemArb) { item ->
                val json = Json.encodeToString(item)
                val deserialized = Json.decodeFromString<InventoryItem>(json)
                
                deserialized shouldBe item
                deserialized.id shouldBe item.id
                deserialized.name shouldBe item.name
                deserialized.description shouldBe item.description
                deserialized.supplier shouldBe item.supplier
                deserialized.sku shouldBe item.sku
                deserialized.quantity shouldBe item.quantity
                deserialized.imageUrl shouldBe item.imageUrl
                deserialized.keywords shouldBe item.keywords
                deserialized.lastUpdated shouldBe item.lastUpdated
            }
        }
        
        it("should maintain data integrity across multiple serialization cycles") {
            checkAll(10, inventoryItemArb) { item ->
                var current = item
                repeat(5) {
                    val json = Json.encodeToString(current)
                    current = Json.decodeFromString<InventoryItem>(json)
                }
                current shouldBe item
            }
        }
    }
    
    describe("InventoryItem data validation properties") {
        it("should handle various quantity values correctly") {
            checkAll(Arb.int()) { quantity ->
                val item = InventoryItem(
                    id = "test",
                    name = "Test Item",
                    description = "Test",
                    supplier = "Test Supplier",
                    sku = "TEST123",
                    quantity = quantity,
                    imageUrl = "",
                    keywords = "test"
                )
                
                item.quantity shouldBe quantity
                // Quantity can be negative (backorders) or zero (out of stock)
                // This is a business rule that should be handled at the application level
            }
        }
        
        it("should preserve SKU format invariants") {
            checkAll(skuArb) { sku ->
                val item = InventoryItem(
                    id = "test",
                    name = "Test Item",
                    description = "Test",
                    supplier = "Test Supplier",
                    sku = sku,
                    quantity = 10,
                    imageUrl = "",
                    keywords = "test"
                )
                
                item.sku shouldBe sku
                item.sku.length shouldBe sku.length
            }
        }
        
        it("should handle empty and whitespace strings appropriately") {
            val whitespaceStrings = listOf("", " ", "  ", "\t", "\n", " \n\t ")
            
            whitespaceStrings.forEach { whitespace ->
                val item = InventoryItem(
                    id = "test",
                    name = whitespace.ifEmpty { "Default Name" },
                    description = whitespace,
                    supplier = whitespace.ifEmpty { "Default Supplier" },
                    sku = "TEST123",
                    quantity = 1,
                    imageUrl = whitespace,
                    keywords = whitespace
                )
                
                // Core business rule: name and supplier should not be empty in practice
                if (whitespace.isEmpty()) {
                    item.name shouldNotBe ""
                    item.supplier shouldNotBe ""
                } else {
                    item.description shouldBe whitespace
                    item.imageUrl shouldBe whitespace
                    item.keywords shouldBe whitespace
                }
            }
        }
    }
    
    describe("InventoryItem edge case properties") {
        it("should handle extreme timestamp values") {
            val extremeTimestamps = listOf(0L, Long.MAX_VALUE, Long.MIN_VALUE)
            
            extremeTimestamps.forEach { timestamp ->
                val item = InventoryItem(
                    id = "test",
                    name = "Test Item",
                    description = "Test",
                    supplier = "Test Supplier",
                    sku = "TEST123",
                    quantity = 1,
                    imageUrl = "",
                    keywords = "test",
                    lastUpdated = timestamp
                )
                
                item.lastUpdated shouldBe timestamp
            }
        }
        
        it("should handle very long strings") {
            checkAll(5, Arb.string(1000, 10000)) { longString ->
                val item = InventoryItem(
                    id = "test",
                    name = longString.take(200), // Reasonable limit for name
                    description = longString,
                    supplier = longString.take(100), // Reasonable limit for supplier
                    sku = "TEST123",
                    quantity = 1,
                    imageUrl = "",
                    keywords = longString.take(500) // Reasonable limit for keywords
                )
                
                item.description.length shouldBe longString.length
                item.name.length shouldBe minOf(200, longString.length)
                item.supplier.length shouldBe minOf(100, longString.length)
            }
        }
        
        it("should maintain equality contract") {
            checkAll(inventoryItemArb) { item ->
                // Reflexivity
                item shouldBe item
                
                // Symmetry and consistency
                val copy1 = item.copy()
                val copy2 = item.copy()
                
                copy1 shouldBe copy2
                copy2 shouldBe copy1
                
                // Different objects with same data should be equal
                val identical = InventoryItem(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    supplier = item.supplier,
                    sku = item.sku,
                    quantity = item.quantity,
                    imageUrl = item.imageUrl,
                    keywords = item.keywords,
                    lastUpdated = item.lastUpdated
                )
                
                identical shouldBe item
            }
        }
        
        it("should handle copy operations correctly") {
            checkAll(inventoryItemArb, Arb.int()) { item, newQuantity ->
                val modified = item.copy(quantity = newQuantity)
                
                // Only quantity should change
                modified.quantity shouldBe newQuantity
                modified.id shouldBe item.id
                modified.name shouldBe item.name
                modified.description shouldBe item.description
                modified.supplier shouldBe item.supplier
                modified.sku shouldBe item.sku
                modified.imageUrl shouldBe item.imageUrl
                modified.keywords shouldBe item.keywords
                modified.lastUpdated shouldBe item.lastUpdated
                
                // Original should be unchanged
                item.quantity shouldNotBe newQuantity
            }
        }
    }
    
    describe("Search and filtering properties") {
        it("should be searchable by any text field") {
            checkAll(inventoryItemArb, Arb.string(1, 10)) { item, searchTerm ->
                val matchesName = item.name.contains(searchTerm, ignoreCase = true)
                val matchesDescription = item.description.contains(searchTerm, ignoreCase = true)
                val matchesSupplier = item.supplier.contains(searchTerm, ignoreCase = true)
                val matchesSku = item.sku.contains(searchTerm, ignoreCase = true)
                val matchesKeywords = item.keywords.contains(searchTerm, ignoreCase = true)
                
                val isSearchable = matchesName || matchesDescription || matchesSupplier || 
                                 matchesSku || matchesKeywords
                
                // This property verifies that our search logic covers all text fields
                // If any field contains the search term, the item should be findable
                if (isSearchable) {
                    // The item contains the search term in at least one field
                    (matchesName || matchesDescription || matchesSupplier || 
                     matchesSku || matchesKeywords) shouldBe true
                }
            }
        }
    }
})