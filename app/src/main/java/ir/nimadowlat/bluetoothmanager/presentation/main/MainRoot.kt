package ir.nimadowlat.bluetoothmanager.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.nimadowlat.bluetoothmanager.R
import ir.nimadowlat.bluetoothmanager.data.common.base.BluetoothState


@Composable
fun mainActivityView(
    activity: MainActivity,
    viewModel: MainViewModel
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = colorResource(id = R.color.munsellBlue),
    ) {
        val state = viewModel.bluetoothState.collectAsState().value

        if (state == BluetoothState.NeedPermission){
            activity.checkPermissions()
        }

        val buttonTextStringId = getButtonTextStringId(state)
        val statusTextStringId = getStatusTextStringId(state)
        val statusTextColorId = getStatusTextColorId(state)
        val statusModifier = getStatusModifier(state)
        val statusColorId = getStatusColorId(state)

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .aspectRatio(1.0f),
                color = colorResource(id = R.color.munsellBlue)
            ) {
                Surface(
                    modifier = statusModifier,
                    color = colorResource(id = statusColorId)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = statusTextStringId),
                            fontSize = 18.sp,
                            style = TextStyle(
                                color = colorResource(id = statusTextColorId),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )

                    }
                }
            }
            Spacer(modifier = Modifier.size(30.dp))

            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(80.dp, 40.dp)
                    .clip(
                        RoundedCornerShape(999.dp)
                    )
                    .background(colorResource(id = R.color.citrine)),
                onClick = {
                    when (state) {
                        is BluetoothState.NotConnected -> {
                            viewModel.scan()
                        }
                        is BluetoothState.Scanning -> {
                            viewModel.cancel()
                        }
                        is BluetoothState.Connected -> {
                            viewModel.disconnect()
                        }
                        is BluetoothState.ConnectionFail -> {
                            viewModel.scan()
                        }
                        is BluetoothState.NeedPermission -> {
                            viewModel.scan()
                        }
                    }
                }
            ) {
                Text(
                    text = stringResource(id = buttonTextStringId),
                    fontSize = 16.sp,
                    style = TextStyle(
                        color = colorResource(id = R.color.graniteGray),
                        textAlign = TextAlign.Center
                    )

                )
            }
        }
    }

}

fun getButtonTextStringId(state: BluetoothState): Int {
    return when (state) {
        is BluetoothState.NotConnected -> R.string.scan
        is BluetoothState.Scanning -> R.string.cancel
        is BluetoothState.Connected -> R.string.disconnect
        is BluetoothState.ConnectionFail -> R.string.retry
        is BluetoothState.NeedPermission -> R.string.scan
    }
}

@Composable
fun getStatusModifier(state: BluetoothState): Modifier {
    return when (state) {
        is BluetoothState.NotConnected -> Modifier
            .fillMaxSize()
            .padding(80.dp)
            .clip(
                CircleShape
            )
        is BluetoothState.Scanning -> Modifier
            .fillMaxSize()
            .padding(15.dp)
            .border(2.dp, colorResource(id =  R.color.aureolin3) , CircleShape)
            .padding(35.dp)
            .border(2.dp, colorResource(id =  R.color.aureolin2) , CircleShape)
            .padding(40.dp)
            .border(2.dp, colorResource(id =  R.color.aureolin) , CircleShape)
        is BluetoothState.Connected -> Modifier
            .fillMaxSize()
            .padding(80.dp)
            .clip(
                CircleShape
            )
        is BluetoothState.ConnectionFail -> Modifier
            .fillMaxSize()
            .padding(80.dp)
            .clip(
                CircleShape
            )
        is BluetoothState.NeedPermission -> Modifier
            .fillMaxSize()
            .padding(80.dp)
            .clip(
                CircleShape
            )
    }
}

fun getStatusColorId(state: BluetoothState): Int {
    return when (state) {
        is BluetoothState.NotConnected -> R.color.red
        is BluetoothState.Scanning -> R.color.munsellBlue
        is BluetoothState.Connected -> R.color.aureolin
        is BluetoothState.ConnectionFail -> R.color.red
        is BluetoothState.NeedPermission -> R.color.red
    }
}

fun getStatusTextStringId(state: BluetoothState): Int {
    return when (state) {
        is BluetoothState.NotConnected -> R.string.not_connected
        is BluetoothState.Scanning -> R.string.scanning
        is BluetoothState.Connected -> R.string.connected
        is BluetoothState.ConnectionFail -> R.string.connection_fail
        is BluetoothState.NeedPermission -> R.string.not_connected
    }
}

fun getStatusTextColorId(state: BluetoothState): Int {
    return when (state) {
        is BluetoothState.NotConnected -> R.color.white
        is BluetoothState.Scanning -> R.color.aureolin
        is BluetoothState.Connected -> R.color.graniteGray
        is BluetoothState.ConnectionFail -> R.color.white
        is BluetoothState.NeedPermission -> R.color.white
    }
}

fun getStateMessage(state: BluetoothState): String {
    return when (state) {
        is BluetoothState.NotConnected -> "NotConnected"
        is BluetoothState.Scanning -> "Scanning"
        is BluetoothState.Connected -> "Connected"
        is BluetoothState.ConnectionFail -> "ConnectionFail"
        is BluetoothState.NeedPermission -> "NeedPermission"
    }
}
