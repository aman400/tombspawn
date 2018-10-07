package com.ramukaka

import com.ramukaka.extensions.toMap
import org.junit.Test
import kotlin.test.assertEquals

class UtilsTest {
    @Test
    fun testStringMapper() {
        val mapped = "abc = def aaa = ghi = rr".toMap()
        mapped!!.forEach { t, u -> println("$t, $u}") }
        assertEquals(null, mapped["aaa"])
    }
}