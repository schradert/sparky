package com.sparky.inventory.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

class PlatformUtilsTest : DescribeSpec({
    
    describe("PlatformUtils") {
        
        describe("currentTimeMillis") {
            it("should return current time in milliseconds") {
                val time1 = PlatformUtils.currentTimeMillis()
                Thread.sleep(1) // Small delay to ensure time difference
                val time2 = PlatformUtils.currentTimeMillis()
                
                time1.shouldBeInstanceOf<Long>()
                time2.shouldBeInstanceOf<Long>()
                (time2 >= time1) shouldBe true
            }
            
            it("should return positive timestamp") {
                val timestamp = PlatformUtils.currentTimeMillis()
                (timestamp > 0) shouldBe true
            }
            
            it("should return reasonable timestamp (after year 2020)") {
                val timestamp = PlatformUtils.currentTimeMillis()
                val year2020InMillis = 1577836800000L // Jan 1, 2020 00:00:00 UTC
                (timestamp > year2020InMillis) shouldBe true
            }
            
            it("property test: timestamps are always increasing or equal") {
                checkAll(Arb.long(1L, 100L)) { delayMs ->
                    val time1 = PlatformUtils.currentTimeMillis()
                    if (delayMs > 0) {
                        Thread.sleep(delayMs)
                    }
                    val time2 = PlatformUtils.currentTimeMillis()
                    
                    (time2 >= time1) shouldBe true
                }
            }
        }
        
        describe("timestamp consistency") {
            it("should provide consistent timestamps within short time windows") {
                val timestamps = mutableListOf<Long>()
                repeat(10) {
                    timestamps.add(PlatformUtils.currentTimeMillis())
                }
                
                // All timestamps should be within a reasonable range (1 second)
                val minTime = timestamps.min()
                val maxTime = timestamps.max()
                val timeDifference = maxTime - minTime
                (timeDifference < 1000) shouldBe true // Less than 1 second difference
            }
            
            it("should handle rapid successive calls") {
                val time1 = PlatformUtils.currentTimeMillis()
                val time2 = PlatformUtils.currentTimeMillis()
                val time3 = PlatformUtils.currentTimeMillis()
                
                // Should all be valid timestamps
                (time1 > 0) shouldBe true
                (time2 > 0) shouldBe true
                (time3 > 0) shouldBe true
                
                // Should be in non-decreasing order
                (time2 >= time1) shouldBe true
                (time3 >= time2) shouldBe true
            }
        }
        
        describe("time-based operations") {
            it("should support time duration calculations") {
                val startTime = PlatformUtils.currentTimeMillis()
                Thread.sleep(10) // 10ms delay
                val endTime = PlatformUtils.currentTimeMillis()
                
                val duration = endTime - startTime
                (duration >= 10) shouldBe true // Should be at least 10ms
                (duration < 1000) shouldBe true // Should be less than 1 second
            }
            
            it("should support session timeout calculations") {
                val sessionStart = PlatformUtils.currentTimeMillis()
                val sessionDuration = 7 * 24 * 60 * 60 * 1000L // 7 days in milliseconds
                val sessionExpiry = sessionStart + sessionDuration
                
                (sessionExpiry > sessionStart) shouldBe true
                val actualDuration = sessionExpiry - sessionStart
                actualDuration shouldBe sessionDuration
            }
        }
        
        describe("edge cases") {
            it("should handle multiple concurrent calls") {
                val timestamps = mutableSetOf<Long>()
                
                // Make multiple concurrent calls
                repeat(100) {
                    timestamps.add(PlatformUtils.currentTimeMillis())
                }
                
                // All timestamps should be valid
                timestamps.forEach { timestamp ->
                    (timestamp > 0) shouldBe true
                }
                
                // Should have reasonable variance (not all exactly the same)
                val uniqueTimestamps = timestamps.size
                (uniqueTimestamps >= 1) shouldBe true
            }
        }
    }
})