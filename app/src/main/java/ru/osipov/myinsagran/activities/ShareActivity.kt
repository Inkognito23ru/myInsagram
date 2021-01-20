package ru.osipov.myinsagran.activities

import android.app.Activity
import android.content.Intent
import android.icu.text.DateTimePatternGenerator.PatternInfo.OK
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_share.*
import ru.osipov.myinsagran.R
import ru.osipov.myinsagran.utils.CameraHelper
import ru.osipov.myinsagran.utils.GlideApp

class ShareActivity : BaseActivity(2) {

    private lateinit var mCameraHelper: CameraHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        Log.d(TAG, "ShareActivity")

        mCameraHelper = CameraHelper(this)
        mCameraHelper.takeCameraPicture()

        back_image.setOnClickListener { finish() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCameraHelper.REQUEST_CODE && resultCode == Activity.RESULT_OK){
            GlideApp.with(this).load(mCameraHelper.imageUri).centerCrop().into(post_image)
        }
    }
}
