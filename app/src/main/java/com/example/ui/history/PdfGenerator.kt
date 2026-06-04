package com.example.ui.history

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.data.model.Transaction
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

fun printMonthlyExpensePdf(context: Context, transactions: List<Transaction>, isSpanish: Boolean) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val jobName = if (isSpanish) "Resumen de Gastos" else "Expense Summary"

    val currentMonthTx = transactions.filter {
        val calTx = Calendar.getInstance()
        calTx.timeInMillis = it.timestamp
        val calNow = Calendar.getInstance()
        calTx.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) && calTx.get(Calendar.MONTH) == calNow.get(Calendar.MONTH)
    }

    val expenses = currentMonthTx.filter { it.amount < 0 }

    printManager.print(jobName, object : PrintDocumentAdapter() {
        private var pdfDocument: PdfDocument? = null

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: android.os.Bundle?
        ) {
            pdfDocument = PdfDocument()
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder("Resumen_Gastos.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1) // Keep it simple to 1 page for now, or PrintDocumentInfo.PAGE_COUNT_UNKNOWN
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback?
        ) {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument?.startPage(pageInfo) ?: return
            val canvas = page.canvas
            val paint = Paint()

            // Draw PDF Content
            paint.color = Color.BLACK
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val title = if (isSpanish) "Resumen Mensual de Gastos" else "Monthly Expense Summary"
            canvas.drawText(title, 50f, 80f, paint)

            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val dateStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
            canvas.drawText("Mes / Month: ${dateStr.replaceFirstChar { it.uppercase() }}", 50f, 110f, paint)

            var y = 150f
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Categoría / Concepto", 50f, y, paint)
            canvas.drawText("Cantidad", 450f, y, paint)

            y += 20f
            paint.strokeWidth = 1f
            canvas.drawLine(50f, y, 500f, y, paint)
            y += 20f

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            var totalExpenses = 0.0

            expenses.forEach { tx ->
                val dateFmt = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(tx.timestamp))
                val amountStr = String.format(Locale.US, "%.2f %s", tx.amount, tx.currency)
                
                canvas.drawText("${dateFmt} - ${tx.concept} (${tx.category})", 50f, y, paint)
                canvas.drawText(amountStr, 450f, y, paint)
                y += 20f
                totalExpenses += tx.amount

                // Safety break for single page
                if (y > 800f) {
                    canvas.drawText("... más en la siguiente página ...", 50f, y, paint)
                    return@forEach
                }
            }

            y += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 14f
            canvas.drawLine(50f, y, 500f, y, paint)
            y += 20f
            
            val totalStr = String.format(Locale.getDefault(), "Total: %.2f", totalExpenses)
            canvas.drawText(totalStr, 400f, y, paint)

            pdfDocument?.finishPage(page)

            try {
                pdfDocument?.writeTo(FileOutputStream(destination.fileDescriptor))
                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: IOException) {
                callback?.onWriteFailed(e.toString())
            } finally {
                pdfDocument?.close()
                pdfDocument = null
            }
        }
    }, null)
}
