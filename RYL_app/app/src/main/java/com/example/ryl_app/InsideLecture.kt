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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
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
import com.example.ryl_app.encryptFile
import com.example.ryl_app.decryptFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// InsideALectureScreen prepares the merged file for emailing, handles recording state, and displays the UI
// for recording, playback, and emailing of lecture audio.
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
    // Process module name to determine type and update module name if necessary.
    val (isL, updatedModuleName) = processModuleName(moduleName)
    val context = LocalContext.current

    // Recording state and timer initialization.
    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var recordingTimeMs by remember { mutableStateOf(0L) }

    // Manage recorded segments and their file indices.
    var recordingIndex by remember { mutableStateOf(1) }
    val recordedSegments = remember { mutableStateListOf<File>() }

    // Build folder structure for the lecture.
    val baseDirectory = File(context.filesDir, "RYL_Directory/Modules")
    val lectureDirectory = File(baseDirectory, "$updatedModuleName/week$week/$day/$name")
    if (!lectureDirectory.exists()) {
        lectureDirectory.mkdirs()
    }

    var isFileReady by remember { mutableStateOf(false) }

    // State for showing the delete confirmation dialog.
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Remove trailing time information from the lecture name.
    val cleanedName = removeafter__(name)
    // File that will store the merged recording.
    val mergedOutputFile = File(lectureDirectory, "$cleanedName.m4a")

    // Prevent the screen from turning off while recording.
    KeepScreenOn(enabled = isRecording)

    // On initial composition, check if a merged file already exists and mark it as ready.
    LaunchedEffect(Unit) {
        if (mergedOutputFile.exists()) {
            isFileReady = true
        }
    }

    // Load previously recorded segment files and update the recording index.
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

    // Returns the next file for recording a segment.
    fun getNextSegmentFile(): File {
        return File(lectureDirectory, "recording_${recordingIndex++}.m4a")
    }

    // Start recording: set up MediaRecorder, configure parameters, and begin recording.
    fun startRecording() {
        val segmentFile = getNextSegmentFile()
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(segmentFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
            start()
        }
        isRecording = true
        recordingTimeMs = 0L
    }

    // Stop recording: stop and release MediaRecorder, encrypt the segment, and merge segments.
    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        isRecording = false
        val segmentFile = File(lectureDirectory, "recording_${recordingIndex - 1}.m4a")
        // Encrypt the recorded segment after stopping.
        encryptFile(segmentFile)
        recordedSegments.add(segmentFile)

        // Merge segments asynchronously and update the file ready state.
        CoroutineScope(Dispatchers.IO).launch {
            val success = prepareMergedFileForEmail(recordedSegments, mergedOutputFile)
            withContext(Dispatchers.Main) {
                isFileReady = success
            }
        }
    }

    // Update the recording timer while recording.
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            recordingTimeMs += 1000
        }
    }

    // Format milliseconds into a mm:ss string.
    fun formatRecordingTime(ms: Long): String {
        val totalSeconds = (ms / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // UI Layout for the lecture screen.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header displaying the lecture name.
        Column(modifier = Modifier.padding(top = 60.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .border(6.dp, Color.Blue, RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cleanedName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        // Display elapsed recording time.
        Text(
            text = if (isRecording) formatRecordingTime(recordingTimeMs) else "00:00",
            fontSize = 50.sp,
            color = Color.Black,
            modifier = Modifier.padding(30.dp)
        )
        Spacer(modifier = Modifier.weight(0.5f))

        // Record button to start or stop recording.
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

        // Section for playback and emailing: includes an audio playback button and an email button.
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            AudioPlaybackButton(
                recordedSegments = recordedSegments,
                mergedOutputFile = mergedOutputFile,
                isRecording = isRecording
            )
            EmailButton(
                mergedOutputFile = mergedOutputFile,
                cleanedName = cleanedName,
                isFileReady = isFileReady
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        // Bottom row containing Back and Delete buttons.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Back button navigates back based on module type.
            Box(
                modifier = Modifier
                    .width(170.dp)
                    .height(60.dp)
                    .border(4.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(Color.Red.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { if (isL) BackToLectureBuilder() else BackToInsideADay() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back",
                    color = Color.Black,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Delete button triggers a confirmation dialog.
            Box(
                modifier = Modifier
                    .width(170.dp)
                    .height(60.dp)
                    .border(4.dp, Color.Black, RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF9800), RoundedCornerShape(12.dp))
                    .clickable { showDeleteDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Delete",
                    color = Color.Black,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Delete confirmation dialog for deleting the lecture.
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false

                        // Stop recording if it is active.
                        if (isRecording) {
                            stopRecording()
                        }

                        // Navigate back immediately.
                        if (isL) BackToLectureBuilder() else BackToInsideADay()

                        // Launch a coroutine to delete the lecture folder after a brief delay.
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(500L) // Wait for 500ms to ensure resources are released.
                            val deleted = lectureDirectory.deleteRecursively()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    if (deleted) "Lecture deleted." else "Failed to delete lecture.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                ) {
                    Text("Yes", color = Color.Red, fontSize = 18.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No", color = Color.Gray, fontSize = 18.sp)
                }
            },
            title = { Text("Delete Lecture", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this lecture?\nThis action cannot be undone.") },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.DarkGray
        )
    }
}

// AudioPlaybackButton displays a button to listen to the merged audio and opens an audio player dialog.
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

// EmailButton enables the user to send an email with the merged audio recording.
// It decrypts the merged file temporarily and launches an email intent.
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun EmailButton(
    mergedOutputFile: File,
    cleanedName: String,
    isFileReady: Boolean
) {
    val context = LocalContext.current
    // Create an ActivityResultLauncher to handle email sending.
    val emailLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // When the email app is closed, wait and then delete the temporary decrypted file.
        CoroutineScope(Dispatchers.IO).launch {
            delay(5000L)
            val tempDecryptedFile = File(mergedOutputFile.parent, "$cleanedName-.m4a")
            if (tempDecryptedFile.exists()) {
                tempDecryptedFile.delete()
            }
        }
    }

    Box(
        modifier = Modifier
            .width(180.dp)
            .height(125.dp)
            .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(
                color = if (isFileReady) Color(0xFFADD8E6) else Color.Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isFileReady) {
                // Decrypt the merged file into a temporary file for email attachment.
                val tempDecryptedFile = File(mergedOutputFile.parent, "$cleanedName-.m4a")
                decryptFile(mergedOutputFile, tempDecryptedFile)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    tempDecryptedFile
                )
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/m4a"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Combined Audio Recording")
                    putExtra(Intent.EXTRA_TEXT, "Please find the attached audio recording.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                emailLauncher.launch(Intent.createChooser(emailIntent, "Send email using:"))
            },
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

// AudioPlayerDialog provides a UI dialog for playing back the merged audio recording.
// It handles playback controls, seeking, and displays current playback time.
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
    var durationMs by remember { mutableStateOf(1f) } // default to avoid division by zero
    var isDragging by remember { mutableStateOf(false) }
    // Store a temporary dummy file for playback.
    var dummyAudioFile by remember { mutableStateOf<File?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Helper to format milliseconds into a mm:ss string.
    fun formatMs(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Prepares the audio for playback by merging recorded segments.
    fun prepareAudio(onReady: (File) -> Unit) {
        if (recordedSegments.isNotEmpty()) {
            isMerging = true
            coroutineScope.launch {
                // Decrypt each segment into temporary files.
                val decryptedSegments = recordedSegments.map { encryptedFile ->
                    File(encryptedFile.parent, "decrypted_${encryptedFile.name}").also { tempFile ->
                        decryptFile(encryptedFile, tempFile)
                    }
                }
                // Merge the decrypted segments.
                mergeSegments(decryptedSegments, mergedOutputFile)
                // Clean up temporary decrypted segment files.
                decryptedSegments.forEach { it.delete() }
                // Encrypt the merged file.
                encryptFile(mergedOutputFile)
                isMerging = false

                // For playback, decrypt the merged file into a dummy file.
                val tempDummy = File(mergedOutputFile.parent, "dummy_decrypted.m4a")
                decryptFile(mergedOutputFile, tempDummy)
                dummyAudioFile = tempDummy // Save dummy file reference for later deletion.
                onReady(tempDummy)
            }
        } else {
            Toast.makeText(context, "No recordings available!", Toast.LENGTH_SHORT).show()
        }
    }

    // Launch effect to prepare audio when the dialog is shown.
    LaunchedEffect(Unit) {
        prepareAudio { audioFile ->
            // Set up MediaPlayer with the prepared dummy audio file.
            val fis = java.io.FileInputStream(audioFile)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(fis.fd)
                prepare()
            }
            fis.close()
            durationMs = mediaPlayer?.duration?.toFloat() ?: 1f
        }
    }

    // Toggle play/pause for the audio playback.
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

    // Seek to a specific millisecond in the audio.
    fun seekToMs(ms: Int) {
        mediaPlayer?.seekTo(ms)
        currentPositionMs = ms.toFloat()
    }

    // Jump forward or backward by a specified delta in milliseconds.
    fun jump(deltaMs: Int) {
        mediaPlayer?.let { player ->
            val newMs = (player.currentPosition + deltaMs).coerceIn(0, player.duration)
            player.seekTo(newMs)
            currentPositionMs = newMs.toFloat()
        }
    }

    // Update current playback position periodically when playing.
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

    // Dialog for the audio player UI.
    Dialog(
        onDismissRequest = {
            mediaPlayer?.release()
            mediaPlayer = null
            // Delete the temporary dummy file after closing the dialog.
            dummyAudioFile?.delete()
            dummyAudioFile = null
            onDismiss()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(250.dp)
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
                // Slider to control audio playback position.
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
                // Playback control buttons: Skip previous, Play/Pause, Skip next.
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

// sendEmail prepares a temporary decrypted file and launches an email intent to send the audio.
@RequiresApi(Build.VERSION_CODES.N)
fun sendEmail(context: Context, file: File, cleanedName: String) {
    // Create a temporary file for the decrypted merged file.
    val tempDecryptedFile = File(file.parent, "$cleanedName-.m4a")
    // Decrypt the merged file into the temporary file.
    decryptFile(file, tempDecryptedFile)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        tempDecryptedFile
    )

    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        type = "audio/m4a"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Combined Audio Recording")
        putExtra(Intent.EXTRA_TEXT, "Please find the attached audio recording.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(emailIntent, "Send email using:"))

    // Schedule deletion of the temporary file after 5 seconds.
    CoroutineScope(Dispatchers.IO).launch {
        delay(5000L)
        if (tempDecryptedFile.exists()) {
            tempDecryptedFile.delete()
        }
    }
}

// mergeSegments takes a list of segment files, extracts audio samples, and writes them into a merged output file.
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
        // Find the audio track.
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
        // Read sample data from the segment file and write to the muxer.
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
        // Update time offset for the next segment.
        val durationUs = format.getLong(MediaFormat.KEY_DURATION)
        timeOffsetUs += durationUs
        extractor.release()
    }
    muxer.stop()
    muxer.release()
}

// processModuleName inspects the module name and returns a Pair indicating a flag and the cleaned module name.
fun processModuleName(moduleName: String): Pair<Boolean, String> {
    if (moduleName.isEmpty()) return Pair(false, moduleName)
    val firstChar = moduleName[0].uppercaseChar()
    return when (firstChar) {
        'L' -> Pair(true, moduleName.substring(1))
        'Z' -> Pair(false, moduleName.substring(1))
        else -> Pair(false, moduleName)
    }
}

// removeafter__ removes any trailing time range information from the input string.
fun removeafter__(input: String): String {
    // Regex matches optional whitespace, two underscores, optional whitespace, then a time range (HH:MM - HH:MM) at the end.
    val regex = Regex("""\s*_{2}\s*\d{2}:\d{2}\s*-\s*\d{2}:\d{2}$""")
    return input.replace(regex, "")
}

// prepareMergedFileForEmail is a suspend function that merges recorded segments, cleans up, and encrypts the merged file.
@RequiresApi(Build.VERSION_CODES.N)
suspend fun prepareMergedFileForEmail(
    recordedSegments: List<File>,
    mergedOutputFile: File
): Boolean {
    if (recordedSegments.isNotEmpty()) {
        // Decrypt each recorded segment into a temporary file.
        val decryptedSegments = recordedSegments.map { encryptedFile ->
            File(encryptedFile.parent, "decrypted_${encryptedFile.name}").also { tempFile ->
                decryptFile(encryptedFile, tempFile)
            }
        }
        // Merge the decrypted segments.
        mergeSegments(decryptedSegments, mergedOutputFile)
        // Delete temporary decrypted files.
        decryptedSegments.forEach { it.delete() }
        // Encrypt the merged file for secure storage.
        encryptFile(mergedOutputFile)
        return true
    }
    return false
}

// KeepScreenOn enables or disables the "keep screen on" flag for the current activity.
@Composable
fun KeepScreenOn(enabled: Boolean) {
    val context = LocalContext.current
    DisposableEffect(enabled) {
        // Cast context to Activity to access window flags.
        val activity = context as? android.app.Activity
        if (enabled) {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
