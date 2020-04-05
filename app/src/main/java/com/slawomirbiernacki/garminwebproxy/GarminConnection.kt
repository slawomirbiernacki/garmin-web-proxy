package com.slawomirbiernacki.garminwebproxy

import android.content.Context
import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice

class GarminConnection(private val context: Context) {

    init {
        connect()
    }

    private fun connect() {
        val connectIQ = ConnectIQ.getInstance(
            context,
            ConnectIQ.IQConnectType.WIRELESS
        )
        connectIQ.initialize(context, true, getSDKListener(connectIQ))

    }

    fun getSDKListener(connectIQ: ConnectIQ): ConnectIQ.ConnectIQListener {
        return object : ConnectIQ.ConnectIQListener {
            override fun onSdkReady() {
                Log.d(TAG, "Garmin SDK ready")
                val paired: List<IQDevice>? = connectIQ.knownDevices

                if (paired != null && paired.isNotEmpty()) { // get the status of the devices
                    for (device in paired) { //handle multiple devices
                        val status: IQDevice.IQDeviceStatus = connectIQ.getDeviceStatus(device)
                        println("Found paired device: ${device.friendlyName}")
                        if (status == IQDevice.IQDeviceStatus.CONNECTED) { // Work with the device
                            println("Found connected device: ${device.friendlyName}")
                            testApp(connectIQ, device)
                        }
                    }
//                    println("No paired")
                } else {
                    println("No paired devices")
                }
            }

            override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus) {
                println("Garmin connect error $status")
            }

            override fun onSdkShutDown() {
                println("Garmin connect shutdown")
            }
        }
    }

    fun testApp(connectIQ: ConnectIQ, iqdevice: IQDevice) {

        val listener = object : ConnectIQ.IQApplicationInfoListener {
            override fun onApplicationInfoReceived(iqapp: IQApp) {
                println("Found application: " + iqapp.applicationId)


                val appEventsListener =
                    ConnectIQ.IQApplicationEventListener { device, app, message, status ->
                        println("Message $status: $message")

                        connectIQ.sendMessage(
                            iqdevice,
                            iqapp,
                            "pong"
                        ) { iqDevice, iqApp, iqMessageStatus ->
                            println("Responded! $iqMessageStatus")
                        }
                    }

                connectIQ.registerForAppEvents(iqdevice, iqapp, appEventsListener)
            }

            override fun onApplicationNotInstalled(p0: String) {
                println("Not installed")
            }

        }


        connectIQ.getApplicationInfo("def6540badbe454d9e610c11e36ed476", iqdevice, listener)
    }
}