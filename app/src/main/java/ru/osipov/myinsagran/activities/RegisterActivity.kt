package ru.osipov.myinsagran.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_email.email_input
import kotlinx.android.synthetic.main.fragment_register_namepass.*
import kotlinx.android.synthetic.main.fragment_register_namepass.password_input
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import ru.osipov.myinsagran.R
import ru.osipov.myinsagran.models.User

class RegisterActivity : AppCompatActivity(), EmailFragment.Listener, NameFragment.Listener
    {

    private var mEmail: String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDataBase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        mDataBase = FirebaseDatabase.getInstance().reference

        if (savedInstanceState == null){
            supportFragmentManager.beginTransaction().add(R.id.frame_layout, EmailFragment())
                .commit()
        }

    }

    override fun onNext(email: String) {
        if (email.isNotEmpty()){

            mEmail = email
            mAuth.fetchSignInMethodsForEmail(email){signInMethods ->

                    if (signInMethods.isEmpty()){
                        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, NameFragment())
                            .addToBackStack(null)
                            .commit()
                    } else{
                        showToast("This email already exists")
                    }
            }
        } else{
            showToast("Please enter email")
        }
    }



    override fun onRegister(fullName: String, password: String) {
        if (fullName.isNotEmpty() && password.isNotEmpty()){
            val email = mEmail
                if (email != null){
                    mAuth.createUserWithEmailAndPassword(email, password)
                         {
                             mDataBase.createUser(it.user!!.uid, mkUser(fullName, email)){
                                 startHomeActivity()
                             }
                        }
                } else {
                    Log.d(TAG, "onRegister: email is null")
                    showToast("Please enter email")
                    supportFragmentManager.popBackStack()
                }
        } else {
            showToast("Please enter Full name and password")
        }
    }

    private fun unknownRegisterError(it: Task<out Any>) {
        Log.d(TAG, "failed to create user profile", it.exception)
        showToast("Something wrong happened: Please try again later")
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun mkUser(fullName: String, email: String): User{
        val username = mkUserName(fullName)
        return User(fullName, username = username, email = email)
    }

    private fun mkUserName(fullName: String) =
        fullName.toLowerCase().replace(" ", ".")

        private fun DatabaseReference.createUser(uid: String, user: User, onSuccess: () -> Unit){
            val reference = child("users").child(uid)
            reference.setValue(user).addOnCompleteListener {
                if (it.isSuccessful){
                    onSuccess()
                } else{
                    unknownRegisterError(it)
                }
            }
        }

        private fun FirebaseAuth.createUserWithEmailAndPassword(email: String, password: String,
                                                                onSuccess: (AuthResult) -> Unit){
            createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        onSuccess(it.result!!)
                    } else{
                        unknownRegisterError(it)
                    }
                }
        }

        private fun FirebaseAuth.fetchSignInMethodsForEmail(email: String, onSuccess: (List<String>) -> Unit){
            fetchSignInMethodsForEmail(email).addOnCompleteListener {
                if (it.isSuccessful){
                    onSuccess(it.result?.signInMethods ?: emptyList<String>())
                } else{
                    showToast(it.exception!!.message.toString())
                }
            }
        }
}

class EmailFragment : Fragment(){

    private lateinit var mListener: Listener

    interface Listener{
        fun onNext(email : String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        coordinateBtnAndInputs(next_btn, email_input)

        next_btn.setOnClickListener {
            val email = email_input.text.toString()
            mListener.onNext(email)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}

class NameFragment : Fragment(){

    private lateinit var mListener : Listener

    interface Listener{
        fun onRegister(fullName : String, password : String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_namepass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        coordinateBtnAndInputs(register_btn, full_name_input, password_input)

        register_btn.setOnClickListener {
            val fulName = full_name_input.text.toString()
            val password = password_input.text.toString()
            mListener.onRegister(fulName, password)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}
