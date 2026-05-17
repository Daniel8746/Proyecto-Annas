package com.annas.data.services.interceptors

interface NetworkMonitorService {
    fun isConnected(): Boolean
}