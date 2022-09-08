package ir.nimadowlat.bluetoothmanager.presentation.main

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.*
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nimadowlat.bluetoothmanager.data.common.base.BluetoothState
import ir.nimadowlat.bluetoothmanager.data.main.DaggerMainComponent
import ir.nimadowlat.bluetoothmanager.infra.utils.ConnectThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@SuppressLint("StaticFieldLeak")
@HiltViewModel
class MainViewModel
@Inject
constructor(private val context: Context) : ViewModel() {

    var deviceManager: CompanionDeviceManager? = null

    private val _bluetoothState: MutableStateFlow<BluetoothState> =
        MutableStateFlow(BluetoothState.NotConnected)
    val bluetoothState: StateFlow<BluetoothState> = _bluetoothState

    private val bluetoothManager: BluetoothManager =
        getSystemService(context, BluetoothManager::class.java)!!
    private var bluetoothAdapter = bluetoothManager.adapter



    private lateinit var connectThread: ConnectThread

    private val deviceFoundReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    bluetoothAdapter.cancelDiscovery()
                    context.unregisterReceiver(this)
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        connect(device)
                    } else {
                        _bluetoothState.value = BluetoothState.ConnectionFail
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _bluetoothState.value = BluetoothState.Scanning

                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    context.unregisterReceiver(this)
                    disconnect()
                }
            }
        }
    }

    private val scanModeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getIntExtra(
                    BluetoothAdapter.EXTRA_SCAN_MODE,
                    0
                ) == BluetoothAdapter.SCAN_MODE_CONNECTABLE ||
                intent.getIntExtra(
                    BluetoothAdapter.EXTRA_SCAN_MODE,
                    0
                ) == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
            ) {
                context.unregisterReceiver(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    scanOnHighAPIVersion()
                } else {
                    registerDeviceFoundReceivers()
                    bluetoothAdapter.startDiscovery()
                }

            }
        }
    }


    private val bondDeviceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val status: Int = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0)
                    if (status == BluetoothDevice.BOND_BONDED) {
                        context.unregisterReceiver(this)
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        if (device != null) {
                            _bluetoothState.value = BluetoothState.Connected
                            connectThread =
                                ConnectThread(device, object : ConnectThread.ConnectThreadListener {
                                    override fun connected() {
                                    }

                                    override fun connectionFailed() {
                                    }
                                })
                            connectThread.run()
                        }
                    }
                }
            }
        }
    }


    private fun registerDeviceFoundReceivers() {
        context.registerReceiver(
            deviceFoundReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        context.registerReceiver(
            deviceFoundReceiver,
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        )
        context.registerReceiver(
            deviceFoundReceiver,
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        )
    }

    private fun checkBluetooth(): Boolean {

        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth is unavailable", Toast.LENGTH_LONG).show()
            return false
        } else if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtIntent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(enableBtIntent)
            return false
        } else if (!bluetoothAdapter.isDiscovering) {
            context.registerReceiver(
                scanModeReceiver,
                IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
            )
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            discoverableIntent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(discoverableIntent)
            return false
        }
        return true
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }

        }
        return true
    }

    fun setToNotConnected() {
        _bluetoothState.value = BluetoothState.NotConnected
    }
    fun scan() = viewModelScope.launch {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkPermissions()) {
                if (checkBluetooth()) {
                    scanOnHighAPIVersion()
                }
            } else {
                _bluetoothState.value = BluetoothState.NeedPermission
            }
        } else {
            try {
                if (checkBluetooth()) {
                    registerDeviceFoundReceivers()
                    bluetoothAdapter.startDiscovery()
                }
            } catch (e: SecurityException) {
                _bluetoothState.value = BluetoothState.NeedPermission
            }
        }
    }


    private fun scanOnHighAPIVersion() {

        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .setSingleDevice(true)
            .build()


        val mainComponent = DaggerMainComponent.builder().build()
        mainComponent.inject(deviceManager)


        deviceManager?.associate(
            pairingRequest,
            object : CompanionDeviceManager.Callback() {
                @Deprecated("Deprecated in Java")
                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    val device: BluetoothDevice? =
                        (chooserLauncher as Intent).getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        connect(device)
                    } else {
                        _bluetoothState.value = BluetoothState.ConnectionFail
                    }
                }

                override fun onAssociationCreated(associationInfo: AssociationInfo) {
                    super.onAssociationCreated(associationInfo)
                    _bluetoothState.value = BluetoothState.Scanning
                }

                override fun onAssociationPending(intentSender: IntentSender) {
                    val device: BluetoothDevice? =
                        (intentSender as Intent).getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        connect(device)
                    } else {
                        _bluetoothState.value = BluetoothState.ConnectionFail
                    }
                }

                override fun onFailure(error: CharSequence?) {
                    disconnect()
                }
            }, null
        )
    }


    fun disconnect() = viewModelScope.launch {
        if (::connectThread.isInitialized && connectThread.isConnected()) {
            connectThread.cancel()
        }
        _bluetoothState.value = BluetoothState.ConnectionFail
    }

    fun cancel() = viewModelScope.launch {
        _bluetoothState.value = BluetoothState.ConnectionFail
        bluetoothAdapter.cancelDiscovery()
    }

    private fun connect(device: BluetoothDevice) = viewModelScope.launch {
        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            _bluetoothState.value = BluetoothState.Connected
            connectThread =
                ConnectThread(device, object : ConnectThread.ConnectThreadListener {
                    override fun connected() {
                    }

                    override fun connectionFailed() {
                    }
                })
            connectThread.run()
        } else {
            context.registerReceiver(
                bondDeviceReceiver,
                IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            )
            device.createBond()
        }
    }
}