package com.example.ryl_app

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.MediaFormat
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious


@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun InsideALectureScreen(
    day: String,
    week: Int,
    name: String,
    moduleName: String,
    BackToLectureBuilder: () -> Unit,
    BackToInsideADay: () -> Unit,
) {
    println("InsideALectureScreen Week received: $week")

    // Process the passed-in moduleName.
    val (isL, updatedModuleName) = processModuleName(moduleName)
    println("isL: $isL, updatedModuleName: $updatedModuleName")

    val context = LocalContext.current

    // States for recording.
    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }

    // A counter and list for segment files.
    var recordingIndex by remember { mutableStateOf(1) }
    val recordedSegments = remember { mutableStateListOf<File>() }

    // Build your custom folder structure.
    val baseDirectory = File(context.filesDir, "RYL_Directory/Modules")
    println(baseDirectory)
    // Directory structure: /<updatedModuleName>/week<week>/<day>/<name>
    val lectureDirectory = File(baseDirectory, "$updatedModuleName/week$week/$day/$name")
    if (!lectureDirectory.exists()) {
        lectureDirectory.mkdirs()
    }
    println(lectureDirectory)

    // The file for the merged recording.
    val mergedOutputFile = File(lectureDirectory, "merged_recording.m4a")

    // Load any existing segment files from disk into recordedSegments.
    LaunchedEffect(lectureDirectory) {
        val segments = lectureDirectory.listFiles { file ->
            file.name.startsWith("recording_") && file.name.endsWith(".m4a")
        }?.toList() ?: emptyList()
        recordedSegments.clear()
        recordedSegments.addAll(segments)
        // Update recordingIndex to one more than the max found.
        if (segments.isNotEmpty()) {
            val maxIndex = segments.mapNotNull { file ->
                file.name.removePrefix("recording_").removeSuffix(".m4a").toIntOrNull()
            }.maxOrNull() ?: 0
            recordingIndex = maxIndex + 1
        }
    }

    // Returns a new segment file.
    fun getNextSegmentFile(): File {
        return File(lectureDirectory, "recording_${recordingIndex++}.m4a")
    }

    fun startRecording() {
        val segmentFile = getNextSegmentFile()
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // MP4/M4A container
            setOutputFile(segmentFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)    // AAC encoding
            prepare()
            start()
        }
        isRecording = true
    }

    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        isRecording = false
        val segmentFile = File(lectureDirectory, "recording_${recordingIndex - 1}.m4a")
        recordedSegments.add(segmentFile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Lecture header.
        Column(modifier = Modifier.padding(top = 60.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .border(6.dp, Color.Blue, RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$name $day $week $updatedModuleName",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        Text(
            text = if (isRecording) "Recording..." else "00:00",
            fontSize = 50.sp,
            color = Color.Black,
            modifier = Modifier.padding(30.dp)
        )
        Spacer(modifier = Modifier.weight(0.5f))
        // Record Button.
        Column(modifier = Modifier.padding(bottom = 25.dp)) {
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(125.dp)
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(if (isRecording) Color.Gray else Color.Red, RoundedCornerShape(12.dp))
                    .clickable {
                        if (isRecording) stopRecording() else startRecording()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRecording) "Stop" else "Record",
                    color = Color.Black,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Playback Button becomes a popup dialog.
        AudioPlaybackButton(
            recordedSegments = recordedSegments,
            mergedOutputFile = mergedOutputFile,
            isRecording = isRecording
        )
        Spacer(modifier = Modifier.weight(1f))
        // Days Button.
        Column(modifier = Modifier.padding(bottom = 30.dp)) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(90.dp)
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(Color(0xFF800080), RoundedCornerShape(12.dp))
                    .clickable {
                        if (isL) {
                            BackToLectureBuilder()
                        } else {
                            BackToInsideADay()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Days",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun AudioPlaybackButton(
    recordedSegments: List<File>,
    mergedOutputFile: File,
    isRecording: Boolean
) {
    val context = LocalContext.current
    var showPlayerDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(180.dp)
            .height(125.dp)
            .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color.Green, RoundedCornerShape(12.dp))
            .clickable(enabled = !isRecording) {
                if (recordedSegments.isEmpty()) {
                    Toast.makeText(context, "No recordings available!", Toast.LENGTH_SHORT).show()
                } else {
                    showPlayerDialog = true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Listen",
            color = Color.Black,
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold
        )
    }

    if (showPlayerDialog) {
        AudioPlayerDialog(
            recordedSegments = recordedSegments,
            mergedOutputFile = mergedOutputFile,
            onDismiss = { showPlayerDialog = false }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun AudioPlayerDialog(
    recordedSegments: List<File>,
    mergedOutputFile: File,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var isMerging by remember { mutableStateOf(false) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var currentPositionMs by remember { mutableStateOf(0f) }
    var durationMs by remember { mutableStateOf(1f) } // default to avoid divide-by-zero

    // Format milliseconds as "mm:ss"
    fun formatMs(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Helper to merge segments if necessary and then prepare the MediaPlayer.
    fun prepareAudio(onReady: (File) -> Unit) {
        if (recordedSegments.isNotEmpty()) {
            isMerging = true
            coroutineScope.launch {
                mergeSegments(recordedSegments, mergedOutputFile)
                isMerging = false
                onReady(mergedOutputFile)
            }
        } else {
            Toast.makeText(context, "No recordings available!", Toast.LENGTH_SHORT).show()
        }
    }

    // Prepare the MediaPlayer when the dialog appears.
    LaunchedEffect(Unit) {
        prepareAudio { audioFile ->
            val fis = java.io.FileInputStream(audioFile)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(fis.fd)
                prepare()
            }
            fis.close()
            durationMs = mediaPlayer?.duration?.toFloat() ?: 1f
        }
    }

    // Function to toggle play/pause.
    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.pause()
                isPlaying = false
            } else {
                player.start()
                isPlaying = true
            }
        }
    }

    // Seek to a new position.
    fun seekToMs(ms: Int) {
        mediaPlayer?.seekTo(ms)
        currentPositionMs = ms.toFloat()
    }

    // Jump ±30 seconds.
    fun jump(deltaMs: Int) {
        mediaPlayer?.let { player ->
            val newMs = (player.currentPosition + deltaMs).coerceIn(0, player.duration)
            player.seekTo(newMs)
            currentPositionMs = newMs.toFloat()
        }
    }

    // Update slider while playing.
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { player ->
                currentPositionMs = player.currentPosition.toFloat()
            }
            delay(300)
        }
    }

    // Use a full-screen Dialog with controlled width and height.
    Dialog(
        onDismissRequest = {
            mediaPlayer?.release()
            mediaPlayer = null
            onDismiss()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(300.dp)
                .background(Color.DarkGray.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Audio Player",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Position: ${formatMs(currentPositionMs.toInt())} / ${formatMs(durationMs.toInt())}",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Slider(
                    value = currentPositionMs,
                    onValueChange = { newValue ->
                        currentPositionMs = newValue
                    },
                    onValueChangeFinished = {
                        seekToMs(currentPositionMs.toInt())
                    },
                    valueRange = 0f..durationMs,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp),
                        onClick = { jump(-30000) }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Skip Back 30s",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { togglePlayPause() }
                    ) {
                        if (isPlaying) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.Pause,
                                contentDescription = "Pause",
                                tint = Color.White
                            )
                        } else {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 5.dp),
                        onClick = { jump(30000) }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Skip Forward 30s",
                            tint = Color.White
                        )
                    }
                }
                // New text label underneath the buttons.
                Text(
                    text = "Each skip is worth 30s",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.N)
fun mergeSegments(segmentFiles: List<File>, outputFile: File) {
    val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var audioTrackIndex = -1
    var timeOffsetUs = 0L
    val buffer = ByteBuffer.allocate(1024 * 1024) // 1MB buffer
    val bufferInfo = MediaCodec.BufferInfo()

    segmentFiles.forEach { file ->
        val extractor = MediaExtractor()
        extractor.setDataSource(file.absolutePath)

        // Find the first audio track.
        var trackIndex = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                trackIndex = i
                break
            }
        }
        if (trackIndex == -1) {
            extractor.release()
            return@forEach
        }
        extractor.selectTrack(trackIndex)
        val format = extractor.getTrackFormat(trackIndex)

        // On the first file, add the track to the muxer and start it.
        if (audioTrackIndex == -1) {
            audioTrackIndex = muxer.addTrack(format)
            muxer.start()
        }

        // Read samples from the extractor and write them into the muxer.
        while (true) {
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break  // End of stream.
            bufferInfo.size = sampleSize
            bufferInfo.offset = 0
            bufferInfo.presentationTimeUs = extractor.sampleTime + timeOffsetUs
            bufferInfo.flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                extractor.sampleFlags
            } else {
                0
            }
            muxer.writeSampleData(audioTrackIndex, buffer, bufferInfo)
            extractor.advance()
        }
        // Increase time offset by this segment's duration.
        val durationUs = format.getLong(MediaFormat.KEY_DURATION)
        timeOffsetUs += durationUs
        extractor.release()
    }
    muxer.stop()
    muxer.release()
}

fun processModuleName(moduleName: String): Pair<Boolean, String> {
    if (moduleName.isEmpty()) return Pair(false, moduleName)
    val firstChar = moduleName[0].uppercaseChar()
    return when (firstChar) {
        'L' -> Pair(true, moduleName.substring(1))
        'Z' -> Pair(false, moduleName.substring(1))
        else -> Pair(false, moduleName)
    }
}
