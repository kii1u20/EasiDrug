package com.easidrug.persistance

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.easidrug.ui.Screens.LogData
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfLogger(private val context: Context) {

    fun createPdf(logsList: List<LogData>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size page
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        val titlePaint = Paint().apply {
            color = Color.BLUE
            textAlign = Paint.Align.CENTER
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val entryPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val onTimePaint = Paint().apply { color = Color.GREEN }
        val latePaint = Paint().apply { color = Color.RED }

        // Draw the title
        canvas.drawText("Medication Log", (pageInfo.pageWidth / 2).toFloat(), 40f, titlePaint)

        // Draw a line (ribbon) under the title
        canvas.drawRect(0f, 50f, pageInfo.pageWidth.toFloat(), 55f, titlePaint)

        // Y position to start drawing text entries
        var y = 80f

        val sortedLogs =
            com.easidrug.ui.Screens.logsList.sortedWith(compareBy<LogData> { it.timestamp.year }
                .thenBy { it.timestamp.month }
                .thenBy { it.timestamp.day })

        // Header paint
        val headerPaint = Paint(entryPaint).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }

        // Draw table header
        canvas.drawText("Date", 10f, y, headerPaint)
        canvas.drawText("Time", 110f, y, headerPaint)
        canvas.drawText("Pill", 210f, y, headerPaint)
        canvas.drawText("Status", 310f, y, headerPaint)
        y += 20f

        // Draw table rows
        logsList.forEachIndexed { index, log ->
            val backgroundColor = if (index % 2 == 0) Color.LTGRAY else Color.WHITE
            canvas.drawRect(
                0f,
                y,
                pageInfo.pageWidth.toFloat(),
                y + 20f,
                Paint().apply { color = backgroundColor })

            val statusColor =
                if (log.isOnTime.equals("True", ignoreCase = true)) Color.GREEN else Color.RED
            val statusPaint = Paint(entryPaint).apply { color = statusColor }

            val logDate =
                log.timestamp.day.toString() + "/" + log.timestamp.month.toString() + "/" + log.timestamp.year.toString()
            canvas.drawText(logDate, 10f, y + 15f, entryPaint)
            canvas.drawText(
                "${log.timestamp.hour}:${String.format("%02d", log.timestamp.minute)}",
                110f,
                y + 15f,
                entryPaint
            )
            canvas.drawText(log.variable_label, 210f, y + 15f, entryPaint)
            canvas.drawText(
                if (log.isOnTime.equals(
                        "True",
                        ignoreCase = true
                    )
                ) "On Time" else "Late", 310f, y + 15f, statusPaint
            )

            y += 30f // Increase y to move to the next row
        }

        // Finish the page
        pdfDocument.finishPage(page)

        val fileName = "MedicationLogs.pdf"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS + "/EasiDrug"
                )
            }

            val pdfUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            resolver.openOutputStream(pdfUri!!).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        } else {
            val directory =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, fileName)
            pdfDocument.writeTo(FileOutputStream(file))
        }

        Toast.makeText(context, "Medicine Log Exported", Toast.LENGTH_LONG).show()
        pdfDocument.close()
    }
}