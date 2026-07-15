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
import com.dolo.doctor.data.model.PaymentStatus
import com.dolo.doctor.data.model.TokenReceipt
import java.io.FileOutputStream

object AndroidTokenReceiptPrinter {
    private val receiptMediaSize = PrintAttributes.MediaSize("DOLO_58MM", "58 mm receipt", 2283, 7000)

    fun print(context: Context, receipt: TokenReceipt) {
        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val attributes = PrintAttributes.Builder()
            .setMediaSize(receiptMediaSize)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .setResolution(PrintAttributes.Resolution("DOLO_203_DPI", "203 dpi thermal", 203, 203))
            .build()
        manager.print("DO-LO ${receipt.session} token ${receipt.token}", ReceiptDocumentAdapter(receipt), attributes)
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
                PrintDocumentInfo.Builder("dolo-${receipt.session.lowercase()}-${receipt.token}.pdf")
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
                val pageWidth = 164
                val pageHeight = 500
                val document = PdfDocument()
                val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
                val canvas = page.canvas
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.BLACK
                    textAlign = Paint.Align.CENTER
                }
                val centerX = pageWidth / 2f

                fun centered(value: String, y: Float, size: Float = 10f, bold: Boolean = false) {
                    paint.textSize = size
                    paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                    canvas.drawText(value, centerX, y, paint)
                }
                fun wrapped(value: String, startY: Float, size: Float = 9f, bold: Boolean = false, maxWidth: Float = 144f): Float {
                    paint.textSize = size
                    paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                    val lines = mutableListOf<String>()
                    var current = ""
                    value.split(Regex("\\s+")).filter(String::isNotBlank).forEach { word ->
                        val candidate = if (current.isBlank()) word else "$current $word"
                        if (paint.measureText(candidate) <= maxWidth) current = candidate
                        else {
                            if (current.isNotBlank()) lines += current
                            current = word
                        }
                    }
                    if (current.isNotBlank()) lines += current
                    var y = startY
                    lines.take(3).forEach { line ->
                        canvas.drawText(line, centerX, y, paint)
                        y += size + 4f
                    }
                    return y
                }
                fun divider(y: Float) {
                    paint.strokeWidth = 1f
                    canvas.drawLine(10f, y, pageWidth - 10f, y, paint)
                }

                centered("DO-LO", 25f, 20f, true)
                centered(receipt.clinicName, 45f, 11f, true)
                var y = wrapped(receipt.clinicAddress, 61f, 8f)
                divider(y + 1f)
                y += 20f
                centered(receipt.session.uppercase() + " TOKEN", y, 12f, true)
                y += 54f
                centered(receipt.token.toString(), y, 48f, true)
                y += 17f
                divider(y)
                y += 18f
                centered(receipt.patientName, y, 11f, true)
                y += 15f
                if (receipt.patientPhone.isNotBlank()) { centered(receipt.patientPhone, y, 9f); y += 14f }
                y = wrapped(receipt.doctorName, y, 9f, true)
                centered(receipt.appointmentDate + " | " + receipt.session, y, 9f)
                y += 15f
                centered("Booking: " + receipt.bookingSource.name.replace("_", " "), y, 8f)
                y += 14f
                val feeLine = if (receipt.paymentStatus == PaymentStatus.WAIVED) "CONSULTATION FEE: WAIVED"
                    else "FEE PAID: INR ${receipt.consultationFee} | ${receipt.paymentMethod.name}"
                centered(feeLine, y, 9f, true)
                y += 14f
                if (receipt.paidAt.isNotBlank()) { centered("Confirmed: " + receipt.paidAt, y, 8f); y += 13f }
                centered(receipt.receiptNumber, y, 8f)
                y += 17f
                divider(y)
                y += 18f
                wrapped("Please hand this receipt to the doctor.", y, 9f, true)

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
