package com.example.ryl_app

import android.content.Context
import java.io.File
import androidx.compose.ui.graphics.Color

// Creates the main "RYL_Directory" in the app's private storage and ensures that a "Modules" folder exists within it.
fun createRYLDirectory(context: Context): File {
    val rootDir = context.filesDir  // Get the app's internal storage directory.
    val rylDirectory = File(rootDir, "RYL_Directory")
    if (!rylDirectory.exists()) {
        rylDirectory.mkdirs()  // Create "RYL_Directory" if it does not exist.
    }
    // Ensure the "Modules" subfolder is present.
    val modulesDirectory = File(rylDirectory, "Modules")
    if (!modulesDirectory.exists()) {
        modulesDirectory.mkdirs()  // Create "Modules" folder if it does not exist.
    }
    return rylDirectory
}

// Creates a module folder with its info file and subfolders for weeks and days.
// This function creates a folder for the module, writes the module info to "module_info.txt",
// and then creates a folder for each week with subfolders for each day.
fun createWeeklyFolders(mainDirectory: String, name: String, numberOfWeeks: Int, textColor: Color) {
    // Create the main module folder.
    val mainFolder = File(mainDirectory, name)
    if (!mainFolder.exists()) {
        mainFolder.mkdir()  // Create the folder if it does not exist.
        println("Main folder '$name' created in '$mainDirectory'")
    } else {
        println("Main folder '$name' already exists in '$mainDirectory'")
    }

    // Write module metadata (text color and weeks) into "module_info.txt".
    val infoFile = File(mainFolder, "module_info.txt")
    val infoContent = """
        Text Color: $textColor
        Weeks: $numberOfWeeks
    """.trimIndent()
    infoFile.writeText(infoContent)
    println("Created module_info.txt with color and weeks data.")

    // Create subfolders for each week and its days.
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    for (weekNum in 1..numberOfWeeks) {
        val weekFolderName = "week$weekNum"
        val weekFolder = File(mainFolder, weekFolderName)
        if (!weekFolder.exists()) {
            weekFolder.mkdir()  // Create the week folder.
            println("Created folder: $weekFolder")
        }
        // Create a subfolder for each day of the week.
        for (day in daysOfWeek) {
            val dayFolder = File(weekFolder, day)
            if (!dayFolder.exists()) {
                dayFolder.mkdir()  // Create the day folder.
                println("Created folder: $dayFolder")
            }
        }
    }
}

// Checks if a module exists in the "Modules" directory and creates it if not.
// Returns true if the module was created; false if it already exists.
fun modulemaker(name: String, numberOfWeeks: Int, textColor: androidx.compose.ui.graphics.Color): Boolean {
    val mainDirectory = "/data/data/com.example.ryl_app/files/RYL_Directory/Modules"
    val modulePath = File(mainDirectory, name)

    // Check for an existing module folder.
    if (modulePath.exists()) {
        println("Module '$name' already exists in Modules directory.")
        return false
    }

    // Create the module folder structure if it does not exist.
    createWeeklyFolders(mainDirectory, name, numberOfWeeks, textColor)
    println("Module '$name' created successfully")
    return true
}

// Reads module data from each module's info file and returns arrays of module paths, text colors, and week counts.
fun getModuleData(): Triple<Array<String>, Array<Color>, Array<Int>> {
    val modulesDirectory = File("/data/data/com.example.ryl_app/files/RYL_Directory/Modules")
    if (!modulesDirectory.exists() || !modulesDirectory.isDirectory) {
        println("Modules directory does not exist.")
        return Triple(emptyArray(), emptyArray(), emptyArray())
    }

    val modulePaths = mutableListOf<String>()
    val moduleColors = mutableListOf<Color>()
    val moduleNumbers = mutableListOf<Int>()

    // Iterate over each module folder.
    modulesDirectory.listFiles { file -> file.isDirectory }?.forEach { moduleFolder ->
        modulePaths.add(moduleFolder.absolutePath)

        // Read metadata from "module_info.txt".
        val infoFile = File(moduleFolder, "module_info.txt")
        if (infoFile.exists()) {
            val lines = infoFile.readLines()
            var textColor = Color.Black  // Default color.
            var number = 0               // Default weeks count.
            lines.forEach { line ->
                when {
                    line.startsWith("Text Color:") -> {
                        val colorString = line.removePrefix("Text Color:").trim()
                        textColor = parseComposeColor(colorString)
                    }
                    line.startsWith("Weeks:") -> {
                        val numberString = line.removePrefix("Weeks:").trim()
                        number = numberString.toIntOrNull() ?: 0
                    }
                }
            }
            moduleColors.add(textColor)
            moduleNumbers.add(number)
        } else {
            moduleColors.add(Color.Black)
            moduleNumbers.add(0)
        }
    }

    return Triple(modulePaths.toTypedArray(), moduleColors.toTypedArray(), moduleNumbers.toTypedArray())
}

// Parses a string representation of a Compose Color into a Color object.
// Expected format is similar to "Color(r, g, b, a, ...)".
fun parseComposeColor(colorString: String): Color {
    return try {
        val regex = Regex("""Color\(([\d.]+), ([\d.]+), ([\d.]+), ([\d.]+), .*?\)""")
        val matchResult = regex.find(colorString)
        if (matchResult != null) {
            val (r, g, b, a) = matchResult.destructured
            Color(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
        } else {
            println("Invalid color format: $colorString")
            Color.Black
        }
    } catch (e: Exception) {
        println("Error parsing color: $e")
        Color.Black
    }
}

// Searches for a module folder based on a query, navigates to a specified week and day,
// and creates a lecture folder with the format "lectureName__time".
// Returns the absolute path of the day folder if successful, or null otherwise.
fun findModuleWeekDayLectureAndCreateFolder(
    moduleNameQuery: String,
    weekNumber: String,
    dayName: String,
    lectureName: String,
    time: String
): String? {
    val modulesDirectory = File("/data/data/com.example.ryl_app/files/RYL_Directory/Modules")
    if (!modulesDirectory.exists() || !modulesDirectory.isDirectory) {
        println("Modules directory does not exist.")
        return null
    }

    // Iterate through module folders to find a match.
    modulesDirectory.listFiles { file -> file.isDirectory }?.forEach { moduleFolder ->
        val moduleName = moduleFolder.name
        if (moduleName.equals(moduleNameQuery, ignoreCase = true)) {
            // Find the week folder within the module.
            val weekFolder = File(moduleFolder, "week$weekNumber")
            if (weekFolder.exists() && weekFolder.isDirectory) {
                // Find the day folder within the week.
                val dayFolder = File(weekFolder, dayName)
                if (dayFolder.exists() && dayFolder.isDirectory) {
                    // Create the lecture folder if it doesn't already exist.
                    val folderName = lectureName + "__" + time
                    val newLectureFolder = File(dayFolder, folderName)
                    if (!newLectureFolder.exists()) {
                        newLectureFolder.mkdir()
                        println("Created folder '$folderName' inside '$dayName' in week '$weekNumber'.")
                    } else {
                        println("Folder '$folderName' already exists inside '$dayName' in week '$weekNumber'.")
                    }
                    return dayFolder.absolutePath
                } else {
                    println("Day '$dayName' not found in week '$weekNumber' of module '$moduleName'.")
                    return null
                }
            } else {
                println("Week $weekNumber not found in module '$moduleName'.")
                return null
            }
        }
    }
    println("No module containing '$moduleNameQuery' found.")
    return null
}

// Retrieves an array of lecture folder names from a specified module, week, and day.
// Returns an empty array if the module/week/day path is invalid.
fun getLecturesInDay(moduleName: String, week: String, day: String): Array<String> {
    val modulesDirectory = File("/data/data/com.example.ryl_app/files/RYL_Directory/Modules")
    if (!modulesDirectory.exists() || !modulesDirectory.isDirectory) {
        println("Modules directory does not exist.")
        return emptyArray()
    }

    // Locate module, week, and day folders sequentially.
    val moduleFolder = File(modulesDirectory, moduleName)
    if (!moduleFolder.exists() || !moduleFolder.isDirectory) {
        println("Module '$moduleName' not found.")
        return emptyArray()
    }
    val weekFolder = File(moduleFolder, "week$week")
    if (!weekFolder.exists() || !weekFolder.isDirectory) {
        println("Week '$week' not found in module '$moduleName'.")
        return emptyArray()
    }
    val dayFolder = File(weekFolder, day)
    if (!dayFolder.exists() || !dayFolder.isDirectory) {
        println("Day '$day' not found in week '$week' of module '$moduleName'.")
        return emptyArray()
    }

    // Return the names of all lecture folders in the day folder.
    val lectureNames = dayFolder.listFiles { file -> file.isDirectory }?.map { it.name }?.toTypedArray() ?: emptyArray()
    return lectureNames
}

// Checks if a new lecture time (formatted as "HH:mm - HH:mm") conflicts with existing lectures on the same day.
// Returns true if a conflict exists, false otherwise.
fun checkLectureTimeConflict(
    moduleName: String,
    weekNumber: String,
    dayName: String,
    time: String
): Boolean {
    val modulesDirectory = File("/data/data/com.example.ryl_app/files/RYL_Directory/Modules")
    if (!modulesDirectory.exists() || !modulesDirectory.isDirectory) {
        println("Modules directory does not exist.")
        return false
    }

    // Locate module, week, and day folders.
    val moduleFolder = File(modulesDirectory, moduleName)
    if (!moduleFolder.exists() || !moduleFolder.isDirectory) {
        println("Module '$moduleName' not found.")
        return false
    }
    val weekFolder = File(moduleFolder, "week$weekNumber")
    if (!weekFolder.exists() || !weekFolder.isDirectory) {
        println("Week '$weekNumber' not found in module '$moduleName'.")
        return false
    }
    val dayFolder = File(weekFolder, dayName)
    if (!dayFolder.exists() || !dayFolder.isDirectory) {
        println("Day '$dayName' not found in week '$weekNumber' of module '$moduleName'.")
        return false
    }

    // Convert the new lecture time into minutes and check for overlapping time ranges.
    val (newStart, newEnd) = time.split(" - ").map { it.toMinutes() }
    dayFolder.listFiles { file -> file.isDirectory }?.forEach { existingLecture ->
        val existingLectureTime = existingLecture.name.split("__").getOrNull(1)
        if (existingLectureTime != null) {
            val (existingStart, existingEnd) = existingLectureTime.split(" - ").map { it.toMinutes() }
            if (newStart < existingEnd && newEnd > existingStart) {
                println("Time conflict: The new lecture ($time) overlaps with existing lecture ($existingLectureTime).")
                return true
            }
        }
    }
    return false
}

// Extension function: Converts a "HH:mm" formatted string into total minutes.
fun String.toMinutes(): Int {
    val (hours, minutes) = this.split(":").map { it.toInt() }
    return hours * 60 + minutes
}

// Deletes a module folder (and its contents) from the "Modules" directory.
// Returns true if deletion is successful.
fun deleteModuleByName(moduleName: String): Boolean {
    val modulesDirectory = File("/data/data/com.example.ryl_app/files/RYL_Directory/Modules")
    val moduleFolder = File(modulesDirectory, moduleName)
    if (moduleFolder.exists() && moduleFolder.isDirectory) {
        val deleted = moduleFolder.deleteRecursively()  // Recursively delete the module folder.
        if (deleted) {
            println("Module '$moduleName' deleted successfully.")
        } else {
            println("Failed to delete module '$moduleName'.")
        }
        return deleted
    } else {
        println("Module '$moduleName' does not exist.")
        return false
    }
}

// Deletes a lecture folder identified by module, week, day, and lecture names.
// Returns true if the lecture folder is deleted successfully.
fun deleteLectureByName(moduleName: String, weekNumber: String, dayName: String, lectureName: String): Boolean {
    val modulesDirectory = File("/data/data/com.example.ryl_app/files/RYL_Directory/Modules")
    val lectureFolder = File(modulesDirectory, "$moduleName/week$weekNumber/$dayName/$lectureName")
    if (!lectureFolder.exists() || !lectureFolder.isDirectory) {
        println("Lecture '$lectureName' does not exist at path: ${lectureFolder.absolutePath}")
        return false
    }
    println("Attempting to delete lecture folder at: ${lectureFolder.absolutePath}")
    // Log the contents of the lecture folder before deletion.
    lectureFolder.walkTopDown().forEach { file ->
        println("Found in folder before deletion: ${file.absolutePath}")
    }
    val deleted = lectureFolder.deleteRecursively()
    val stillExists = lectureFolder.exists()
    if (deleted && !stillExists) {
        println("Lecture '$lectureName' deleted successfully.")
        return true
    } else {
        println("Failed to delete lecture '$lectureName'. Still exists: $stillExists")
        return false
    }
}
