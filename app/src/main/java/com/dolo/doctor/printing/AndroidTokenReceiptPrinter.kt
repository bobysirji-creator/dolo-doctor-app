package com.dolo.doctor.printing

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.PageRange
import com.dolo.doctor.data.model.TokenReceipt
import java.io.FileOutputStream

object AndroidTokenReceiptPrinter {
    fun print(context: Context, receipt: TokenReceipt) {
        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        manager.print(
            "DO-LO token ${receipt.token}",
            ReceiptDocumentAdapter(receipt),
            PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT).build()
        )
    }

    private class ReceiptDocumentAdapter(private val receipt: TokenReceipt) : PrintDocumentAdapter() {
        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal.isCanceled) {
                callback.onLayoutCancelled()
                return
            }
            callback.onLayoutFinished(
                PrintDocumentInfo.Builder("dolo-token-${receipt.token}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build(),
                true
            )
        }

        override fun onWrite(
            pages: Array<out PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal,
            callback: WriteResultCallback
        ) {
            if (cancellationSignal.isCanceled) {
                callback.onWriteCancelled()
                return
            }
            runCatching {
                val document = PdfDocument()
                val page = document.startPage(PdfDocument.PageInfo.Builder(384, 700, 1).create())
                val canvas = page.canvas
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.BLACK }
                fun line(value: String, y: Float, size: Float = 18f, bold: Boolean = false) {
                    paint.textSize = size
                    paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                    canvas.drawText(value, 24f, y, paint)
                }
                line("DO-LO", 45f, 28f, true)
                line(receipt.clinicName, 76f, 19f, true)
                line(receipt.clinicAddress.take(42), 102f, 13f)
                line("TOKEN", 150f, 21f, true)
                line(receipt.token.toString(), 225f, 68f, true)
                line("Patient: ${receipt.patientName}", 270f, 17f, true)
                if (receipt.patientPhone.isNotBlank()) line("Mobile: ${receipt.patientPhone}", 296f, 15f)
                line("Doctor: ${receipt.doctorName}", 322f, 15f)
                line("Date: ${receipt.appointmentDate}", 348f, 15f)
                line("Session: ${receipt.session}", 374f, 15f)
                line("Booking: ${receipt.bookingSource.name.replace("_", " ")}", 400f, 15f)
                line("Receipt: ${receipt.receiptNumber}", 426f, 15f)
                line("Generated: ${receipt.generatedAt}", 452f, 15f)
                line("Please hand this receipt to the doctor.", 500f, 14f, true)
                document.finishPage(page)
                FileOutputStream(destination.fileDescriptor).use { document.writeTo(it) }
                document.close()
            }.onSuccess {
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }.onFailure {
                callback.onWriteFailed(it.message ?: "Unable to create token receipt")
            }
        }
    }
}
