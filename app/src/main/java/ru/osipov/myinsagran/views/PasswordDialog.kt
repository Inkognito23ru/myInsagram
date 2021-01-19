package ru.osipov.myinsagran.views

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_password.view.*
import ru.osipov.myinsagran.R

class PasswordDialog : DialogFragment() {

    private lateinit var mListener: Listener

    interface Listener{
       fun onPasswordConfirm(password: String)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        mListener = activity as Listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_password, null)
        return AlertDialog.Builder(context!!)
            .setView(view)
            .setPositiveButton(android.R.string.ok, {_, _ ->
                    mListener.onPasswordConfirm(view.password_input.text.toString())
            })
            .setNegativeButton(android.R.string.cancel, {_, _ ->

            })
            .setTitle(R.string.please_enter_password)
            .create()
    }
}