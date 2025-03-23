package com.example.ryl_app

import android.content.Context
import java.io.File
import androidx.compose.ui.graphics.Color




fun createRYLDirectory(context: Context): File {
    val rootDir = context.filesDir  // App's private internal storage
    val rylDirectory = File(rootDir, "RYL_Directory")
    if (!rylDirectory.exists()) {
        rylDirectory.mkdirs()  // Create RYL_Directory if it doesn't exist
    }

    // Create the Modules folder inside RYL_Directory
    val modulesDirectory = File(rylDirectory, "Modules")
    if (!modulesDirectory.exists()) {
        modulesDirectory.mkdirs()  // Create Modules folder if it doesn't exist
    }

    return rylDirectory
}






fun createWeeklyFolders(mainDirectory: String, name: String, numberOfWeeks: Int, textColor: Color) {
    // Create the main folder (e.g., 'name')
    val mainFolder = File(mainDirectory, name)
    if (!mainFolder.exists()) {
        mainFolder.mkdir()  // Create the main folder
        println("Main folder '$name' created in '$mainDirectory'")
    } else {
        println("Main folder '$name' already exists in '$mainDirectory'")
    }

    // Create module_info.txt and write both the color and number of weeks
    val infoFile = File(mainFolder, "module_info.txt")
    val infoContent = """
        Text Color: $textColor
        Weeks: $numberOfWeeks
    """.trimIndent()

    infoFile.writeText(infoContent) // Save the color and weeks info

    println("Created module_info.txt with color and weeks data.")

    // Days of the week (subfolders to be created in each week folder)
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    // Loop through and create 'week' folders
    for (weekNum in 1..numberOfWeeks) {
        val weekFolderName = "week$weekNum"
        val weekFolder = File(mainFolder, weekFolderName)

        if (!weekFolder.exists()) {
            weekFolder.mkdir()  // Create the week folder
            println("Created folder: $weekFolder")
        }

        // Inside each week folder, create folders for each day of the week
        for (day in daysOfWeek) {
            val dayFolder = File(weekFolder, day)
            if (!dayFolder.exists()) {
                dayFolder.mkdir()  // Create the day folder
                println("Created folder: $dayFolder")
            }
        }
    }
}



fun modulemaker(name: String, numberOfWeeks: Int, textColor: androidx.compose.ui.graphics.Color): Boolean {
    val mainDirectory = "/data/data/com.example.ryl_app/files/RYL_Directory/Modules"
    val modulePath = File(mainDirectory, name)

    //if module exits then done create
    if (modulePath.exists()) {
        println("Module '$name' already exists in Modules directory.")

        return false
    }

    createWeeklyFolders(mainDirectory, name, numberOfWeeks, textColor)
    println("Module '$name' created successfully")
    return true
}


fun getModuleData(): Triple<Array<String>, Array<Color>, Array<Int>> {
    val modulesDirectory = File("/data/data/com.example.ryl_app/files/RYL_Directory/Modules")

    if (!modulesDirectory.exists() || !modulesDirectory.isDirectory) {
        println("Modules directory does not exist.")
        return Triple(emptyArray(), emptyArray(), emptyArray())
    }

    val modulePaths = mutableListOf<String>()
    val moduleColors = mutableListOf<Color>()
    val moduleNumbers = mutableListOf<Int>()

    modulesDirectory.listFiles { file -> file.isDirectory }?.forEach { moduleFolder ->
        val modulePath = moduleFolder.absolutePath
        modulePaths.add(modulePath)

        // Read module_info.txt inside the module folder
        val infoFile = File(moduleFolder, "module_info.txt")
        if (infoFile.exists()) {
            val lines = infoFile.readLines()

            // Default values
            var textColor = Color.Black
            var number = 0

            lines.forEach { line ->
                when {
                    line.startsWith("Text Color:") -> {
                        // Extract color string and parse it
                        val colorString = line.removePrefix("Text Color:").trim()
                        textColor = parseComposeColor(colorString)
                    }
                    line.startsWith("Weeks:") -> {
                        // Extract the number following "Weeks:" (remove the prefix and trim the spaces)
                        val numberString = line.removePrefix("Weeks:").trim()
                        number = numberString.toIntOrNull() ?: 0  // Default to 0 if not valid
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





//is used to handle the colors coming from the text file
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


//function that allows the addition of a named folder for the lecture times
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

    // List all the subfolders in the Modules directory
    modulesDirectory.listFiles { file -> file.isDirectory }?.forEach { moduleFolder ->
        val moduleName = moduleFolder.name

        // Check if the module name contains the query string (case insensitive)
        if (moduleName.equals(moduleNameQuery, ignoreCase = true)) {
            // Now, search for the specific week folder inside the module
            val weekFolder = File(moduleFolder, "week$weekNumber")

            if (weekFolder.exists() && weekFolder.isDirectory) {
                // Now, search for the specific day folder inside the week
                val dayFolder = File(weekFolder, dayName)

                if (dayFolder.exists() && dayFolder.isDirectory) {
                    // Create a new folder with the name "lectureName__time"
                    val folderName = lectureName + "__" + time
                    val newLectureFolder = File(dayFolder, folderName)

                    if (!newLectureFolder.exists()) {
                        newLectureFolder.mkdir()  // Create the new folder
                        println("Created folder '$folderName' inside '$dayName' in week '$weekNumber'.")
                    } else {
                        println("Folder '$folderName' already exists inside '$dayName' in week '$weekNumber'.")
                    }

                    // Return the path to the day folder
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

    // If no module was found
    println("No module containing '$moduleNameQuery' found.")
    return null
}



// fetches the lecture folder names inside of a day.
fun getLecturesInDay(moduleName: String, week: String, day: String): Array<String> {
    val modulesDirectory = File("/data/data/com.example.ryl_app/files/RYL_Directory/Modules")

    if (!modulesDirectory.exists() || !modulesDirectory.isDirectory) {
        println("Modules directory does not exist.")
        return emptyArray()
    }

    // Locate the module folder
    val moduleFolder = File(modulesDirectory, moduleName)
    if (!moduleFolder.exists() || !moduleFolder.isDirectory) {
        println("Module '$moduleName' not found.")
        return emptyArray()
    }

    // Locate the week folder
    val weekFolder = File(moduleFolder, "week$week")
    if (!weekFolder.exists() || !weekFolder.isDirectory) {
        println("Week '$week' not found in module '$moduleName'.")
        return emptyArray()
    }

    // Locate the day folder
    val dayFolder = File(weekFolder, day)
    if (!dayFolder.exists() || !dayFolder.isDirectory) {
        println("Day '$day' not found in week '$week' of module '$moduleName'.")
        return emptyArray()
    }

    // Get all lecture folder names inside the day folder
    val lectureNames = dayFolder.listFiles { file -> file.isDirectory }?.map { it.name }?.toTypedArray() ?: emptyArray()

    return lectureNames
}


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

    // Convert new lecture time to a comparable format
    val (newStart, newEnd) = time.split(" - ").map { it.toMinutes() }

    // Check if any existing lecture has overlapping time
    dayFolder.listFiles { file -> file.isDirectory }?.forEach { existingLecture ->
        val existingLectureTime = existingLecture.name.split("__").getOrNull(1)

        if (existingLectureTime != null) {
            val (existingStart, existingEnd) = existingLectureTime.split(" - ").map { it.toMinutes() }

            // Check for time overlap
            if (newStart < existingEnd && newEnd > existingStart) {
                println("Time conflict: The new lecture ($time) overlaps with existing lecture ($existingLectureTime).")
                return true
            }
        }
    }

    return false
}

// Extension function to convert "HH:mm" string to minutes
fun String.toMinutes(): Int {
    val (hours, minutes) = this.split(":").map { it.toInt() }
    return hours * 60 + minutes
}




fun deleteModuleByName(moduleName: String): Boolean {
    val modulesDirectory = File("/data/data/com.example.ryl_app/files/RYL_Directory/Modules")
    val moduleFolder = File(modulesDirectory, moduleName)

    if (moduleFolder.exists() && moduleFolder.isDirectory) {
        // Recursively delete the module directory
        val deleted = moduleFolder.deleteRecursively()
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

fun deleteLectureByName(moduleName: String, weekNumber: String, dayName: String, lectureName: String): Boolean {
    val modulesDirectory = File("/data/data/com.example.ryl_app/files/RYL_Directory/Modules")
    val lectureFolder = File(modulesDirectory, "$moduleName/week$weekNumber/$dayName/$lectureName")

    if (!lectureFolder.exists() || !lectureFolder.isDirectory) {
        println("Lecture '$lectureName' does not exist at path: ${lectureFolder.absolutePath}")
        return false
    }

    println("Attempting to delete lecture folder at: ${lectureFolder.absolutePath}")

    // Log contents before deletion
    lectureFolder.walkTopDown().forEach { file ->
        println("Found in folder before deletion: ${file.absolutePath}")
    }

    val deleted = lectureFolder.deleteRecursively()

    // Double-check that it's gone
    val stillExists = lectureFolder.exists()

    if (deleted && !stillExists) {
        println("Lecture '$lectureName' deleted successfully.")
        return true
    } else {
        println("Failed to delete lecture '$lectureName'. Still exists: $stillExists")
        return false
    }
}

