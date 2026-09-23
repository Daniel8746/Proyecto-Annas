package com.annas.ua

import android.content.Context
import android.os.Build
import android.webkit.WebSettings
import androidx.annotation.RequiresApi

object MultiBrandUserAgentProvider {

    /**
     * Devuelve el User-Agent nativo y real del dispositivo (Samsung, Xiaomi, etc.).
     * Al ser el oficial del sistema, coincide al 100% con la huella TLS y salta Cloudflare.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun get(context: Context): String {
        return try {
            // El sistema genera el User-Agent perfecto de forma nativa
            WebSettings.getDefaultUserAgent(context)
                .replace("; wv", "")
                .replace("Version/4.0 ", "")
        } catch (_: Exception) {
            // Respaldo simple en caso de llamarse fuera del hilo principal
            "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MANUFACTURER} ${Build.MODEL}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
        }
    }

    /**
     * Extrae dinámicamente la versión de Chrome del User-Agent nativo
     * y genera la cabecera Sec-Ch-Ua perfecta para este dispositivo móvil.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun getSecChUa(context: Context): String {
        val ua = get(context)

        // Buscamos "Chrome/XX." en el User-Agent para sacar la versión mayor
        val index = ua.indexOf("Chrome/")
        val version = if (index != -1) {
            val start = index + 7
            val end = ua.indexOf(".", start)
            if (end != -1) ua.substring(start, end) else "128"
        } else {
            "128" // Fallback si no encuentra la cadena
        }

        // Devolvemos el formato exacto que piden los navegadores móviles basados en Chromium
        return "\"Chromium\";v=\"$version\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"$version\""
    }
}