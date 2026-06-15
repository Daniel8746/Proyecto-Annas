package com.annas.data.extensions

import kotlinx.collections.immutable.PersistentSet

fun PersistentSet<String>.toggle(item: String): PersistentSet<String> =
    if (item in this) removing(item) else adding(item)
