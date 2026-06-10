package com.annas.data.extensions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

inline fun <T> MutableStateFlow<T>.updateState(
    block: T.() -> T
) {
    update(block)
}