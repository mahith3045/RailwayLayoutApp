package com.mahith.desktopapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class TrackSegmentData(
    val imageName: String,
    val x: Int,
    val y: Int,
    val segmentNumber: Int,
    val rotation: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val sizeWidth: Float = 100f,
    val sizeHeight: Float = 100f
)

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Railway Control Panel") {
        RailwayControlPanel()
    }
}

@Composable
fun RailwayControlPanel() {
    var selectedSegment by remember { mutableStateOf<TrackSegmentData?>(null) }
    var trackSegments by remember { mutableStateOf(loadTracksFromFile()) }

    LaunchedEffect(trackSegments) {
        saveTracksToFile(trackSegments)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(2000.dp) // Adjust grid height for more space
            ) {
                // Draw the grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawGrid()
                }

                // Render track segments
                trackSegments.forEach { segment ->
                    TrackSegment(segment, onClick = { selectedSegment = segment })
                }

                // Display selected track details
                Text(
                    "Last Clicked Track: ${
                        selectedSegment?.let { "Segment ${it.segmentNumber} (X: ${it.x}, Y: ${it.y})" }
                            ?: "No track selected"
                    }",
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }

            // Control Panel
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                elevation = 4.dp
            ) {
                TrackControlPanel(
                    onAddTrack = { newSegment ->
                        trackSegments = trackSegments + newSegment
                        saveTracksToFile(trackSegments)
                    },
                    onDeleteTrack = {
                        selectedSegment?.let { segment ->
                            trackSegments = trackSegments.filterNot { it.segmentNumber == segment.segmentNumber }
                            saveTracksToFile(trackSegments)
                            selectedSegment = null
                        }
                    },
                    nextSegmentNumber = trackSegments.maxOfOrNull { it.segmentNumber + 1 } ?: 1
                )
            }
        }
    }
}

// Draw the grid
fun DrawScope.drawGrid() {
    val gridSize = 50f
    val color = Color.Gray

    for (x in 0 until size.width.toInt() step gridSize.toInt()) {
        drawLine(color, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
    }
    for (y in 0 until size.height.toInt() step gridSize.toInt()) {
        drawLine(color, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
    }
}

@Composable
fun TrackSegment(track: TrackSegmentData, onClick: (TrackSegmentData) -> Unit) {
    val trackImage: Painter = painterResource(track.imageName)

    Box(
        modifier = Modifier
            .absoluteOffset(x = track.x.dp, y = track.y.dp)
            .clickable { onClick(track) }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = trackImage,
                contentDescription = "Track Segment ${track.segmentNumber}",
                modifier = Modifier
                    .size(track.sizeWidth.dp, track.sizeHeight.dp)
                    .rotate(track.rotation)
                    .scale(track.scaleX, track.scaleY)
            )

        }
    }
}

@Composable
fun TrackControlPanel(
    onAddTrack: (TrackSegmentData) -> Unit,
    onDeleteTrack: () -> Unit,
    nextSegmentNumber: Int
) {
    var imageName by remember { mutableStateOf("picture77.png") }
    var x by remember { mutableStateOf("400") }
    var y by remember { mutableStateOf("150") }
    var rotation by remember { mutableStateOf("0") }
    var scaleX by remember { mutableStateOf("1") }
    var scaleY by remember { mutableStateOf("1") }

    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
        Row {
            TextField(value = imageName, onValueChange = { imageName = it }, label = { Text("Image") })
            Spacer(modifier = Modifier.width(8.dp))
            TextField(value = x, onValueChange = { x = it }, label = { Text("X") })
            Spacer(modifier = Modifier.width(8.dp))
            TextField(value = y, onValueChange = { y = it }, label = { Text("Y") })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Text("Segment: $nextSegmentNumber", style = MaterialTheme.typography.h6, color = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            TextField(value = rotation, onValueChange = { rotation = it }, label = { Text("Rotation") })
            Spacer(modifier = Modifier.width(8.dp))
            TextField(value = scaleX, onValueChange = { scaleX = it }, label = { Text("Scale X") })
            Spacer(modifier = Modifier.width(8.dp))
            TextField(value = scaleY, onValueChange = { scaleY = it }, label = { Text("Scale Y") })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                val xInt = x.toIntOrNull() ?: 0
                val yInt = y.toIntOrNull() ?: 0
                val rotationFloat = rotation.toFloatOrNull() ?: 0f
                val scaleXFloat = scaleX.toFloatOrNull() ?: 1f
                val scaleYFloat = scaleY.toFloatOrNull() ?: 1f

                val newTrack = TrackSegmentData(
                    imageName = imageName,
                    x = (xInt / 5) * 5,
                    y = (yInt / 5) * 5,
                    segmentNumber = nextSegmentNumber,
                    rotation = rotationFloat,
                    scaleX = scaleXFloat,
                    scaleY = scaleYFloat
                )
                onAddTrack(newTrack)
            }) {
                Text("Add Track")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onDeleteTrack, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)) {
                Text("Delete Track", color = Color.White)
            }
        }
    }
}

// File Handling Functions
fun loadTracksFromFile(): List<TrackSegmentData> {
    val file = File("tracks.json")
    return if (file.exists()) {
        val content = file.readText()
        Json.decodeFromString(content)
    } else {
        emptyList()
    }
}

fun saveTracksToFile(trackSegments: List<TrackSegmentData>) {
    val file = File("tracks.json")
    val jsonString = Json.encodeToString(trackSegments)
    file.writeText(jsonString)
}
