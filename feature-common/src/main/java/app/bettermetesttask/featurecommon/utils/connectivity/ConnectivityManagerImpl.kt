package app.bettermetesttask.featurecommon.utils.connectivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import app.bettermetesttask.domaincore.utils.connectivity.ConnectivityManager
import app.bettermetesttask.domaincore.utils.connectivity.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ConnectivityManagerImpl @Inject constructor(private val context: Context) : ConnectivityManager {

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    override fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService<android.net.ConnectivityManager>()
        val activeNetwork: NetworkInfo? = cm?.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

}