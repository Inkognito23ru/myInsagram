package ru.osipov.myinsagran.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_profile.*
import ru.osipov.myinsagran.R
import ru.osipov.myinsagran.utils.FirebaseHelper
import ru.osipov.myinsagran.utils.ValueEventListenerAdapter

class ProfileActivity : BaseActivity(4) {
    private lateinit var mUser: ru.osipov.myinsagran.models.User
    private lateinit var mFirebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNavigation()
        Log.d(TAG, "LikesActivity")

        edit_profile_btn.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        mFirebaseHelper = FirebaseHelper(this)
        mFirebaseHelper.currentUserReference().addValueEventListener(ValueEventListenerAdapter{
            mUser = it.getValue(ru.osipov.myinsagran.models.User::class.java)!!
            profile_image.loadUserPhoto(mUser.photo!!)
            user_name_text.text = mUser.username
        })
    }
}
