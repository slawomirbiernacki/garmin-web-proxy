package com.slawomirbiernacki.garminwebproxy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.ConnectIQ.*
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice

const val TAG = "GarminWebProxy"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val connectIQ = getInstance(
            applicationContext,
            IQConnectType.WIRELESS
        )

        val listener = object : ConnectIQListener {
            override fun onSdkReady() {
                println("Connected to Garmin connect")

                val paired: List<IQDevice>? = connectIQ.knownDevices

                if (paired != null && paired.isNotEmpty()) { // get the status of the devices
                    for (device in paired) {
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

            override fun onInitializeError(status: IQSdkErrorStatus) {
                println("Garmin connect error $status")
            }

            override fun onSdkShutDown() {
                println("Garmin connect shutdown")
            }
        }

        connectIQ.initialize(applicationContext, true, listener)
    }


    fun testApp(connectIQ: ConnectIQ, iqdevice: IQDevice) {

        val listener = object : IQApplicationInfoListener {
            override fun onApplicationInfoReceived(iqapp: IQApp) {
                println("Found application: " + iqapp.applicationId)


                val appEventsListener =
                    IQApplicationEventListener { device, app, message, status ->
                        println("Message $status: $message")

                        connectIQ.sendMessage(iqdevice, iqapp, "pong"
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
