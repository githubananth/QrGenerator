package com.xsquare.qrgenerator

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.xsquare.qrgenerator.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private var qrInput: String? = null

    companion object {
        const val QRCodeWidth = 500
        const val QRCodeHeight = 500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        listeners()
    }

    private fun listeners() {
        activityMainBinding.btnGenerateQrCodeId.setOnClickListener {
            getUserInput()
            if (validateInputField()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = textToImageEncode(qrInput!!)
                    showBitmapImage(bitmap)
                }
            }

        }
    }

    private fun getUserInput() {
        qrInput = activityMainBinding.edtQrTextId.text.toString()
    }

    private fun validateInputField(): Boolean {
        return if (qrInput.isNullOrEmpty()) {
            Toast.makeText(this, "Please Enter the QR code Text", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }


    @Throws(WriterException::class)
    private fun textToImageEncode(value: String): Bitmap? {

        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.MARGIN] = 1

        val bitMatrix: BitMatrix = try {
            MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, QRCodeWidth, QRCodeHeight, hints)
        } catch (illegalArgumentException: IllegalArgumentException) {
            return null
        }
        val bitMatrixWidth = bitMatrix.width
        val bitMatrixHeight = bitMatrix.height
        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)
        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth
            for (x in 0 until bitMatrixWidth) {
                pixels[offset + x] = if (bitMatrix[x, y])
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        resources.getColor(R.color.colorBlack, null)
                    } else {
                        resources.getColor(R.color.colorBlack)
                    }
                else
                    resources.getColor(R.color.colorWhite)
            }
        }
        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight)
        return bitmap
    }

    private suspend fun showBitmapImage(bitmap: Bitmap?) {
        withContext(Dispatchers.Main) {
            Log.i("ProfileDetail ===>3: ", Thread.currentThread().name)
            activityMainBinding.progressCircleId.visibility = View.VISIBLE
            activityMainBinding.lytQrImageId.visibility = View.VISIBLE
            activityMainBinding.progressCircleId.visibility = View.GONE

            activityMainBinding.imgQRCodeId.setImageBitmap(bitmap)
        }
    }
}