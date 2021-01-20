package ru.osipov.myinsagran.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_profile.*
import ru.osipov.myinsagran.R
import ru.osipov.myinsagran.utils.FirebaseHelper
import ru.osipov.myinsagran.utils.GlideApp
import ru.osipov.myinsagran.utils.ValueEventListenerAdapter

class ProfileActivity : BaseActivity(4) {
    private lateinit var mUser: ru.osipov.myinsagran.models.User
    private lateinit var mFirebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNavigation()
        Log.d(TAG, "LikesActivity")

        edit_profile_btn.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        mFirebase = FirebaseHelper(this)
        mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter{
            mUser = it.getValue(ru.osipov.myinsagran.models.User::class.java)!!
            profile_image.loadUserPhoto(mUser.photo!!)
            user_name_text.text = mUser.username
        })

        images_recycler.layoutManager = GridLayoutManager(this, 3)
        mFirebase.mDatabase.child("images")
            .child(mFirebase.mAuth.currentUser!!.uid)
            .addValueEventListener(ValueEventListenerAdapter{
                val images = it.children.map { it.getValue(String::class.java)!! }
                    images_recycler.adapter = ImagesAdapter(images)

            })
    }
}

class ImagesAdapter(private val images: List<String>) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>(){
    class ViewHolder(val image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val image = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false) as ImageView
        return ViewHolder(image)
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.loadImage(images[position])
    }

    private fun ImageView.loadImage(image: String){
        GlideApp.with(context).load(image).centerCrop().into(this)
    }
}

@SuppressLint("AppCompatCustomView")
class SquareImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs){
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}

