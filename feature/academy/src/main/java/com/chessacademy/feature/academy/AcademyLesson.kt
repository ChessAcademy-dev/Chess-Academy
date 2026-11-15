package com.chessacademy.feature.academy

enum class LessonCategory(val titleDe: String) {
    OPENINGS("Eröffnungen"),
    TACTICS("Taktikmuster"),
    MIDDLEGAME("Mittelspiel"),
    ENDGAME("Endspiele")
}

data class LessonStep(
    val fen: String,
    val instruction: String,
    val expectedMoveFrom: String, // z.B. "e2"
    val expectedMoveTo: String,   // z.B. "e4"
    val explanationOnSuccess: String
)

data class AcademyLesson(
    val id: String,
    val title: String,
    val category: LessonCategory,
    val description: String,
    val steps: List<LessonStep>
)
