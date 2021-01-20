package ru.osipov.myinsagran.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_share.*
import ru.osipov.myinsagran.R
import ru.osipov.myinsagran.utils.CameraHelper
import ru.osipov.myinsagran.utils.FirebaseHelper
import ru.osipov.myinsagran.utils.GlideApp

class ShareActivity : BaseActivity(2) {

    private lateinit var mCamera: CameraHelper
    private lateinit var mFirebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        Log.d(TAG, "ShareActivity")

        mFirebase = FirebaseHelper(this)

        mCamera = CameraHelper(this)
        mCamera.takeCameraPicture()

        back_image.setOnClickListener { finish() }
        share_text.setOnClickListener { share() }
    }

    private fun share() {

        val imageUri = mCamera.imageUri

        if (mCamera != null){
            mFirebase.mStorage.child("users")
                .child(mFirebase.mAuth.currentUser!!.uid).child("images")
                .child(imageUri!!.lastPathSegment.toString())
                .putFile(imageUri)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        mFirebase.mDatabase.child("images")
                            .child(mFirebase.mAuth.currentUser!!.uid)
                            .push().setValue(imageUri.toString())
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                    startActivity(Intent(this, ProfileActivity::class.java))
                                    finish()
                                } else{
                                    showToast(it.exception!!.message!!)
                                }
                            }
                    }else{
                        showToast(it.exception!!.message!!)
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCamera.REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                GlideApp.with(this).load(mCamera.imageUri).centerCrop().into(post_image)
            } else{
                finish()
            }
        }
    }
}
