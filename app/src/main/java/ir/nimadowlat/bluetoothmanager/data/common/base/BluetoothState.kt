package ir.nimadowlat.bluetoothmanager.data.common.base


sealed class BluetoothState{
    object Scanning : BluetoothState()
    object ConnectionFail : BluetoothState()
    object Connected : BluetoothState()
    object NotConnected : BluetoothState()
    object NeedPermission : BluetoothState()
}
