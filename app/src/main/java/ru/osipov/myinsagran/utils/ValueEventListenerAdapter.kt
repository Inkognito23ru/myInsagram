package ru.osipov.myinsagran.utils

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import ru.osipov.myinsagran.activities.TAG

class ValueEventListenerAdapter(val handler : (DataSnapshot) -> Unit) :
    ValueEventListener {

    override fun onDataChange(data: DataSnapshot) {
        handler(data)
    }

    override fun onCancelled(error: DatabaseError) {
        Log.d(
            TAG,
            "Error",
            error.toException()
        )
    }
}