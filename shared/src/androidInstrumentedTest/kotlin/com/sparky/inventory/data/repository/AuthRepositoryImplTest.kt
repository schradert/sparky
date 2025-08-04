package com.sparky.inventory.data.repository

import app.cash.turbine.test
import com.sparky.inventory.data.remote.GoogleSheetsApi
import com.sparky.inventory.domain.model.User
import com.sparky.inventory.util.AppException
import com.sparky.inventory.util.AppResult
import com.sparky.inventory.util.ErrorType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.email
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class AuthRepositoryImplTest : DescribeSpec({
    
    describe("AuthRepositoryImpl login") {
        it("should login successfully with valid approved email") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            val email = "test@example.com"
            
            coEvery { googleSheetsApi.checkApprovedEmail(email) } returns AppResult.success(true)
            coEvery { localStorage.saveUser(any()) } returns Unit
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val result = repository.login(email)
            
            result.isSuccess() shouldBe true
            val user = result.getDataOrNull()
            user?.email shouldBe email
            user?.isApproved shouldBe true
            user?.sessionToken shouldNotBe null
            
            coVerify { localStorage.saveUser(any()) }
        }
        
        it("should fail login with unapproved email") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            val email = "unauthorized@example.com"
            
            coEvery { googleSheetsApi.checkApprovedEmail(email) } returns AppResult.success(false)
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val result = repository.login(email)
            
            result.isError() shouldBe true
            val error = result.getErrorOrNull()
            error?.type shouldBe ErrorType.AUTHORIZATION
            error?.message shouldBe "Email '$email' is not authorized. Contact your administrator for access."
        }
        
        it("should fail login with blank email") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val result = repository.login("")
            
            result.isError() shouldBe true
            val error = result.getErrorOrNull()
            error?.type shouldBe ErrorType.VALIDATION
            error?.message shouldBe "Email cannot be blank"
        }
        
        it("should handle API error during email check") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            val email = "test@example.com"
            val apiError = AppException("API failed", ErrorType.API_ERROR)
            
            coEvery { googleSheetsApi.checkApprovedEmail(email) } returns AppResult.error(apiError)
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val result = repository.login(email)
            
            result.isError() shouldBe true
            val error = result.getErrorOrNull()
            error?.type shouldBe ErrorType.API_ERROR
        }
        
        it("should handle unexpected exceptions during login") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            val email = "test@example.com"
            
            coEvery { googleSheetsApi.checkApprovedEmail(email) } throws RuntimeException("Unexpected error")
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val result = repository.login(email)
            
            result.isError() shouldBe true
            val error = result.getErrorOrNull()
            error?.type shouldBe ErrorType.UNKNOWN
        }
        
        it("property test: valid emails create proper user objects") = runTest {
            checkAll(Arb.email()) { email ->
                val googleSheetsApi = mockk<GoogleSheetsApi>()
                val localStorage = mockk<LocalStorage>()
                
                coEvery { googleSheetsApi.checkApprovedEmail(email) } returns AppResult.success(true)
                coEvery { localStorage.saveUser(any()) } returns Unit
                
                val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
                val result = repository.login(email)
                
                if (result.isSuccess()) {
                    val user = result.getDataOrNull()
                    user?.email shouldBe email
                    user?.isApproved shouldBe true
                }
            }
        }
    }
    
    describe("AuthRepositoryImpl session management") {
        it("should check auth status with valid session") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            val validUser = User(
                email = "test@example.com",
                isApproved = true,
                tokenExpiry = System.currentTimeMillis() + 100000L
            )
            
            coEvery { localStorage.getUser() } returns validUser
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            
            repository.isLoggedIn().test {
                val isLoggedIn = awaitItem()
                isLoggedIn shouldBe true
            }
        }
        
        it("should logout user with expired session") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            val expiredUser = User(
                email = "test@example.com",
                isApproved = true,
                tokenExpiry = System.currentTimeMillis() - 1000L
            )
            
            coEvery { localStorage.getUser() } returns expiredUser
            coEvery { localStorage.clearUser() } returns Unit
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            
            repository.isLoggedIn().test {
                val isLoggedIn = awaitItem()
                isLoggedIn shouldBe false
            }
            
            coVerify { localStorage.clearUser() }
        }
        
        it("should handle missing user in storage") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            
            coEvery { localStorage.getUser() } returns null
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            
            repository.isLoggedIn().test {
                val isLoggedIn = awaitItem()
                isLoggedIn shouldBe false
            }
        }
    }
    
    describe("AuthRepositoryImpl logout") {
        it("should clear user data on logout") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            
            coEvery { localStorage.clearUser() } returns Unit
            coEvery { localStorage.getUser() } returns null
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            repository.logout()
            
            coVerify { localStorage.clearUser() }
            
            repository.isLoggedIn().test {
                val isLoggedIn = awaitItem()
                isLoggedIn shouldBe false
            }
        }
    }
    
    describe("AuthRepositoryImpl getCurrentUser") {
        it("should return current user from storage") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            val testUser = User(email = "test@example.com", isApproved = true)
            
            coEvery { localStorage.getUser() } returns testUser
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val user = repository.getCurrentUser()
            
            user shouldBe testUser
        }
        
        it("should return null when no user is stored") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            
            coEvery { localStorage.getUser() } returns null
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val user = repository.getCurrentUser()
            
            user shouldBe null
        }
    }
    
    describe("Error handling edge cases") {
        it("should handle storage exceptions gracefully during auth check") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            
            coEvery { localStorage.getUser() } throws RuntimeException("Storage error")
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            
            repository.isLoggedIn().test {
                val isLoggedIn = awaitItem()
                isLoggedIn shouldBe false
            }
        }
        
        it("should handle storage failures during login") = runTest {
            val googleSheetsApi = mockk<GoogleSheetsApi>()
            val localStorage = mockk<LocalStorage>()
            val email = "test@example.com"
            
            coEvery { googleSheetsApi.checkApprovedEmail(email) } returns AppResult.success(true)
            coEvery { localStorage.saveUser(any()) } throws RuntimeException("Storage full")
            
            val repository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val result = repository.login(email)
            
            result.isError() shouldBe true
            val error = result.getErrorOrNull()
            error?.type shouldBe ErrorType.UNKNOWN
        }
    }
})