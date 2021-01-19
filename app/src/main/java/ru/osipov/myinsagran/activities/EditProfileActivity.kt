package ru.osipov.myinsagran.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_edit_profile.*
import ru.osipov.myinsagran.R
import ru.osipov.myinsagran.models.User
import ru.osipov.myinsagran.views.PasswordDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraPictureTaker{

}

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {

    private lateinit var mImageUri: Uri
    private lateinit var mStorage: StorageReference
    private val TAKE_PICTURE_REQUEST_CODE: Int = 1
    private lateinit var mUser: User
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mPendingUser: User
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "EditProfileActivity")

        close_image.setOnClickListener {finish()}
        save_image.setOnClickListener{updateProfile()}
        change_photo_text.setOnClickListener {
            takeCameraPicture()
        }

        mAuth = FirebaseAuth.getInstance()
        mStorage = FirebaseStorage.getInstance().reference
        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("users").child(mAuth.currentUser!!.uid).addListenerForSingleValueEvent(
            ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                name_input.setText(mUser.name, TextView.BufferType.EDITABLE)
                user_input.setText(mUser.username, TextView.BufferType.EDITABLE)
                website_input.setText(mUser.website, TextView.BufferType.EDITABLE)
                bio_input.setText(mUser.bio, TextView.BufferType.EDITABLE)
                email_input.setText(mUser.email, TextView.BufferType.EDITABLE)
                phone_input.setText(mUser.phone?.toString(), TextView.BufferType.EDITABLE)
                profile_image.loadUserPhoto(mUser.photo)
            })
    }



    private fun takeCameraPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null){
            val imageFile = createImageFile()
            mImageUri = FileProvider.getUriForFile(this,
                "ru.osipov.myinsagran.fileprovider",
                imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE)
        }
    }

    private fun createImageFile(): File{
       val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
       return File.createTempFile(
            "JPEG_${timeStamp.format(Date())}_",
            ".jpg",
            storageDir
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            val uid = mAuth.currentUser!!.uid

            mStorage.child("users/$uid/photo").putFile(mImageUri).addOnCompleteListener {
                if (it.isSuccessful) {
                    val downloadTask = it.result!!.metadata!!.reference!!.downloadUrl
                    downloadTask.addOnSuccessListener { uri ->
                        mUser = mUser.copy(photo = uri.toString())
                        profile_image.loadUserPhoto(mUser.photo)
                        mDatabase.child("users/$uid/photo").setValue(uri.toString())
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
            mAuth.currentUser!!.reauthenticate(credential){

                    mAuth.currentUser!!.updateEmail(mPendingUser.email){
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

        mDatabase.updateUser(mAuth.currentUser!!.uid, updateMap){
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

    private fun DatabaseReference.updateUser(uid: String, updates: Map<String, Any?>, onSuccess: () -> Unit){
        child("users").child(uid)
            .updateChildren(updates)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    onSuccess()
                } else{
                    showToast(it.exception!!.message!!)
                }
            }
    }

    private fun FirebaseUser.updateEmail(email: String, onSuccess: () -> Unit){
        updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful){
                onSuccess()
            } else{
                showToast(it.exception!!.message!!)
            }
        }
    }

    private fun FirebaseUser.reauthenticate(credential: AuthCredential, onSuccess: () -> Unit){
        reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful){
                onSuccess()
            } else{
                showToast(it.exception!!.message!!)
            }
        }
    }
}


