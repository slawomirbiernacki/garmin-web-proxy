package com.slawomirbiernacki.garminwebproxy.messagehandler

interface GarminMessageHandler {

    fun handleMessage(message: Any): Any
}

