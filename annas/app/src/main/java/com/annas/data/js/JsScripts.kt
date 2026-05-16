package com.annas.data.js

object JsScripts {
    val AUTO_EXTRACT_AND_REDIRECT_SCRIPT by lazy {
        """
        (function() {
            if (window.__annasAutoExtractRunning) return;
            window.__annasAutoExtractRunning = true;

            let completed = false;
            let observer = null;
            let interval = null;

            function finish() {
                if (completed) return;

                completed = true;
                window.__annasAutoExtractRunning = false;

                if (observer) {
                    observer.disconnect();
                }

                if (interval) {
                    clearInterval(interval);
                }
            }

            function normalizeUrl(value) {
                if (!value) return "";

                const text = String(value).trim();
                const match = text.match(/https?:\/\/[^\s'"<>]+/i);

                return match ? match[0] : "";
            }

            function isVisible(el) {
                if (!el) return false;

                const style = window.getComputedStyle(el);

                return style.display !== "none"
                    && style.visibility !== "hidden"
                    && style.opacity !== "0"
                    && (el.offsetParent !== null || el.getClientRects().length > 0);
            }

            function redirect(url) {
                if (!url) return false;

                finish();

                window.location.href = url;

                return true;
            }

            function tryExtract() {
                if (completed) return true;

                const textNode = document.querySelector(
                    "span.break-all, code.break-all, pre.break-all, textarea, input[value^='http']"
                );

                const textUrl = textNode
                    ? normalizeUrl(textNode.value || textNode.textContent)
                    : "";

                if (textUrl && redirect(textUrl)) {
                    return true;
                }

                const urlHolder = document.querySelector(
                    "button[onclick*='http'], a[onclick*='http'], [data-clipboard-text*='http'], [data-copy*='http']"
                );

                if (urlHolder) {
                    const holderUrl = normalizeUrl(
                        urlHolder.getAttribute("onclick")
                            || urlHolder.getAttribute("data-clipboard-text")
                            || urlHolder.getAttribute("data-copy")
                    );

                    if (holderUrl && redirect(holderUrl)) {
                        return true;
                    }
                }

                const link = document.querySelector(
                    "p.font-bold a, .js-download-link, a[href*='get.php'], a[href*='/get/'], a[href*='/download/']"
                );

                if (link && isVisible(link)) {
                    const href = normalizeUrl(link.href || link.getAttribute("href"));

                    if (href && redirect(href)) {
                        return true;
                    }

                    finish();

                    link.removeAttribute("target");
                    link.click();

                    return true;
                }

                return false;
            }

            if (tryExtract()) return;

            let scheduled = false;

            observer = new MutationObserver(() => {
                if (scheduled) return;

                scheduled = true;

                setTimeout(() => {
                    scheduled = false;
                    tryExtract();

                }, 50);
            });

            const target = document.documentElement || document.body;

            if (!target) {
                window.__annasAutoExtractRunning = false;
                return;
            }

            observer.observe(
                target,
                {
                    childList: true,
                    subtree: true,
                    attributes: true,
                    characterData: true
                }
            );

            interval = setInterval(() => {
                if (completed) return;

                tryExtract();

            }, 150);

            setTimeout(() => {
                finish();

            }, 30000);
        })();
        """.trimIndent()
    }

    val HTML_CAPTURE_AND_SEND by lazy {
        """
        (function() {
            const selector = __SELECTOR__;

            if (window.__annasHtmlCaptureRunning === selector) return;
            window.__annasHtmlCaptureRunning = selector;

            let completed = false;
            let observer = null;
            let interval = null;

            function finish() {
                if (completed) return;

                completed = true;
                window.__annasHtmlCaptureRunning = null;

                if (observer) {
                    observer.disconnect();
                }

                if (interval) {
                    clearInterval(interval);
                }
            }

            function send(html) {
                finish();

                if (window.Android && Android.onHtmlReady) {
                    Android.onHtmlReady(html);
                }
            }

            function tryExtract() {
                if (completed) return true;

                let elements;

                try {
                    elements = document.querySelectorAll(selector);
                } catch (e) {
                    send("");
                    return true;
                }

                if (elements.length > 0) {
                    const html =
                        Array.from(elements)
                            .map(el => el.outerHTML)
                            .join("");

                    send(html);

                    return true;
                }

                return false;
            }

            if (tryExtract()) return;

            let scheduled = false;

            observer = new MutationObserver(() => {
                if (scheduled) return;

                scheduled = true;

                setTimeout(() => {
                    scheduled = false;
                    tryExtract();

                }, 50);
            });

            const target = document.documentElement || document.body;

            if (!target) {
                window.__annasHtmlCaptureRunning = null;
                return;
            }

            observer.observe(
                target,
                {
                    childList: true,
                    subtree: true,
                    attributes: true,
                    characterData: true
                }
            );

            interval = setInterval(() => {
                if (completed) return;

                tryExtract();

            }, 150);

            setTimeout(() => {
                finish();

            }, __TIMEOUT__);
        })();
        """.trimIndent()
    }

    val DOM_HTML_COLLECTOR by lazy {
        """
        (function(){
            const selector = __SELECTOR__;

            function safe() {
                if (!selector) return "";

                try {
                    const el = document.querySelectorAll(selector);

                    if (!el || el.length === 0) return "";

                    return Array.from(el)
                        .map(d => d.outerHTML)
                        .join("");

                } catch (e) {
                    return "";
                }
            }

            return safe();
        })();
        """.trimIndent()
    }
}
