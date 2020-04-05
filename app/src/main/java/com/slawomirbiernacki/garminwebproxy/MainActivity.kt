package com.slawomirbiernacki.garminwebproxy

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.slawomirbiernacki.garminwebproxy.messagehandler.PingPongMessageHandler


const val TAG = "GarminWebProxy"


fun TextView.printLine(line: String) {
    this.append("\n$line")
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logsTextView = findViewById<TextView>(R.id.logs)
        logsTextView.movementMethod = ScrollingMovementMethod()

        //TODO should go into a service and be triggered from the receiver
        GarminConnection(applicationContext,
            PingPongMessageHandler()
        )
    }
}
