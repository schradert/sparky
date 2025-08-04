package com.sparky.inventory.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class AppResultTest : DescribeSpec({
    
    describe("AppResult.Success") {
        it("should create success result with data") {
            val data = "test data"
            val result = AppResult.success(data)
            
            result.shouldBeInstanceOf<AppResult.Success<String>>()
            result.isSuccess() shouldBe true
            result.isError() shouldBe false
            result.getDataOrNull() shouldBe data
            result.getErrorOrNull() shouldBe null
        }
        
        it("should handle null data correctly") {
            val result = AppResult.success<String?>(null)
            
            result.isSuccess() shouldBe true
            result.getDataOrNull() shouldBe null
        }
        
        it("property test: success results are always successful") {
            checkAll(Arb.string()) { data ->
                val result = AppResult.success(data)
                result.isSuccess() shouldBe true
                result.isError() shouldBe false
                result.getDataOrNull() shouldBe data
            }
        }
    }
    
    describe("AppResult.Error") {
        it("should create error result with exception") {
            val exception = AppException("Test error", ErrorType.VALIDATION)
            val result = AppResult.error(exception)
            
            result.shouldBeInstanceOf<AppResult.Error>()
            result.isSuccess() shouldBe false
            result.isError() shouldBe true
            result.getDataOrNull() shouldBe null
            result.getErrorOrNull() shouldBe exception
        }
        
        it("should create error result with message and type") {
            val message = "Network error"
            val type = ErrorType.NETWORK
            val result = AppResult.error(message, type)
            
            result.isError() shouldBe true
            val error = result.getErrorOrNull()
            error shouldNotBe null
            error!!.message shouldBe message
            error.type shouldBe type
        }
        
        it("property test: error results are always failures") {
            checkAll(Arb.string(), Arb.int()) { message, _ ->
                val result = AppResult.error(message, ErrorType.UNKNOWN)
                result.isSuccess() shouldBe false
                result.isError() shouldBe true
                result.getDataOrNull() shouldBe null
                result.getErrorOrNull()?.message shouldBe message
            }
        }
    }
    
    describe("AppResult utility methods") {
        it("should get data or default value") {
            val successResult = AppResult.success("data")
            val errorResult: AppResult<String> = AppResult.error("error", ErrorType.UNKNOWN)
            
            successResult.getDataOrElse { "default" } shouldBe "data"
            errorResult.getDataOrElse { "default" } shouldBe "default"
        }
        
        it("should handle onSuccess and onError callbacks") {
            var successCalled = false
            var errorCalled = false
            var receivedData: String? = null
            var receivedError: AppException? = null
            
            val successResult = AppResult.success("test")
            successResult.onSuccess { 
                successCalled = true
                receivedData = it
            }.onError {
                errorCalled = true
                receivedError = it
            }
            
            successCalled shouldBe true
            errorCalled shouldBe false
            receivedData shouldBe "test"
            receivedError shouldBe null
        }
        
        it("should handle error callbacks") {
            var successCalled = false
            var errorCalled = false
            var receivedError: AppException? = null
            
            val exception = AppException("error", ErrorType.API_ERROR)
            val errorResult = AppResult.error(exception)
            errorResult.onSuccess { 
                successCalled = true
            }.onError {
                errorCalled = true
                receivedError = it
            }
            
            successCalled shouldBe false
            errorCalled shouldBe true
            receivedError shouldBe exception
        }
    }
    
    describe("ErrorType enumeration") {
        it("should have all expected error types") {
            val expectedTypes = setOf(
                ErrorType.NETWORK,
                ErrorType.API_ERROR,
                ErrorType.VALIDATION,
                ErrorType.AUTHORIZATION,
                ErrorType.CONFIGURATION,
                ErrorType.UNKNOWN
            )
            
            ErrorType.entries.toSet() shouldBe expectedTypes
        }
    }
    
    describe("AppException") {
        it("should create exception with message and type") {
            val message = "Test exception"
            val type = ErrorType.VALIDATION
            val exception = AppException(message, type)
            
            exception.message shouldBe message
            exception.type shouldBe type
            exception.cause shouldBe null
        }
        
        it("should create exception with cause") {
            val cause = RuntimeException("Root cause")
            val exception = AppException("Wrapper", ErrorType.UNKNOWN, cause)
            
            exception.cause shouldBe cause
        }
        
        it("property test: exception properties are preserved") {
            checkAll(Arb.string()) { message ->
                val exception = AppException(message, ErrorType.NETWORK)
                exception.message shouldBe message
                exception.type shouldBe ErrorType.NETWORK
            }
        }
    }
})