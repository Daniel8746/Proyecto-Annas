package com.annas.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadUtilsTest {

    @Test
    fun isDirectDetectsFileAndServerUrls() {
        assertTrue(isDirect("https://example.org/book.epub?download=1"))
        assertTrue(isDirect("https://libgen.example/get.php?md5=abc"))
        assertTrue(isDirect("https://files.example.org/download/book.pdf"))
    }

    @Test
    fun isDirectDoesNotTreatAnnasSlowDownloadPageAsFile() {
        assertFalse(isDirect("https://annas-archive.org/slow_download/abc"))
    }

    @Test
    fun getMimeMapsKnownBookFormats() {
        assertEquals("application/pdf", getMime("book.pdf"))
        assertEquals("application/epub+zip", getMime("book.epub"))
        assertEquals("application/x-mobipocket-ebook", getMime("book.azw3"))
        assertEquals("application/octet-stream", getMime("book.unknown"))
    }
}
