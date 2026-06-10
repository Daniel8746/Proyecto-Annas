package com.annas.data.services.interceptors

interface NetworkMonitor {
    fun isConnected(): Boolean
}