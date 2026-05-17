package com.annas.data.exceptions

import okio.IOException

class NoNetworkException(mensaje: String) : IOException(mensaje)