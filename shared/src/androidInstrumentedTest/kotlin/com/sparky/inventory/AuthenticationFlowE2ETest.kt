package com.sparky.inventory.e2e

import app.cash.turbine.test
import com.sparky.inventory.config.ApiKeyManager
import com.sparky.inventory.data.remote.GoogleSheetsApi
import com.sparky.inventory.data.repository.AuthRepositoryImpl
import com.sparky.inventory.data.repository.LocalStorage
import com.sparky.inventory.domain.model.User
import com.sparky.inventory.presentation.viewmodel.AuthViewModel
import com.sparky.inventory.util.AppResult
import com.sparky.inventory.util.PlatformUtils
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json

/**
 * End-to-End tests for complete authentication flows
 * These tests simulate real user interactions from UI to storage
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationFlowE2ETest : DescribeSpec({
    
    val testDispatcher = StandardTestDispatcher()
    
    beforeTest {
        Dispatchers.setMain(testDispatcher)
    }
    
    afterTest {
        Dispatchers.resetMain()
    }
    
    fun createMockHttpClient(approvedEmails: List<String>): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when {
                        request.url.encodedPath.contains("approved_emails") -> {
                            val emailValues = listOf(listOf("Email")) + approvedEmails.map { listOf(it) }
                            val response = """{"values": ${Json.encodeToString(emailValues)}}"""
                            respond(
                                content = ByteReadChannel(response),
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                        else -> {
                            respond(
                                content = ByteReadChannel("""{"error": "Not found"}"""),
                                status = HttpStatusCode.NotFound
                            )
                        }
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    
    fun createMockApiKeyManager(): ApiKeyManager {
        return mockk<ApiKeyManager> {
            every { getGoogleSheetsApiKey() } returns "test-api-key"
            every { getInventorySheetId() } returns "test-inventory-sheet-id"
            every { getApprovedEmailsSheetId() } returns "test-emails-sheet-id"
            every { validateConfiguration() } returns Unit
        }
    }
    
    describe("Complete authentication flow E2E") {
        it("should complete successful login flow from UI to storage") = runTest {
            // Setup infrastructure
            val httpClient = createMockHttpClient(listOf("admin@company.com", "user@company.com"))
            val apiKeyManager = createMockApiKeyManager()
            val localStorage = mockk<LocalStorage>()
            
            var savedUser: User? = null
            coEvery { localStorage.saveUser(any()) } answers {
                savedUser = firstArg()
                Unit
            }
            coEvery { localStorage.getUser() } answers { savedUser }
            coEvery { localStorage.clearUser() } answers { 
                savedUser = null
                Unit 
            }
            
            // Create the complete stack
            val googleSheetsApi = GoogleSheetsApi(apiKeyManager, httpClient)
            val authRepository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val authViewModel = AuthViewModel(authRepository)
            
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Test the complete flow
            authViewModel.uiState.test {
                // 1. Initial state - not logged in
                val initialState = awaitItem()
                initialState.isLoggedIn shouldBe false
                initialState.user shouldBe null
                initialState.error shouldBe null
                
                // 2. User attempts login with approved email
                authViewModel.login("admin@company.com")
                testDispatcher.scheduler.advanceUntilIdle()
                
                // 3. Loading state appears
                val loadingState = awaitItem()
                loadingState.isLoading shouldBe true
                loadingState.error shouldBe null
                
                // 4. Successful login state
                val successState = awaitItem()
                successState.isLoading shouldBe false
                successState.isLoggedIn shouldBe true
                successState.user shouldNotBe null
                successState.user?.email shouldBe "admin@company.com"
                successState.user?.isApproved shouldBe true
                successState.error shouldBe null
                
                // 5. Verify data was saved to storage  
                coVerify { localStorage.saveUser(any()) }
                savedUser shouldNotBe null
                savedUser?.email shouldBe "admin@company.com"
                savedUser?.isSessionValid() shouldBe true
                
                // 6. User logs out
                authViewModel.logout()
                testDispatcher.scheduler.advanceUntilIdle()
                
                // 7. Logged out state
                val loggedOutState = awaitItem()
                loggedOutState.isLoggedIn shouldBe false
                loggedOutState.user shouldBe null
                
                // 8. Verify data was cleared from storage
                coVerify { localStorage.clearUser() }
                savedUser shouldBe null
            }
        }
        
        it("should handle unauthorized user flow completely") = runTest {
            // Setup infrastructure with limited approved emails
            val httpClient = createMockHttpClient(listOf("admin@company.com"))
            val apiKeyManager = createMockApiKeyManager()
            val localStorage = mockk<LocalStorage>()
            
            coEvery { localStorage.getUser() } returns null
            
            // Create the complete stack
            val googleSheetsApi = GoogleSheetsApi(apiKeyManager, httpClient)
            val authRepository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val authViewModel = AuthViewModel(authRepository)
            
            testDispatcher.scheduler.advanceUntilIdle()
            
            authViewModel.uiState.test {
                // 1. Initial state
                val initialState = awaitItem()
                initialState.isLoggedIn shouldBe false
                
                // 2. User attempts login with unauthorized email
                authViewModel.login("unauthorized@hacker.com")
                testDispatcher.scheduler.advanceUntilIdle()
                
                // 3. Loading state
                val loadingState = awaitItem()
                loadingState.isLoading shouldBe true
                
                // 4. Error state - authorization failed
                val errorState = awaitItem()
                errorState.isLoading shouldBe false
                errorState.isLoggedIn shouldBe false
                errorState.error shouldBe "Email 'unauthorized@hacker.com' is not authorized. Contact your administrator for access."
                
                // 5. Verify no data was saved
                coVerify(exactly = 0) { localStorage.saveUser(any()) }
                
                // 6. User clears error and tries again with valid email
                authViewModel.clearError()
                
                val clearedState = awaitItem()
                clearedState.error shouldBe null
                
                authViewModel.login("admin@company.com")
                testDispatcher.scheduler.advanceUntilIdle()
                
                // 7. This time it should succeed
                awaitItem() // loading
                val validLoginState = awaitItem()
                validLoginState.isLoggedIn shouldBe true
                validLoginState.error shouldBe null
            }
        }
        
        it("should handle session expiry flow") = runTest {
            val httpClient = createMockHttpClient(listOf("user@company.com"))
            val apiKeyManager = createMockApiKeyManager()
            val localStorage = mockk<LocalStorage>()
            
            // Create expired user
            val expiredUser = User(
                email = "user@company.com",
                isApproved = true,
                tokenExpiry = PlatformUtils.currentTimeMillis() - 1000L // Expired 1 second ago
            )
            
            var storedUser: User? = expiredUser
            coEvery { localStorage.getUser() } answers { storedUser }
            coEvery { localStorage.clearUser() } answers { 
                storedUser = null
                Unit 
            }
            coEvery { localStorage.saveUser(any()) } answers {
                storedUser = firstArg()
                Unit
            }
            
            // Create the complete stack
            val googleSheetsApi = GoogleSheetsApi(apiKeyManager, httpClient)
            val authRepository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val authViewModel = AuthViewModel(authRepository)
            
            testDispatcher.scheduler.advanceUntilIdle()
            
            authViewModel.uiState.test {
                // Session should be detected as expired and user logged out automatically
                val state = awaitItem()
                state.isLoggedIn shouldBe false
                state.user shouldBe null
                
                // Verify expired session was cleared
                coVerify { localStorage.clearUser() }
                storedUser shouldBe null
            }
        }
        
        it("should handle network errors in complete flow") = runTest {
            // Create client that fails requests
            val failingClient = HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        throw kotlinx.coroutines.TimeoutCancellationException("Network timeout")
                    }
                }
                install(ContentNegotiation) {
                    json()
                }
            }
            
            val apiKeyManager = createMockApiKeyManager()
            val localStorage = mockk<LocalStorage>()
            coEvery { localStorage.getUser() } returns null
            
            // Create the complete stack with failing network
            val googleSheetsApi = GoogleSheetsApi(apiKeyManager, failingClient)
            val authRepository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val authViewModel = AuthViewModel(authRepository)
            
            testDispatcher.scheduler.advanceUntilIdle()
            
            authViewModel.uiState.test {
                // Initial state
                awaitItem()
                
                // Attempt login - should fail due to network
                authViewModel.login("test@company.com")
                testDispatcher.scheduler.advanceUntilIdle()
                
                // Loading state
                awaitItem()
                
                // Network error state
                val errorState = awaitItem()
                errorState.isLoading shouldBe false
                errorState.isLoggedIn shouldBe false
                errorState.error shouldNotBe null
                errorState.error shouldBe "Failed to verify email: Request timeout"
                
                // Verify no data was saved despite the attempt
                coVerify(exactly = 0) { localStorage.saveUser(any()) }
            }
        }
        
        it("should maintain state consistency across app restarts") = runTest {
            val httpClient = createMockHttpClient(listOf("persistent@company.com"))
            val apiKeyManager = createMockApiKeyManager()
            val localStorage = mockk<LocalStorage>()
            
            // Simulate user who was previously logged in
            val validUser = User(
                email = "persistent@company.com",
                isApproved = true,
                tokenExpiry = PlatformUtils.currentTimeMillis() + 100000L // Valid for another 100 seconds
            )
            
            coEvery { localStorage.getUser() } returns validUser
            
            // Create the stack (simulating app restart)
            val googleSheetsApi = GoogleSheetsApi(apiKeyManager, httpClient)
            val authRepository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val authViewModel = AuthViewModel(authRepository)
            
            testDispatcher.scheduler.advanceUntilIdle()
            
            authViewModel.uiState.test {
                // Should automatically restore logged in state
                val restoredState = awaitItem()
                restoredState.isLoggedIn shouldBe true
                restoredState.user?.email shouldBe "persistent@company.com"
                restoredState.error shouldBe null
                
                // Verify session is still valid
                restoredState.user?.isSessionValid() shouldBe true
            }
        }
    }
    
    describe("Security edge cases E2E") {
        it("should prevent session fixation attacks") = runTest {
            val httpClient = createMockHttpClient(listOf("user1@company.com", "user2@company.com"))
            val apiKeyManager = createMockApiKeyManager()
            val localStorage = mockk<LocalStorage>()
            
            var savedUsers = mutableListOf<User>()
            coEvery { localStorage.saveUser(any()) } answers {
                savedUsers.add(firstArg())
                Unit
            }
            coEvery { localStorage.getUser() } answers { savedUsers.lastOrNull() }
            coEvery { localStorage.clearUser() } answers { 
                savedUsers.clear()
                Unit 
            }
            
            val googleSheetsApi = GoogleSheetsApi(apiKeyManager, httpClient)
            val authRepository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val authViewModel = AuthViewModel(authRepository)
            
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Login as first user
            authViewModel.login("user1@company.com")
            testDispatcher.scheduler.advanceUntilIdle()
            
            val firstUserToken = savedUsers.last().sessionToken
            
            // Logout
            authViewModel.logout()
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Login as second user
            authViewModel.login("user2@company.com")
            testDispatcher.scheduler.advanceUntilIdle()
            
            val secondUserToken = savedUsers.last().sessionToken
            
            // Verify different users get different session tokens
            firstUserToken shouldNotBe secondUserToken
            
            // Verify each user has their own identity
            savedUsers.size shouldBe 2
            savedUsers[0].email shouldBe "user1@company.com"
            savedUsers[1].email shouldBe "user2@company.com"
        }
        
        it("should handle rapid login attempts gracefully") = runTest {
            val httpClient = createMockHttpClient(listOf("rapid@company.com"))
            val apiKeyManager = createMockApiKeyManager()
            val localStorage = mockk<LocalStorage>()
            
            coEvery { localStorage.saveUser(any()) } returns Unit
            coEvery { localStorage.getUser() } returns null
            
            val googleSheetsApi = GoogleSheetsApi(apiKeyManager, httpClient)
            val authRepository = AuthRepositoryImpl(googleSheetsApi, localStorage)
            val authViewModel = AuthViewModel(authRepository)
            
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Simulate rapid login attempts
            repeat(5) {
                authViewModel.login("rapid@company.com")
                delay(10) // Small delay between attempts
            }
            
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Verify the system remains stable and eventually succeeds
            authViewModel.uiState.test {
                val finalState = awaitItem()
                // Should either be logged in successfully or show a reasonable error
                // The key is that the system doesn't crash or become unresponsive
                finalState.isLoading shouldBe false
            }
        }
    }
})