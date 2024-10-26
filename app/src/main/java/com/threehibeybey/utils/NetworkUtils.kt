package com.threehibeybey.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest

/**
 * Utility class to handle network connectivity status.
 */
object NetworkUtils {

    /**
     * Registers a callback to monitor network connectivity changes.
     * @param context The application context.
     * @param onAvailable Callback when network becomes available.
     * @param onLost Callback when network is lost.
     */
    fun registerNetworkCallback(
        context: Context,
        onAvailable: () -> Unit,
        onLost: () -> Unit
    ) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    onAvailable()
                }

                override fun onLost(network: Network) {
                    onLost()
                }
            }
        )
    }
}
