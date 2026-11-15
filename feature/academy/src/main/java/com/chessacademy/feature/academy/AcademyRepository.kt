package com.chessacademy.feature.academy

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AcademyRepository @Inject constructor() {

    fun getLessons(): List<AcademyLesson> {
        return listOf(
            AcademyLesson(
                id = "op_1",
                title = "Das Zentrum besetzen (e4)",
                category = LessonCategory.OPENINGS,
                description = "Lerne, wie du mit dem Bauern auf e4 das Zentrum kontrollierst.",
                steps = listOf(
                    LessonStep(
                        fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        instruction = "Ziehe den e-Baurern zwei Felder nach vorne (e2 nach e4).",
                        expectedMoveFrom = "e2",
                        expectedMoveTo = "e4",
                        explanationOnSuccess = "Ausgezeichnet! e4 besetzt das Zentrum und öffnet Linien für Läufer und Dame."
                    )
                )
            ),
            AcademyLesson(
                id = "tac_1",
                title = "Einfache Gabel (Schach mit Springer)",
                category = LessonCategory.TACTICS,
                description = "Nutze einen Springer, um König und Dame gleichzeitig anzugreifen.",
                steps = listOf(
                    LessonStep(
                        fen = "r1bqkb1r/pppp1ppp/2n5/4p3/2B1n3/5N2/PPPP1PPP/RNBQ1RK1 w kq - 0 5",
                        instruction = "Finde den starken Springerausfall nach e5!",
                        expectedMoveFrom = "f3",
                        expectedMoveTo = "e5",
                        explanationOnSuccess = "Stark! Der Springer greift den e7-Bauern und das Zentrum an."
                    )
                )
            ),
            AcademyLesson(
                id = "end_1",
                title = "Quadratregel im Bauernendspiel",
                category = LessonCategory.ENDGAME,
                description = "Verhindere die gegnerische Bauernumwandlung mit der geometrischen Quadratregel.",
                steps = listOf(
                    LessonStep(
                        fen = "8/4k3/8/4P3/8/4K3/8/8 w - - 0 1",
                        instruction = "Bringe den König in Richtung des Freibauern auf e5 (e3 nach e4).",
                        expectedMoveFrom = "e3",
                        expectedMoveTo = "e4",
                        explanationOnSuccess = "Perfekt! Der König betritt das Kontrollquadrat."
                    )
                )
            )
        )
    }
}
