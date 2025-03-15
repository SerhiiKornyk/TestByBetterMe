package app.bettermetesttask.domaincore.utils.connectivity

sealed class NetworkState {
    object Available : NetworkState()
    object Unavailable : NetworkState()
    object Init : NetworkState()
}