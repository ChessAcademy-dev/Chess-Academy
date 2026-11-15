package com.chessacademy.feature.play

import android.content.Context
import com.chessacademy.chess.engine.AnalysisResult
import com.chessacademy.chess.engine.UciEngine
import com.chessacademy.chess.rules.Color
import com.chessacademy.core.database.dao.GameDao
import com.chessacademy.core.database.entity.GameEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class PlayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var gameDao: GameDao
    private lateinit var uciEngine: UciEngine

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mock(Context::class.java)
        gameDao = mock(GameDao::class.java)
        uciEngine = mock(UciEngine::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startNewGame_initializesStateCorrectly() = runTest {
        // Act
        val viewModel = PlayViewModel(context, gameDao, uciEngine)
        viewModel.startNewGame(Color.WHITE, EngineDifficulty.EASY, TimeControl.BULLET_1_0)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isConfiguring)
        assertEquals(Color.WHITE, state.playerColor)
        assertEquals(EngineDifficulty.EASY, state.engineDifficulty)
        assertEquals(TimeControl.BULLET_1_0, state.timeControl)
        assertEquals(0, state.moveHistory.size)
        assertFalse(state.isGameOver)
    }

    @Test
    fun playerMove_updatesBoardAndTriggersEngine() = runTest {
        // Arrange
        `when`(uciEngine.analyzePosition(anyString(), anyLong())).thenReturn(
            AnalysisResult(bestMove = "e7e5", scoreCp = 20)
        )

        val viewModel = PlayViewModel(context, gameDao, uciEngine)
        viewModel.startNewGame(Color.WHITE, EngineDifficulty.MEDIUM, TimeControl.UNLIMITED)
        testScheduler.advanceUntilIdle()

        // Act: e2e4 ausführen (Weiß)
        val fromSquare = com.chessacademy.chess.rules.Square.fromAlgebraic("e2")!!
        val toSquare = com.chessacademy.chess.rules.Square.fromAlgebraic("e4")!!
        
        viewModel.onSquareSelected(fromSquare)
        viewModel.onSquareSelected(toSquare)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(2, state.moveHistory.size) // Spielerzug + Engine-Antwort (e7e5)
    }

    @Test
    fun engineError_handlesGracefullyWithoutCrash() = runTest {
        // Arrange: Engine wirft Exception
        `when`(uciEngine.analyzePosition(anyString(), anyLong())).thenThrow(RuntimeException("Engine crash"))

        val viewModel = PlayViewModel(context, gameDao, uciEngine)
        viewModel.startNewGame(Color.WHITE, EngineDifficulty.MEDIUM, TimeControl.UNLIMITED)
        testScheduler.advanceUntilIdle()

        val fromSquare = com.chessacademy.chess.rules.Square.fromAlgebraic("e2")!!
        val toSquare = com.chessacademy.chess.rules.Square.fromAlgebraic("e4")!!
        
        // Act
        viewModel.onSquareSelected(fromSquare)
        viewModel.onSquareSelected(toSquare)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        // Spiel sollte wegen des Engine-Fehlers ordnungsgemäß abgefangen werden
        assertNotNull(state.resultMessage)
    }

    @Test
    fun resignGame_savesCompletedGameToDatabase() = runTest {
        // Arrange
        val viewModel = PlayViewModel(context, gameDao, uciEngine)
        viewModel.startNewGame(Color.WHITE, EngineDifficulty.EASY, TimeControl.UNLIMITED)
        testScheduler.advanceUntilIdle()

        // Act
        viewModel.resignGame()
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.isGameOver)
        verify(gameDao, times(1)).insertGame(any(GameEntity::class.java))
    }
}
