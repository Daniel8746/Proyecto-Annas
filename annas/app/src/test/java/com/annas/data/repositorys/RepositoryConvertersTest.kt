package com.annas.data.repositorys

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositoryConvertersTest {

    @Test
    fun toLibrosParsesCurrentSearchCardShape() {
        val html = """
            <div class="flex pt-3 pb-3 border-b">
                <div>
                    <a class="text-lg" href="/md5/abc">Clean Code</a>
                    <a class="text-sm" href="/search?q=Robert">Robert C. Martin</a>
                    <img data-src="/covers/clean-code.jpg">
                    <div class="text-gray-800 font-semibold text-sm">
                        English [en] \u00B7 PDF \u00B7 5.2MB
                    </div>
                </div>
            </div>
        """.trimIndent()

        val books = Jsoup.parseBodyFragment(html).body().children().toLibros()

        assertEquals(1, books.size)
        assertEquals("/md5/abc", books.first().enlace)
        assertEquals("Clean Code", books.first().titulo)
        assertEquals("Robert C. Martin", books.first().autor)
        assertEquals("/covers/clean-code.jpg", books.first().portada)
        assertEquals("English [en]", books.first().idioma)
        assertEquals("PDF", books.first().formato)
        assertEquals("5.2MB", books.first().tamano)
    }

    @Test
    fun toLibrosKeepsResultWhenAuthorOrImageIsMissing() {
        val html = """
            <div class="border-b pt-3">
                <a href="/md5/def">Domain Driven Design</a>
                <div class="font-semibold text-sm">
                    Spanish [es] \u00B7 EPUB \u00B7 1.8 MB
                </div>
            </div>
        """.trimIndent()

        val books = Jsoup.parseBodyFragment(html).body().children().toLibros()

        assertEquals(1, books.size)
        assertEquals("Domain Driven Design", books.first().titulo)
        assertEquals("Desconocido", books.first().autor)
        assertEquals("", books.first().portada)
        assertEquals("Spanish [es]", books.first().idioma)
        assertEquals("EPUB", books.first().formato)
        assertEquals("1.8 MB", books.first().tamano)
    }

    @Test
    fun toLibrosRemovesDuplicateCardsFromBroadSelectors() {
        val html = """
            <div class="border-b pt-3">
                <a href="/md5/abc">Clean Code</a>
                <div class="font-semibold text-sm">English [en] \u00B7 PDF \u00B7 5MB</div>
            </div>
            <div class="border-b py-3">
                <a href="/md5/abc">Clean Code duplicate</a>
                <div class="font-semibold text-sm">English [en] \u00B7 PDF \u00B7 5MB</div>
            </div>
        """.trimIndent()

        val books = Jsoup.parseBodyFragment(html).body().children().toLibros()

        assertEquals(1, books.size)
    }
}
