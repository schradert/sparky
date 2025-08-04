package com.sparky.inventory.domain.model

import com.sparky.inventory.util.PlatformUtils
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.property.Arb
import io.kotest.property.arbitrary.email
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

class UserTest : DescribeSpec({
    
    describe("User creation") {
        it("should create user with default values") {
            val email = "test@example.com"
            val user = User(email = email)
            
            user.email shouldBe email
            user.isApproved shouldBe false
            user.lastLogin shouldNotBe 0L
            user.sessionToken shouldStartWith "session_"
            user.tokenExpiry shouldNotBe 0L
        }
        
        it("should create user with explicit values") {
            val email = "admin@company.com"
            val lastLogin = 1234567890L
            val user = User(
                email = email,
                isApproved = true,
                lastLogin = lastLogin
            )
            
            user.email shouldBe email
            user.isApproved shouldBe true
            user.lastLogin shouldBe lastLogin
        }
        
        it("property test: user creation with valid emails") {
            checkAll(Arb.email()) { email ->
                val user = User(email = email)
                user.email shouldBe email
                user.sessionToken shouldStartWith "session_"
                (user.tokenExpiry > user.lastLogin) shouldBe true
            }
        }
    }
    
    describe("Session validation") {
        it("should validate active session") {
            val futureExpiry = PlatformUtils.currentTimeMillis() + 1000000L
            val user = User(
                email = "test@example.com",
                tokenExpiry = futureExpiry
            )
            
            user.isSessionValid() shouldBe true
        }
        
        it("should invalidate expired session") {
            val pastExpiry = PlatformUtils.currentTimeMillis() - 1000L
            val user = User(
                email = "test@example.com",
                tokenExpiry = pastExpiry
            )
            
            user.isSessionValid() shouldBe false
        }
        
        it("should calculate remaining session time") {
            val currentTime = PlatformUtils.currentTimeMillis()
            val oneHour = 60 * 60 * 1000L
            val futureExpiry = currentTime + oneHour
            
            val user = User(
                email = "test@example.com",
                tokenExpiry = futureExpiry
            )
            
            val remaining = user.getRemainingSessionTime()
            remaining shouldBe (futureExpiry - currentTime)
        }
        
        it("should return zero for expired sessions") {
            val pastExpiry = PlatformUtils.currentTimeMillis() - 1000L
            val user = User(
                email = "test@example.com",
                tokenExpiry = pastExpiry
            )
            
            user.getRemainingSessionTime() shouldBe 0L
        }
        
        it("property test: session validation consistency") {
            checkAll(Arb.email(), Arb.long()) { email, timeOffset ->
                val currentTime = PlatformUtils.currentTimeMillis()
                val expiry = currentTime + timeOffset
                val user = User(email = email, tokenExpiry = expiry)
                
                val isValid = user.isSessionValid()
                val remaining = user.getRemainingSessionTime()
                
                if (timeOffset > 0) {
                    isValid shouldBe true
                    remaining shouldBe timeOffset
                } else {
                    isValid shouldBe false
                    remaining shouldBe 0L
                }
            }
        }
    }
    
    describe("Session constants") {
        it("should have correct session duration") {
            User.SESSION_DURATION_MS shouldBe (7 * 24 * 60 * 60 * 1000L)
        }
        
        it("should generate unique session tokens") {
            val user1 = User(email = "test1@example.com")
            val user2 = User(email = "test2@example.com")
            
            user1.sessionToken shouldNotBe user2.sessionToken
            user1.sessionToken shouldStartWith "session_"
            user2.sessionToken shouldStartWith "session_"
        }
    }
    
    describe("Edge cases") {
        it("should handle minimum timestamp values") {
            val user = User(
                email = "test@example.com",
                lastLogin = 0L,
                tokenExpiry = 0L
            )
            
            user.isSessionValid() shouldBe false
            user.getRemainingSessionTime() shouldBe 0L
        }
        
        it("should handle maximum timestamp values") {
            val maxTime = Long.MAX_VALUE
            val user = User(
                email = "test@example.com",
                tokenExpiry = maxTime
            )
            
            user.isSessionValid() shouldBe true
            user.getRemainingSessionTime() shouldBe (maxTime - PlatformUtils.currentTimeMillis())
        }
    }
})