package com.example.ccagatedispatch.ui.dispatch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

data class LinePath(
    val path: List<Offset> = emptyList()
)

@Composable
fun SignatureCanvas(
    modifier: Modifier = Modifier,
    onSignatureCaptured: (String) -> Unit,
    onClear: () -> Unit
) {
    val lines = remember { mutableStateListOf<LinePath>() }
    val currentLine = remember { mutableStateListOf<Offset>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        Text(
            text = "Driver Digital Signature Canvas (Sign Below):",
            style = MaterialTheme.typography.labelLarge,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color(0xFFFAFAFA))
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentLine.clear()
                            currentLine.add(offset)
                        },
                        onDrag = { change, _ ->
                            currentLine.add(change.position)
                        },
                        onDragEnd = {
                            if (currentLine.isNotEmpty()) {
                                lines.add(LinePath(currentLine.toList()))
                                currentLine.clear()
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                lines.forEach { line ->
                    if (line.path.size > 1) {
                        val path = Path().apply {
                            moveTo(line.path.first().x, line.path.first().y)
                            for (i in 1 until line.path.size) {
                                lineTo(line.path[i].x, line.path[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(width = 4f)
                        )
                    }
                }

                if (currentLine.size > 1) {
                    val path = Path().apply {
                        moveTo(currentLine.first().x, currentLine.first().y)
                        for (i in 1 until currentLine.size) {
                            lineTo(currentLine[i].x, currentLine[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(width = 4f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    lines.clear()
                    currentLine.clear()
                    onClear()
                }
            ) {
                Text("Clear Signature")
            }

            Button(
                onClick = {
                    if (lines.isNotEmpty()) {
                        val svgString = buildSvgFromLines(lines)
                        onSignatureCaptured(svgString)
                    }
                },
                enabled = lines.isNotEmpty()
            ) {
                Text("Confirm Signature")
            }
        }
    }
}

private fun buildSvgFromLines(lines: List<LinePath>): String {
    val sb = StringBuilder()
    sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"400\" height=\"200\" viewBox=\"0 0 400 200\">")
    lines.forEach { line ->
        if (line.path.size > 1) {
            sb.append("<path d=\"M ${line.path.first().x} ${line.path.first().y}")
            for (i in 1 until line.path.size) {
                sb.append(" L ${line.path[i].x} ${line.path[i].y}")
            }
            sb.append("\" stroke=\"black\" stroke-width=\"2\" fill=\"none\"/>")
        }
    }
    sb.append("</svg>")
    return sb.toString()
}
