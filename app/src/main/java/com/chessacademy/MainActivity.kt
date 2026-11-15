package com.chessacademy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.chessacademy.core.datastore.PreferencesRepository
import com.chessacademy.core.designsystem.theme.ChessAcademyTheme
import com.chessacademy.navigation.ChessAcademyNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessAcademyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ChessAcademyNavGraph(
                        preferencesRepository = preferencesRepository
                    )
                }
            }
        }
    }
}
