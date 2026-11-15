package com.chessacademy.chess.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Wiederverwendbare, responsive Schachbrett-Komponente für Jetpack Compose.
 * Trennt Darstellung und Schachlogik strikt voneinander.
 */
@Composable
fun ChessBoard(
    modifier: Modifier = Modifier,
    viewState: BoardViewState,
    lightSquareColor: Color = Color(0xFFF0D9B5),
    darkSquareColor: Color = Color(0xFFB58863),
    highlightColor: Color = Color(0x66BBC938),
    lastMoveColor: Color = Color(0x55CDD26B),
    onSquareClick: (Int) -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0..7) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for (col in 0..7) {
                        val actualRow = if (viewState.isFlipped) 7 - row else row
                        val actualCol = if (viewState.isFlipped) 7 - col else col
                        val squareIndex = actualRow * 8 + actualCol

                        val isLight = (actualRow + actualCol) % 2 == 0
                        val baseColor = if (isLight) lightSquareColor else darkSquareColor

                        val isSelected = viewState.selectedSquare == squareIndex
                        val isLegalMove = viewState.legalMoves.contains(squareIndex)
                        val isLastMove = squareIndex == viewState.lastMoveFrom || squareIndex == viewState.lastMoveTo

                        val squareColor = when {
                            isSelected -> highlightColor
                            isLastMove -> lastMoveColor
                            else -> baseColor
                        }

                        val piece = viewState.pieces[squareIndex]
                        val fileChar = 'a' + actualCol
                        val rankChar = '8' - actualRow
                        val squareName = "$fileChar$rankChar"

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(squareColor)
                                .clickable { onSquareClick(squareIndex) }
                                .semantics {
                                    contentDescription = "Feld $squareName" + if (piece != null) ", ${piece.color} ${piece.type}" else ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Markierung für legale Züge (Punkt in der Mitte)
                            if (isLegalMove && piece == null) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0x66000000), CircleShape)
                                )
                            }

                            // Figur anzeigen
                            if (piece != null) {
                                ChessPieceView(piece = piece)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChessPieceView(piece: ChessPieceModel) {
    val textColor = if (piece.color == PieceColor.WHITE) Color.White else Color.Black
    val strokeColor = if (piece.color == PieceColor.WHITE) Color.Black else Color.White

    // Textbasierte Darstellung als Fallback, bis die Figurengrafiken/SVGs eingebunden sind
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = piece.symbol,
            color = textColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
