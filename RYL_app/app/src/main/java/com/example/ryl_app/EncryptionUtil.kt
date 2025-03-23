package com.example.ryl_app

import java.io.File
import kotlin.experimental.xor

// A simple XOR key for demonstration purposes.
private const val XOR_KEY: Byte = 0x55

// Encrypts the given data by XOR-ing each byte with XOR_KEY.
fun encryptData(data: ByteArray): ByteArray {
    return data.map { it xor XOR_KEY }.toByteArray()
}

// Decrypts the data (since XOR is symmetric, the same operation decrypts it).
fun decryptData(data: ByteArray): ByteArray {
    return data.map { it xor XOR_KEY }.toByteArray()
}

// Reads the file, encrypts its content, and writes the encrypted bytes back to the same file.
fun encryptFile(file: File) {
    val data = file.readBytes()
    val encrypted = encryptData(data)
    file.writeBytes(encrypted)
}

// Overloaded version: Encrypts the inputFile and writes the encrypted content to outputFile.
fun encryptFile(inputFile: File, outputFile: File) {
    val data = inputFile.readBytes()
    val encrypted = encryptData(data)
    outputFile.writeBytes(encrypted)
}

// Decrypts an encrypted file and writes the decrypted data to a provided output file.
fun decryptFile(encryptedFile: File, outputFile: File) {
    val data = encryptedFile.readBytes()
    val decrypted = decryptData(data)
    outputFile.writeBytes(decrypted)
}
