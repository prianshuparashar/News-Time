package com.prianshuparashar.newstime.common.networkhelper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.prianshuparashar.newstime.di.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkHelperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : NetworkHelper {
    override fun isNetworkConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}