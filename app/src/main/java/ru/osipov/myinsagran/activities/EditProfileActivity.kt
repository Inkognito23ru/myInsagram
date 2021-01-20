package ru.osipov.myinsagran.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.android.synthetic.main.activity_edit_profile.*
import ru.osipov.myinsagran.R
import ru.osipov.myinsagran.models.User
import ru.osipov.myinsagran.utils.CameraHelper
import ru.osipov.myinsagran.utils.FirebaseHelper
import ru.osipov.myinsagran.utils.ValueEventListenerAdapter
import ru.osipov.myinsagran.views.PasswordDialog

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {

    private lateinit var mUser: User
    private lateinit var mPendingUser: User
    private lateinit var cameraHelper: CameraHelper
    private lateinit var mFirebaseHelper: FirebaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "EditProfileActivity")

        cameraHelper = CameraHelper(this)

        close_image.setOnClickListener {finish()}
        save_image.setOnClickListener{updateProfile()}
        change_photo_text.setOnClickListener {
            cameraHelper.takeCameraPicture()
        }

        mFirebaseHelper = FirebaseHelper(this)

        mFirebaseHelper.currentUserReference().addListenerForSingleValueEvent(
            ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                name_input.setText(mUser.name)
                user_input.setText(mUser.username)
                website_input.setText(mUser.website)
                bio_input.setText(mUser.bio)
                email_input.setText(mUser.email)
                phone_input.setText(mUser.phone?.toString())
                profile_image.loadUserPhoto(mUser.photo)
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraHelper.REQUEST_CODE && resultCode == Activity.RESULT_OK){

            val uid = mFirebaseHelper.mAuth.currentUser!!.uid

            mFirebaseHelper.mStorage.child("users/$uid/photo").putFile(cameraHelper.imageUri!!).addOnCompleteListener {
                if (it.isSuccessful) {
                    val downloadTask = it.result!!.metadata!!.reference!!.downloadUrl
                    downloadTask.addOnSuccessListener { uri ->
                        mUser = mUser.copy(photo = uri.toString())
                        profile_image.loadUserPhoto(mUser.photo)
                        mFirebaseHelper.mDatabase.child("users/$uid/photo").setValue(uri.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "onActivityResult: photo saved successfully")
                                } else {
                                    showToast(task.exception!!.message!!)
                                }
                            }
                    }
                } else {
                    showToast(it.exception!!.message!!)
                }
            }
        }
    }

    private fun updateProfile() {
        mPendingUser = readInputs()

        val error = validate(mPendingUser)
        if (error == null){
            if (mPendingUser.email == mUser.email){
                updateUser(mPendingUser)
            } else{
                PasswordDialog().show(supportFragmentManager, "password_dialog")
            }
        } else{
            showToast(error)
        }
    }

    private fun readInputs(): User{
        return User(
            name = name_input.text.toString(),
            username = user_input.text.toString(),
            email = email_input.text.toString(),
            bio = bio_input.text.toStringOrNull(),
            website = website_input.text.toStringOrNull(),
            phone = phone_input.text.toString().toLongOrNull()
        )
    }

    override fun onPasswordConfirm(password: String) {

        if (password.isNotEmpty()){
            val credential = EmailAuthProvider.getCredential(mUser.email, password)
            mFirebaseHelper.reauthenticate(credential){
                    mFirebaseHelper.updateEmail(mPendingUser.email){
                            updateUser(mPendingUser)
                    }
            }
        } else {
            showToast("You should enter your password")
        }
    }

    private fun updateUser(user: User){

        val updateMap = mutableMapOf<String, Any?>()
        if (user.name != mUser.name) updateMap["name"] = user.name
        if (user.username != mUser.username) updateMap["username"] = user.username
        if (user.bio != mUser.bio) updateMap["bio"] = user.bio
        if (user.email != mUser.email) updateMap["email"] = user.email
        if (user.website != mUser.website) updateMap["website"] = user.website
        if (user.phone != mUser.phone) updateMap["phone"] = user.phone

        mFirebaseHelper.updateUser(updateMap){
            showToast("Profile saved")
            finish()
        }
    }

    private fun validate(user: User): String? = when{
        user.name.isEmpty() -> "Please enter name"
        user.username.isEmpty() -> "Please enter username"
        user.email.isEmpty() -> "Please enter email"
        else -> null
        }
}


