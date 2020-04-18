package com.slawomirbiernacki.garminwebproxy

import android.content.Context
import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import com.slawomirbiernacki.garminwebproxy.messagehandler.GarminMessageHandler

class GarminConnection(
    context: Context,
    private val messageHandler: GarminMessageHandler
) {
    var appID = "def6540badbe454d9e610c11e36ed476"  //TODO handle multiple apps
    private var connectIQ: ConnectIQ = ConnectIQ.getInstance(
        context,
        ConnectIQ.IQConnectType.WIRELESS
    )

    //TODO handle missing garmin connect app

    init {
        connectIQ.initialize(context, true, getSDKListener())
    }

    private fun getSDKListener(): ConnectIQ.ConnectIQListener {
        return object : ConnectIQ.ConnectIQListener {
            override fun onSdkReady() {
                Log.i(TAG, "Garmin SDK ready")
                val pairedDevices: List<IQDevice> = connectIQ.knownDevices

                if (pairedDevices.isEmpty()) {
                    Log.w(TAG, "No paired devices found")
                    return
                }

                for (device in pairedDevices) { //TODO handle multiple devices
                    val status: IQDevice.IQDeviceStatus = connectIQ.getDeviceStatus(device)
                    Log.d(TAG, "Found paired device: ${device.friendlyName}, status: $status")

                    if (status != IQDevice.IQDeviceStatus.CONNECTED) {
                        Log.w(TAG, "Device ${device.friendlyName} not connected, status: $status")
                        return
                    }

                    registerForMessages(connectIQ, device, appID)
                }
            }

            override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus) {
                Log.e(TAG, "Garmin connect error $status")
            }

            override fun onSdkShutDown() {
                Log.d(TAG, "Garmin connect shutdown")
            }
        }
    }

    fun registerForMessages(connectIQ: ConnectIQ, iqdevice: IQDevice, applicationID: String) {

        val appInfoListener = object : ConnectIQ.IQApplicationInfoListener {
            override fun onApplicationInfoReceived(iqapp: IQApp) {
                Log.i(TAG, "Found application: ${iqapp.applicationId}")

                connectIQ.registerForAppEvents(iqdevice, iqapp) { device, app, message, status ->
                    Log.d(
                        TAG,
                        "Message received, device: $device, app: ${app.applicationId}, status $status"
                    )
                    if (status == ConnectIQ.IQMessageStatus.SUCCESS) {
                        val response = messageHandler.handleMessage(message)
                        respond(response, device, app)
                    } else {
                        Log.w(TAG, "Message fetch unsuccessful, status: $status")
                    }
                }
            }

            override fun onApplicationNotInstalled(p0: String) {
                Log.w(TAG, "Application not installed")
            }
        }
        connectIQ.getApplicationInfo(applicationID, iqdevice, appInfoListener)
    }

    private fun respond(message: Any, device: IQDevice, app: IQApp) {
        connectIQ.sendMessage(
            device,
            app,
            message
        ) { iqDevice, iqApp, iqMessageStatus ->
            Log.i(
                TAG,
                "Responded to device ${iqDevice.friendlyName} and app: ${iqApp.displayName} with status: $iqMessageStatus"
            ) //TODO retry failures
        }
    }
}