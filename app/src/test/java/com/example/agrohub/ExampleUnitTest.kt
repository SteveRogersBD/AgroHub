package com.example.agrohub

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Example unit test using Kotest to verify the testing framework is properly configured.
 *
 * See [Kotest documentation](https://kotest.io/)
 */
class ExampleUnitTest : FunSpec({
    test("addition should be correct") {
        val result = 2 + 2
        result shouldBe 4
    }
})