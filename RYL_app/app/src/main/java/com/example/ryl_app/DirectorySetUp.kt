package com.example.ryl_app

import android.content.Context
import java.io.File
import androidx.compose.ui.graphics.Color




// Utility function to create the directory
fun createRYLDirectory(context: Context): File {
    val rootDir = context.filesDir  // App's private internal storage
    val directory = File(rootDir, "RYL_Directory")

    // Check if the directory exists; if not, create it
    if (!directory.exists()) {
        directory.mkdirs()  // Creates the directory if it doesn't exist
    }

    return directory
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
        if (moduleName.contains(moduleNameQuery, ignoreCase = true)) {
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
