package com.sparky.inventory.presentation.viewmodel

import app.cash.turbine.test
import com.sparky.inventory.domain.model.User
import com.sparky.inventory.testutil.FakeAuthRepository
import com.sparky.inventory.testutil.TestData
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest : DescribeSpec({
    
    val testDispatcher = StandardTestDispatcher()
    
    beforeTest {
        Dispatchers.setMain(testDispatcher)
    }
    
    afterTest {
        Dispatchers.resetMain()
    }
    
    describe("AuthViewModel initialization") {
        it("should initialize with default state when not logged in") = runTest {
            val authRepository = FakeAuthRepository()
            
            val viewModel = AuthViewModel(authRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.uiState.test {
                val state = awaitItem()
                state.isLoading shouldBe false
                state.isLoggedIn shouldBe false
                state.user shouldBe null
                state.error shouldBe null
            }
        }
        
        it("should initialize with logged in state when user exists") = runTest {
            val authRepository = FakeAuthRepository()
            val testUser = TestData.createUser("test@example.com")
            
            // Pre-login the user in the fake repository
            authRepository.login("test@example.com")
            
            val viewModel = AuthViewModel(authRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.uiState.test {
                val state = awaitItem()
                state.isLoggedIn shouldBe true
                state.user shouldNotBe null
                state.user?.email shouldBe "test@example.com"
            }
        }
    }
    
    describe("Login functionality") {
        it("should handle successful login") = runTest {
            val authRepository = mockk<AuthRepository>()
            val testUser = User(email = "test@example.com", isApproved = true)
            
            every { authRepository.isLoggedIn() } returns flowOf(false, true)
            coEvery { authRepository.getCurrentUser() } returns null andThen testUser
            coEvery { authRepository.login("test@example.com") } returns AppResult.success(testUser)
            
            val viewModel = AuthViewModel(authRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.uiState.test {
                // Initial state
                val initialState = awaitItem()
                initialState.isLoading shouldBe false
                initialState.error shouldBe null
                
                // Start login
                viewModel.login("test@example.com")
                testDispatcher.scheduler.advanceUntilIdle()
                
                // Loading state
                val loadingState = awaitItem()
                loadingState.isLoading shouldBe true
                loadingState.error shouldBe null
                
                // Success state
                val successState = awaitItem()
                successState.isLoading shouldBe false
                successState.isLoggedIn shouldBe true
                successState.user shouldBe testUser
                successState.error shouldBe null
            }
            
            coVerify { authRepository.login("test@example.com") }
        }
        
        it("should handle login failure") = runTest {
            val authRepository = mockk<AuthRepository>()
            val errorMessage = "Invalid email"
            val exception = AppException(errorMessage, ErrorType.VALIDATION)
            
            every { authRepository.isLoggedIn() } returns flowOf(false)
            coEvery { authRepository.getCurrentUser() } returns null
            coEvery { authRepository.login("invalid@email") } returns AppResult.error(exception)
            
            val viewModel = AuthViewModel(authRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.uiState.test {
                // Initial state
                awaitItem()
                
                // Start login
                viewModel.login("invalid@email")
                testDispatcher.scheduler.advanceUntilIdle()
                
                // Loading state
                val loadingState = awaitItem()
                loadingState.isLoading shouldBe true
                
                // Error state
                val errorState = awaitItem()
                errorState.isLoading shouldBe false
                errorState.isLoggedIn shouldBe false
                errorState.error shouldBe errorMessage
            }
        }
        
        it("should handle empty email validation") = runTest {
            val authRepository = mockk<AuthRepository>()
            every { authRepository.isLoggedIn() } returns flowOf(false)
            coEvery { authRepository.getCurrentUser() } returns null
            coEvery { authRepository.login("") } returns AppResult.error("Email cannot be blank", ErrorType.VALIDATION)
            
            val viewModel = AuthViewModel(authRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.login("")
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.uiState.test {
                val state = awaitItem()
                state.error shouldBe "Email cannot be blank"
            }
        }
    }
    
    describe("Logout functionality") {
        it("should handle logout successfully") = runTest {
            val authRepository = mockk<AuthRepository>()
            val testUser = User(email = "test@example.com", isApproved = true)
            
            every { authRepository.isLoggedIn() } returns flowOf(true, false)
            coEvery { authRepository.getCurrentUser() } returns testUser andThen null
            coEvery { authRepository.logout() } returns Unit
            
            val viewModel = AuthViewModel(authRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.uiState.test {
                // Initial logged in state
                val loggedInState = awaitItem()
                loggedInState.isLoggedIn shouldBe true
                loggedInState.user shouldBe testUser
                
                // Perform logout
                viewModel.logout()
                testDispatcher.scheduler.advanceUntilIdle()
                
                // Logged out state
                val loggedOutState = awaitItem()
                loggedOutState.isLoading shouldBe false
                loggedOutState.isLoggedIn shouldBe false
                loggedOutState.user shouldBe null
                loggedOutState.error shouldBe null
            }
            
            coVerify { authRepository.logout() }
        }
    }
    
    describe("Error handling") {
        it("should clear error when requested") = runTest {
            val authRepository = mockk<AuthRepository>()
            every { authRepository.isLoggedIn() } returns flowOf(false)
            coEvery { authRepository.getCurrentUser() } returns null
            coEvery { authRepository.login("invalid") } returns AppResult.error("Error", ErrorType.VALIDATION)
            
            val viewModel = AuthViewModel(authRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Cause an error
            viewModel.login("invalid")
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.uiState.test {
                val errorState = awaitItem()
                errorState.error shouldBe "Error"
                
                // Clear error
                viewModel.clearError()
                
                val clearedState = awaitItem()
                clearedState.error shouldBe null
            }
        }
        
        it("should handle network errors gracefully") = runTest {
            val authRepository = mockk<AuthRepository>()
            val networkError = AppException("Network timeout", ErrorType.NETWORK)
            
            every { authRepository.isLoggedIn() } returns flowOf(false)
            coEvery { authRepository.getCurrentUser() } returns null
            coEvery { authRepository.login(any()) } returns AppResult.error(networkError)
            
            val viewModel = AuthViewModel(authRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.login("test@example.com")
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.uiState.test {
                val state = awaitItem()
                state.error shouldBe "Network timeout"
                state.isLoading shouldBe false
            }
        }
    }
    
    describe("State management") {
        it("should maintain consistent state during multiple operations") = runTest {
            val authRepository = mockk<AuthRepository>()
            every { authRepository.isLoggedIn() } returns flowOf(false)
            coEvery { authRepository.getCurrentUser() } returns null
            coEvery { authRepository.login("valid@email.com") } returns AppResult.success(
                User(email = "valid@email.com", isApproved = true)
            )
            coEvery { authRepository.login("invalid@email.com") } returns AppResult.error(
                "Invalid email", ErrorType.VALIDATION
            )
            
            val viewModel = AuthViewModel(authRepository)
            testDispatcher.scheduler.advanceUntilIdle()
            
            // First login attempt (failure)
            viewModel.login("invalid@email.com")
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Clear error
            viewModel.clearError()
            
            // Second login attempt (success)  
            viewModel.login("valid@email.com")
            testDispatcher.scheduler.advanceUntilIdle()
            
            viewModel.uiState.test {
                val finalState = awaitItem()
                finalState.error shouldBe null
                finalState.isLoading shouldBe false
            }
        }
    }
})