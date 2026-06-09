package com.example.aicompanion

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object PdfUtils {

    fun extractTextFromPdf(
        context: Context,
        uri: Uri
    ): String {

        PDFBoxResourceLoader.init(context)

        val inputStream =
            context.contentResolver.openInputStream(uri)

        val document =
            PDDocument.load(inputStream)

        val text =
            PDFTextStripper().getText(document)

        document.close()

        return text
    }

    suspend fun extractTextFromScannedPdf(
        context: Context,
        uri: Uri
    ): String {

        val fileDescriptor =
            context.contentResolver.openFileDescriptor(uri, "r")
                ?: return ""

        val renderer =
            PdfRenderer(fileDescriptor)

        val recognizer =
            TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
            )

        val resultText = StringBuilder()

        for (i in 0 until renderer.pageCount) {

            val page = renderer.openPage(i)

            val bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )

            page.render(
                bitmap,
                null,
                null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
            )

            val image =
                InputImage.fromBitmap(bitmap, 0)

            val visionText =
                recognizer.process(image).await()

            resultText.append(
                visionText.text
            )

            resultText.append("\n")

            page.close()
        }

        renderer.close()
        fileDescriptor.close()

        return resultText.toString()
    }
}