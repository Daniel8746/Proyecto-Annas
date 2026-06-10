package com.annas.data.extensions

import kotlinx.collections.immutable.PersistentSet

fun Set<String>.toggle(item: String): Set<String> =
    if (item in this) this - item else this + item

fun PersistentSet<String>.toggle(item: String): PersistentSet<String> =
    if (item in this) remove(item) else add(item)
