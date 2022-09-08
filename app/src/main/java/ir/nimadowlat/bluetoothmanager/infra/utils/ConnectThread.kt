package ir.nimadowlat.bluetoothmanager.infra.utils

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.ParcelUuid
import java.io.IOException

class ConnectThread(device: BluetoothDevice, mListener:ConnectThreadListener) : Thread() {

    var listener:ConnectThreadListener = mListener
    var connected:Boolean =false

    interface ConnectThreadListener{
        fun connected()
        fun connectionFailed()
    }

    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(
            ParcelUuid.fromString("00001101-0000-1000-8000-00805F9B34FB").uuid
        )
    }

    override fun run() {

        try {
            mmSocket?.connect()
            listener.connected()
            connected  =true
        }catch (e:IOException){
            e.printStackTrace()
            cancel()
        }

        if (!connected){
            listener.connectionFailed()
        }
    }

    fun isConnected():Boolean{
        return connected
    }

    fun cancel() {
        try {
            mmSocket?.close()
            connected  = false
        } catch (e: IOException){
            e.printStackTrace()
        }
    }
}