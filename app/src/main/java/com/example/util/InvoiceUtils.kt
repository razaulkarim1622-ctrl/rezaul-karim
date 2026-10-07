package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.CustomerTransaction
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.ShopSettings
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("bn", "BD"))
    private val shortDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("bn", "BD"))

    fun formatCurrency(amount: Double, symbol: String = "৳"): String {
        return String.format(Locale.US, "%s %,.2f", symbol, amount)
    }

    fun formatDateTime(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        return shortDateFormatter.format(Date(timestamp))
    }

    fun generateInvoiceNumber(): String {
        val sdf = SimpleDateFormat("yyMMdd-HHmmss", Locale.US)
        return "INV-" + sdf.format(Date())
    }
}

object InvoicePdfGenerator {
    /**
     * Generates a sleek, professional PDF invoice for a Sale and triggers the Android share/print intent.
     */
    fun printOrShareSaleInvoice(
        context: Context,
        sale: Sale,
        items: List<SaleItem>,
        settings: ShopSettings
    ) {
        try {
            val document = PdfDocument()
            val pageWidth = 595 // A4 standard pt
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            var currentY = 50f

            // Header Background Bar
            paint.color = Color.parseColor("#1E3A8A")
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

            // Shop Name
            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.isFakeBoldText = true
            canvas.drawText(settings.shopName, 40f, 45f, paint)

            // Shop Contact
            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("${settings.address} | ফোন: ${settings.phone}", 40f, 70f, paint)

            currentY = 120f

            // Memo / Invoice Title
            paint.color = Color.parseColor("#1E3A8A")
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("বিক্রয় মেমো / CASH MEMO", 40f, currentY, paint)

            paint.color = Color.DKGRAY
            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("মেমো নং: ${sale.invoiceNumber}", (pageWidth - 200).toFloat(), currentY, paint)
            currentY += 18f
            canvas.drawText("তারিখ: ${FormatUtils.formatDateTime(sale.timestamp)}", (pageWidth - 200).toFloat(), currentY, paint)

            currentY += 15f
            paint.color = Color.LTGRAY
            paint.strokeWidth = 1f
            canvas.drawLine(40f, currentY, (pageWidth - 40).toFloat(), currentY, paint)
            currentY += 25f

            // Customer Info Box
            paint.color = Color.BLACK
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("ক্রেতার নাম: ${sale.customerName}", 40f, currentY, paint)
            if (sale.customerPhone.isNotBlank()) {
                paint.isFakeBoldText = false
                canvas.drawText("মোবাইল: ${sale.customerPhone}", 300f, currentY, paint)
            }
            currentY += 25f

            // Items Table Header
            paint.color = Color.parseColor("#EEF2F6")
            canvas.drawRect(40f, currentY - 15f, (pageWidth - 40).toFloat(), currentY + 10f, paint)

            paint.color = Color.parseColor("#1E3A8A")
            paint.textSize = 11f
            paint.isFakeBoldText = true
            canvas.drawText("পণ্য / বিবরণ", 50f, currentY, paint)
            canvas.drawText("পরিমাণ", 280f, currentY, paint)
            canvas.drawText("দর (৳)", 370f, currentY, paint)
            canvas.drawText("মোট দাম (৳)", 460f, currentY, paint)

            currentY += 25f
            paint.color = Color.BLACK
            paint.isFakeBoldText = false
            paint.textSize = 11f

            // Items List
            for (item in items) {
                canvas.drawText(item.productName, 50f, currentY, paint)
                canvas.drawText("${item.quantity} ${item.unit}", 280f, currentY, paint)
                canvas.drawText(String.format(Locale.US, "%.2f", item.unitPrice), 370f, currentY, paint)
                canvas.drawText(String.format(Locale.US, "%.2f", item.totalPrice), 460f, currentY, paint)
                currentY += 20f

                if (currentY > pageHeight - 150) break // Safety
            }

            // Line
            paint.color = Color.LTGRAY
            canvas.drawLine(40f, currentY, (pageWidth - 40).toFloat(), currentY, paint)
            currentY += 25f

            // Totals Section
            val rightAlignX = 360f
            paint.color = Color.BLACK
            paint.textSize = 12f
            canvas.drawText("সাবটোটাল:", rightAlignX, currentY, paint)
            canvas.drawText(FormatUtils.formatCurrency(sale.subtotal, settings.currencySymbol), 460f, currentY, paint)
            currentY += 20f

            if (sale.discount > 0) {
                paint.color = Color.parseColor("#DC2626")
                canvas.drawText("ডিসকাউন্ট:", rightAlignX, currentY, paint)
                canvas.drawText("- " + FormatUtils.formatCurrency(sale.discount, settings.currencySymbol), 460f, currentY, paint)
                currentY += 20f
            }

            paint.color = Color.parseColor("#1E3A8A")
            paint.isFakeBoldText = true
            paint.textSize = 14f
            canvas.drawText("সর্বমোট বিল:", rightAlignX, currentY, paint)
            canvas.drawText(FormatUtils.formatCurrency(sale.grandTotal, settings.currencySymbol), 460f, currentY, paint)
            currentY += 22f

            paint.color = Color.parseColor("#059669")
            paint.isFakeBoldText = false
            paint.textSize = 12f
            canvas.drawText("পরিশোধ (নগদ/অন্যান্য):", rightAlignX, currentY, paint)
            canvas.drawText(FormatUtils.formatCurrency(sale.paidAmount, settings.currencySymbol), 460f, currentY, paint)
            currentY += 20f

            if (sale.dueAmount > 0) {
                paint.color = Color.parseColor("#DC2626")
                paint.isFakeBoldText = true
                canvas.drawText("বর্তমান বাকি:", rightAlignX, currentY, paint)
                canvas.drawText(FormatUtils.formatCurrency(sale.dueAmount, settings.currencySymbol), 460f, currentY, paint)
            } else {
                paint.color = Color.DKGRAY
                canvas.drawText("পরিশোধের অবস্থা:", rightAlignX, currentY, paint)
                canvas.drawText("সম্পূর্ণ পরিশোধিত (PAID)", 460f, currentY, paint)
            }

            // Footer Message
            currentY = (pageHeight - 60).toFloat()
            paint.color = Color.parseColor("#1E3A8A")
            paint.isFakeBoldText = false
            paint.textSize = 11f
            canvas.drawText(settings.thankYouMessage, 40f, currentY, paint)

            document.finishPage(page)

            // Save PDF to cache and launch chooser
            val cachePath = File(context.cacheDir, "invoices")
            cachePath.mkdirs()
            val file = File(cachePath, "${sale.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Invoice ${sale.invoiceNumber}")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(shareIntent, "ইনভয়েস শেয়ার বা প্রিন্ট করুন"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "ইনভয়েস তৈরিতে ত্রুটি: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Generates Customer Payment Receipt PDF
     */
    fun printCustomerPaymentReceipt(
        context: Context,
        tx: CustomerTransaction,
        customerName: String,
        settings: ShopSettings
    ) {
        try {
            val document = PdfDocument()
            val pageWidth = 400
            val pageHeight = 550
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            // Header
            paint.color = Color.parseColor("#0F766E")
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 80f, paint)

            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText(settings.shopName, 25f, 40f, paint)

            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText(settings.phone, 25f, 60f, paint)

            var currentY = 115f
            paint.color = Color.BLACK
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("বাকি পরিশোধ মানি রিসিট", 25f, currentY, paint)

            currentY += 25f
            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("তারিখ: ${FormatUtils.formatDateTime(tx.timestamp)}", 25f, currentY, paint)
            currentY += 20f
            canvas.drawText("কাস্টমারের নাম: $customerName", 25f, currentY, paint)
            currentY += 20f
            canvas.drawText("পেমেন্ট মেথড: ${tx.paymentMethod}", 25f, currentY, paint)
            currentY += 25f

            // Box
            paint.color = Color.parseColor("#ECFDF5")
            canvas.drawRect(20f, currentY, (pageWidth - 20).toFloat(), currentY + 70f, paint)

            paint.color = Color.parseColor("#047857")
            paint.textSize = 13f
            paint.isFakeBoldText = true
            canvas.drawText("জমা করা টাকা: ${FormatUtils.formatCurrency(tx.amount, settings.currencySymbol)}", 35f, currentY + 30f, paint)
            paint.color = Color.parseColor("#B91C1C")
            canvas.drawText("অবশিষ্ট বাকি: ${FormatUtils.formatCurrency(tx.remainingDue, settings.currencySymbol)}", 35f, currentY + 55f, paint)

            currentY += 120f
            paint.color = Color.GRAY
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText(settings.thankYouMessage, 25f, currentY, paint)

            document.finishPage(page)

            val cachePath = File(context.cacheDir, "receipts")
            cachePath.mkdirs()
            val file = File(cachePath, "Receipt_${tx.id}.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(shareIntent, "রিসিট শেয়ার বা প্রিন্ট করুন"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "রিসিট প্রিন্ট করতে সমস্যা: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
