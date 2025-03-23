package com.example.ryl_app

import java.io.File
import kotlin.experimental.xor

// A simple XOR key used for both encryption and decryption.
private const val XOR_KEY: Byte = 0x55

// Function: encryptData
// Purpose: Encrypts the given ByteArray by XOR-ing each byte with the XOR_KEY.
// Explanation: XOR encryption is symmetric so applying the same operation twice returns the original data.
fun encryptData(data: ByteArray): ByteArray {
    return data.map { it xor XOR_KEY }.toByteArray()
}

// Function: decryptData
// Purpose: Decrypts an encrypted ByteArray by applying the same XOR operation as used for encryption.
// Note: Since XOR is symmetric, this function is identical to encryptData.
fun decryptData(data: ByteArray): ByteArray {
    return data.map { it xor XOR_KEY }.toByteArray()
}

// Function: encryptFile (single parameter version)
// Purpose: Reads the content of the provided file, encrypts the data using XOR encryption,
//          and writes the encrypted bytes back to the same file.
fun encryptFile(file: File) {
    val data = file.readBytes()           // Read the file's content as a ByteArray.
    val encrypted = encryptData(data)      // Encrypt the data.
    file.writeBytes(encrypted)             // Overwrite the file with the encrypted data.
}

// Function: encryptFile (overloaded version)
// Purpose: Encrypts the content of inputFile and writes the encrypted output to outputFile.
// This allows keeping the original file intact while storing the encrypted version separately.
fun encryptFile(inputFile: File, outputFile: File) {
    val data = inputFile.readBytes()       // Read data from the input file.
    val encrypted = encryptData(data)      // Encrypt the data.
    outputFile.writeBytes(encrypted)       // Write the encrypted data to the output file.
}

// Function: decryptFile
// Purpose: Reads an encrypted file, decrypts its content, and writes the decrypted data to outputFile.
// Explanation: Uses the same XOR operation for decryption since the algorithm is symmetric.
fun decryptFile(encryptedFile: File, outputFile: File) {
    val data = encryptedFile.readBytes()   // Read the encrypted file's bytes.
    val decrypted = decryptData(data)        // Decrypt the data.
    outputFile.writeBytes(decrypted)         // Write the decrypted data to the output file.
}
