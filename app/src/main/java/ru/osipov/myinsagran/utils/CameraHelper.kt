package ru.osipov.myinsagran.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraHelper(private val activity: Activity){

    val timeStamp: String = SimpleDateFormat(
        "yyyyMMdd_HHmmss",
        Locale.US
    ).format(Date())
    val REQUEST_CODE: Int = 1
    var imageUri: Uri? = null

    fun takeCameraPicture() {
        val intent =
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null){
            val imageFile = createImageFile()
            imageUri = FileProvider.getUriForFile(
                activity,
                "ru.osipov.myinsagran.fileprovider",
                imageFile
            );
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }
    }

    private fun createImageFile(): File {
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp.format(Date())}_",
            ".jpg",
            storageDir
        )
    }

}