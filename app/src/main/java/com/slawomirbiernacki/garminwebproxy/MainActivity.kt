package com.slawomirbiernacki.garminwebproxy

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.slawomirbiernacki.garminwebproxy.messagehandler.PingPongMessageHandler


const val TAG = "GarminWebProxy"
const val APPLICATION_ID_KEY = "ApplicationID"


fun TextView.printLine(line: String) {
    this.append("\n$line")
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "Creating!")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logsTextView = findViewById<TextView>(R.id.logs)
        logsTextView.movementMethod = ScrollingMovementMethod()

        val idInput = findViewById<EditText>(R.id.application_id_input)
        idInput.setOnEditorActionListener(TextView.OnEditorActionListener { textView: TextView, i: Int, keyEvent: KeyEvent? ->
            registerAppId(idInput.text.toString())
            return@OnEditorActionListener true
        })

        idInput.setText(recoverApplicationId())

        val button = findViewById<Button>(R.id.register_application_id_button)
        button.setOnClickListener { v ->
            registerAppId(idInput.text.toString())
        }

        //TODO should go into a service and be triggered from the receiver
        GarminConnection(
            applicationContext,
            PingPongMessageHandler()
        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.i(TAG, "Restoring!")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i(TAG, "Restarting!")
    }


    private fun registerAppId(applicationId: String) {
        Log.i(TAG, "Registering application_id: $applicationId")
        storeApplicationId(applicationId)
    }

    private fun recoverApplicationId(): String {
        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val applicationID = sharedPref.getString(APPLICATION_ID_KEY, "").toString()
        Log.i(TAG, "recovered application id: $applicationID")
        return applicationID
    }

    private fun storeApplicationId(id: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(APPLICATION_ID_KEY, id)
            commit()
        }
    }
}
