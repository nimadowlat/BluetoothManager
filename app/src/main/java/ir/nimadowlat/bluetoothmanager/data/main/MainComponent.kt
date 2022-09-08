package ir.nimadowlat.bluetoothmanager.data.main

import android.companion.CompanionDeviceManager
import dagger.Component

@Component(modules = [MainModule::class])
interface MainComponent {
    fun inject(manger:CompanionDeviceManager?)
}