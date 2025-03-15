package app.bettermetesttask.domaincore.utils.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityManager {

    fun isNetworkAvailable(): Boolean

}