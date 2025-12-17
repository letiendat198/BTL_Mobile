package com.ptit.btl_mobile.model.party.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter


object HostInfo {
    fun getHostIP(context: Context): String {
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip: String = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress())
        return ip
    }
}
