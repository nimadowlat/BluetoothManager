package ir.nimadowlat.bluetoothmanager.data.main

import android.companion.CompanionDeviceManager
import android.content.Context
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object MainModule {

    @Provides
    @ActivityScoped
    fun providesCompanionDeviceManager(@ActivityContext context: Context): CompanionDeviceManager? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val deviceManager: CompanionDeviceManager by lazy {
                context.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
            }

            return deviceManager
        }
        return null
    }


}