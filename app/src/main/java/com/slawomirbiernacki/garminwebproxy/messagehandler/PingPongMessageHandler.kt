package com.slawomirbiernacki.garminwebproxy.messagehandler

import android.util.Log
import com.slawomirbiernacki.garminwebproxy.TAG

class PingPongMessageHandler :
    GarminMessageHandler {
    override fun handleMessage(message: Any): Any {
        Log.i(TAG, "Received message $message")
        Log.e(TAG, "Responding pong")
        return "pong"
    }
}