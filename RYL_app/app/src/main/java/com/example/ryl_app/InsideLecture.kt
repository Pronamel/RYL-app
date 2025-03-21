package com.example.ryl_app

import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

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
    val (isL, updatedModuleName) = processModuleName(moduleName)
    println("isL: $isL, updatedModuleName: $updatedModuleName")
    val context = LocalContext.current

    // Recording states.
    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var recordingTimeMs by remember { mutableStateOf(0L) }

    // Segment file management.
    var recordingIndex by remember { mutableStateOf(1) }
    val recordedSegments = remember { mutableStateListOf<File>() }

    // Build folder structure.
    val baseDirectory = File(context.filesDir, "RYL_Directory/Modules")
    println(baseDirectory)
    val lectureDirectory = File(baseDirectory, "$updatedModuleName/week$week/$day/$name")
    if (!lectureDirectory.exists()) {
        lectureDirectory.mkdirs()
    }
    println(lectureDirectory)

    // File for merged recording.
    val mergedOutputFile = File(lectureDirectory, "merged_recording.m4a")

    // Load existing segment files.
    LaunchedEffect(lectureDirectory) {
        val segments = lectureDirectory.listFiles { file ->
            file.name.startsWith("recording_") && file.name.endsWith(".m4a")
        }?.toList() ?: emptyList()
        recordedSegments.clear()
        recordedSegments.addAll(segments)
        if (segments.isNotEmpty()) {
            val maxIndex = segments.mapNotNull { file ->
                file.name.removePrefix("recording_").removeSuffix(".m4a").toIntOrNull()
            }.maxOrNull() ?: 0
            recordingIndex = maxIndex + 1
        }
    }

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
        recordingTimeMs = 0L
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

    // Update elapsed time.
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            recordingTimeMs += 1000
        }
    }

    fun formatRecordingTime(ms: Long): String {
        val totalSeconds = (ms / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header.
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
        // Elapsed time.
        Text(
            text = if (isRecording) formatRecordingTime(recordingTimeMs) else "00:00",
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
                    .clickable { if (isRecording) stopRecording() else startRecording() },
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
        // Vertical stack for Listen and Email buttons.
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AudioPlaybackButton(
                recordedSegments = recordedSegments,
                mergedOutputFile = mergedOutputFile,
                isRecording = isRecording
            )
            EmailButton(mergedOutputFile = mergedOutputFile)
        }
        Spacer(modifier = Modifier.weight(1f))
        // Days Button.
        Column(modifier = Modifier.padding(bottom = 30.dp)) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(90.dp)
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(Color(0xFF800080), RoundedCornerShape(12.dp))
                    .clickable { if (isL) BackToLectureBuilder() else BackToInsideADay() },
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
fun EmailButton(mergedOutputFile: File) {
    val context = LocalContext.current
    // Styled like the other buttons.
    Box(
        modifier = Modifier
            .width(180.dp)
            .height(125.dp)
            .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color(0xFFADD8E6), RoundedCornerShape(12.dp))
            .clickable { sendEmail(context, mergedOutputFile) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Email",
            color = Color.Black,
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold
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
    var isDragging by remember { mutableStateOf(false) }

    fun formatMs(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

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

    fun seekToMs(ms: Int) {
        mediaPlayer?.seekTo(ms)
        currentPositionMs = ms.toFloat()
    }

    fun jump(deltaMs: Int) {
        mediaPlayer?.let { player ->
            val newMs = (player.currentPosition + deltaMs).coerceIn(0, player.duration)
            player.seekTo(newMs)
            currentPositionMs = newMs.toFloat()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (!isDragging) {
                mediaPlayer?.let { player ->
                    currentPositionMs = player.currentPosition.toFloat()
                }
            }
            delay(300)
        }
    }

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
                .height(350.dp)
                .background(Color.DarkGray.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        isDragging = true
                    },
                    onValueChangeFinished = {
                        seekToMs(currentPositionMs.toInt())
                        isDragging = false
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
fun sendEmail(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        type = "audio/m4a"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Combined Audio Recording")
        putExtra(Intent.EXTRA_TEXT, "Please find the attached audio recording.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(emailIntent, "Send email using:"))
}

@RequiresApi(Build.VERSION_CODES.N)
fun mergeSegments(segmentFiles: List<File>, outputFile: File) {
    val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var audioTrackIndex = -1
    var timeOffsetUs = 0L
    val buffer = ByteBuffer.allocate(1024 * 1024)
    val bufferInfo = MediaCodec.BufferInfo()

    segmentFiles.forEach { file ->
        val extractor = MediaExtractor()
        extractor.setDataSource(file.absolutePath)
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
        if (audioTrackIndex == -1) {
            audioTrackIndex = muxer.addTrack(format)
            muxer.start()
        }
        while (true) {
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break
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
