package org.cubewhy.celestial

import com.lunarclient.common.v1.LunarclientCommonV1.Color
import org.cubewhy.celestial.entity.PlusColor
import kotlin.test.Test

class TestLunarPlusColor {
    @Test
    fun testColor(){
        println(PlusColor.AQUA.color)
    }

    @Test
    fun testColorProto(){
        println("proto color value ${Color.newBuilder().setColor(100).build()}")
    }
}