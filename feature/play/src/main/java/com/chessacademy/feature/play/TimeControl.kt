package com.chessacademy.feature.play

enum class TimeControl(val title: String, val initialMillis: Long, val incrementMillis: Long) {
    BULLET_1_0("Bullet (1+0)", 60 * 1000L, 0L),
    BLITZ_3_2("Blitz (3+2)", 3 * 60 * 1000L, 2 * 1000L),
    RAPID_10_0("Rapid (10+0)", 10 * 60 * 1000L, 0L),
    CLASSICAL_30_0("Klassisch (30+0)", 30 * 60 * 1000L, 0L),
    UNLIMITED("Unbegrenzt", Long.MAX_VALUE, 0L)
}
