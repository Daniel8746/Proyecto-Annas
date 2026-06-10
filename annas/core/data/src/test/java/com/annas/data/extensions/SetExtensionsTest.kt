package com.annas.data.extensions

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetExtensionsTest {

    @Test
    fun persistentToggleKeepsPersistentSetType() {
        val enabled: PersistentSet<String> = persistentSetOf<String>().toggle("epub")

        assertTrue("epub" in enabled)

        val disabled: PersistentSet<String> = enabled.toggle("epub")

        assertFalse("epub" in disabled)
    }
}
