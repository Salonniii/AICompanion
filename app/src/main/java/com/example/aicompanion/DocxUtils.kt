package com.example.aicompanion

import android.content.Context
import android.net.Uri
import org.apache.poi.xwpf.usermodel.XWPFDocument

object DocxUtils {

    fun extractTextFromDocx(
        context: Context,
        uri: Uri
    ): String {

        val inputStream =
            context.contentResolver.openInputStream(uri)
                ?: return ""

        val document =
            XWPFDocument(inputStream)

        val text =
            document.paragraphs.joinToString("\n") {
                it.text
            }

        document.close()
        inputStream.close()

        return text
    }
}