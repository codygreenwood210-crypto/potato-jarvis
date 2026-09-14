package com.potato.jarvis.vision

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class VisionProcessor {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognize(context: Context, uri: Uri): String = suspendCancellableCoroutine { continuation ->
        val image = runCatching { InputImage.fromFilePath(context, uri) }
            .getOrElse { error ->
                continuation.resumeWithException(error)
                return@suspendCancellableCoroutine
            }
        recognizer.process(image)
            .addOnSuccessListener { result ->
                if (continuation.isActive) continuation.resume(result.text)
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }
    }

    fun close() = recognizer.close()
}
