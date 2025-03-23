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


// InsideALectureScreen composable that prepares the merged file and updates the state flag.
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
    val (isL, updatedModuleName) = processModuleName(moduleName)
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
    val lectureDirectory = File(baseDirectory, "$updatedModuleName/week$week/$day/$name")
    if (!lectureDirectory.exists()) {
        lectureDirectory.mkdirs()
    }

    var isFileReady by remember { mutableStateOf(false) }


    // Delete dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }

    val cleanedName = removeafter__(name)
    // File for merged recording.
    val mergedOutputFile = File(lectureDirectory, "$cleanedName.m4a")

    KeepScreenOn(enabled = isRecording)



    // On initial composition, if a merged file already exists, update the flag.
    LaunchedEffect(Unit) {
        if (mergedOutputFile.exists()) {
            // If a merged file exists, we consider it ready for emailing.
            // (You might also want to check that it's valid.)
            // This enables the EmailButton on first entry.
            isFileReady = true
        }
    }

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
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(segmentFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
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
        // Encrypt the recorded segment after recording is complete.
        encryptFile(segmentFile)
        recordedSegments.add(segmentFile)

        // Immediately trigger merging/updating of the merged file.
        CoroutineScope(Dispatchers.IO).launch {
            val success = prepareMergedFileForEmail(recordedSegments, mergedOutputFile)
            withContext(Dispatchers.Main) {
                isFileReady = success
            }
        }
    }

    // Keep track of recording time.
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

    // State flag to indicate if the merged file is ready.
    //var isFileReady by remember { mutableStateOf(false) }
    // (The LaunchedEffect above already sets this if a merged file exists or after stopping recording)

    // UI – (Your UI layout remains unchanged)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
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
        // Elapsed time
        Text(
            text = if (isRecording) formatRecordingTime(recordingTimeMs) else "00:00",
            fontSize = 50.sp,
            color = Color.Black,
            modifier = Modifier.padding(30.dp)
        )
        Spacer(modifier = Modifier.weight(0.5f))

        // Record Button
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

        // Listen + Email buttons
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

        // Bottom row: Back & Delete
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Back Button
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

            // Delete Button
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

    // Delete confirmation dialog (unchanged)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false

                        // Stop recording if active
                        if (isRecording) {
                            stopRecording()
                        }

                        // Navigate back immediately
                        if (isL) BackToLectureBuilder() else BackToInsideADay()

                        // Then, in a coroutine, wait a moment and delete the folder
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(500L) // wait for 500ms to ensure all resources are released
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

// EmailButton composable that uses the state flag to enable/disable itself.
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun EmailButton(
    mergedOutputFile: File,
    cleanedName: String,
    isFileReady: Boolean
) {
    val context = LocalContext.current
    // Create an ActivityResultLauncher to listen for when the email intent finishes.
    val emailLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // When the email app is closed, wait briefly and then delete the temporary decrypted file.
        CoroutineScope(Dispatchers.IO).launch {
            delay(500L) // Adjust delay as needed
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
                // Decrypt the merged file into a temporary file.
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
    // State variable to hold our temporary dummy file for playback.
    var dummyAudioFile by remember { mutableStateOf<File?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }


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
                // Decrypt each recorded segment into a temporary file.
                val decryptedSegments = recordedSegments.map { encryptedFile ->
                    File(encryptedFile.parent, "decrypted_${encryptedFile.name}").also { tempFile ->
                        decryptFile(encryptedFile, tempFile)
                    }
                }
                // Merge the decrypted segments into the mergedOutputFile.
                mergeSegments(decryptedSegments, mergedOutputFile)
                // Optionally, delete the temporary decrypted segment files.
                decryptedSegments.forEach { it.delete() }
                // Now encrypt the merged file.
                encryptFile(mergedOutputFile)
                isMerging = false

                // For playback, decrypt the encrypted merged file into a temporary dummy file.
                val tempDummy = File(mergedOutputFile.parent, "dummy_decrypted.m4a")
                decryptFile(mergedOutputFile, tempDummy)
                dummyAudioFile = tempDummy  // store the dummy file so we can delete it later
                onReady(tempDummy)
            }
        } else {
            Toast.makeText(context, "No recordings available!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        prepareAudio { audioFile ->
            // Use the temporary dummy file as the data source.
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
            // Delete the temporary dummy file after playback.
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
fun sendEmail(context: Context, file: File, cleanedName: String) {
    // Create a temporary file for the decrypted merged file using cleanedName.
    val tempDecryptedFile = File(file.parent, "$cleanedName-.m4a")
    // Decrypt the encrypted merged file into the temporary file.
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

    // Schedule deletion of the temporary file after a shorter delay (5 seconds)
    CoroutineScope(Dispatchers.IO).launch {
        delay(5000L) // 5 seconds delay
        if (tempDecryptedFile.exists()) {
            tempDecryptedFile.delete()
        }
    }
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

fun removeafter__(input: String): String {
    // This regex matches optional whitespace,
    // then exactly two underscores, optional whitespace,
    // then a time range in the format HH:MM - HH:MM at the end of the string.
    val regex = Regex("""\s*_{2}\s*\d{2}:\d{2}\s*-\s*\d{2}:\d{2}$""")
    return input.replace(regex, "")
}


// Suspend function to prepare the merged file (merging, cleaning up, and encryption)
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
        // Merge the decrypted segments into mergedOutputFile.
        mergeSegments(decryptedSegments, mergedOutputFile)
        // Delete the temporary decrypted segment files.
        decryptedSegments.forEach { it.delete() }
        // Encrypt the merged file so it is stored securely.
        encryptFile(mergedOutputFile)
        return true
    }
    return false
}


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
