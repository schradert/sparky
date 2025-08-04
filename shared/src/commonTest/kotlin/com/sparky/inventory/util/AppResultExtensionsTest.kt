package com.sparky.inventory.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class AppResultExtensionsTest : DescribeSpec({
    
    describe("Result to AppResult conversion") {
        
        it("should convert successful Result to AppResult.Success") {
            val result = Result.success("test data")
            val appResult = result.toAppResult()
            
            appResult.shouldBeInstanceOf<AppResult.Success<String>>()
            appResult.isSuccess() shouldBe true
            appResult.getDataOrNull() shouldBe "test data"
        }
        
        it("should convert failed Result to AppResult.Error") {
            val exception = RuntimeException("Test error")
            val result = Result.failure<String>(exception)
            val appResult = result.toAppResult()
            
            appResult.shouldBeInstanceOf<AppResult.Error>()
            appResult.isError() shouldBe true
            appResult.getErrorOrNull()?.type shouldBe ErrorType.UNKNOWN
            appResult.getErrorOrNull()?.originalException shouldBe exception
        }
        
        it("should handle null exception in failed Result") {
            val result = runCatching { throw Exception() }
            val appResult = result.toAppResult()
            
            appResult.shouldBeInstanceOf<AppResult.Error>()
            appResult.isError() shouldBe true
        }
        
        it("property test: successful conversions preserve data") {
            checkAll(Arb.string()) { testData ->
                val result = Result.success(testData)
                val appResult = result.toAppResult()
                
                appResult.isSuccess() shouldBe true
                appResult.getDataOrNull() shouldBe testData
            }
        }
        
        it("property test: failed conversions create errors") {
            checkAll(Arb.string()) { errorMessage ->
                val exception = RuntimeException(errorMessage)
                val result = Result.failure<String>(exception)
                val appResult = result.toAppResult()
                
                appResult.isError() shouldBe true
                appResult.getErrorOrNull()?.message shouldBe errorMessage
            }
        }
        
        it("should handle complex data types") {
            data class TestData(val value: String, val number: Int)
            
            val testData = TestData("test", 42)
            val result = Result.success(testData)
            val appResult = result.toAppResult()
            
            appResult.isSuccess() shouldBe true
            appResult.getDataOrNull() shouldBe testData
        }
        
        it("should maintain error details in conversion") {
            val originalException = IllegalArgumentException("Invalid argument")
            val result = Result.failure<Int>(originalException)
            val appResult = result.toAppResult()
            
            appResult.isError() shouldBe true
            val error = appResult.getErrorOrNull()!!
            error.type shouldBe ErrorType.UNKNOWN
            error.originalException shouldBe originalException
            error.message shouldBe "Invalid argument"
        }
    }
})