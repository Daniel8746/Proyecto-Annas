package com.pmdm.annas.data.js

object JsScripts {
    val AUTO_EXTRACT_AND_REDIRECT_SCRIPT by lazy {
        """
        (function() {
    
            let completed = false;
    
            let observer = null;
    
            let interval = null;
    
            function finish() {
    
                if (completed) return;
    
                completed = true;
    
                if (observer) {
                    observer.disconnect();
                }
    
                if (interval) {
                    clearInterval(interval);
                }
            }
    
            function redirect(url) {
    
                finish();
    
                window.location.replace(url);
            }
    
            function tryExtract() {
    
                if (completed) return true;
    
                const span =
                    document.querySelector('span.break-all');
    
                if (span) {
    
                    const text = span.innerText.trim();
    
                    if (text.startsWith('http')) {
    
                        redirect(text);
    
                        return true;
                    }
                }
    
                const copyBtn =
                    document.querySelector(
                        'button[onclick*="http"]'
                    );
    
                if (copyBtn) {
    
                    const match =
                        copyBtn
                            .getAttribute('onclick')
                            ?.match(/https?:\/\/[^']+/);
    
                    if (match) {
    
                        redirect(match[0]);
    
                        return true;
                    }
                }
    
                const btn =
                    document.querySelector('p.font-bold a')
                    || document.querySelector('.js-download-link');
    
                if (
                    btn &&
                    btn.offsetParent !== null
                ) {
    
                    finish();
    
                    btn.click();
    
                    return true;
                }
    
                return false;
            }
    
            // intento inmediato
            if (tryExtract()) return;
    
            // observer
            observer = new MutationObserver(() => {
                tryExtract();
            });
    
            observer.observe(document.documentElement, {
                childList: true,
                subtree: true
            });
    
            // polling híbrido
            interval = setInterval(() => {
    
                if (completed) return;
    
                tryExtract();
    
            }, 250);
    
            // timeout duro
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
    
            let completed = false;
    
            let observer = null;
    
            let interval = null;
    
            function finish() {
    
                if (completed) return;
    
                completed = true;
    
                if (observer) {
                    observer.disconnect();
                }
    
                if (interval) {
                    clearInterval(interval);
                }
            }
    
            function send(html) {
    
                finish();
    
                if (window.Android?.onHtmlReady) {
                    Android.onHtmlReady(html);
                }
            }
    
            function tryExtract() {
    
                if (completed) return true;
    
                const elements =
                    document.querySelectorAll(selector);
    
                if (elements.length > 0) {
    
                    const html =
                        Array.from(elements)
                            .map(el => el.outerHTML)
                            .join('');
    
                    send(html);
    
                    return true;
                }
    
                return false;
            }
    
            // intento inmediato
            if (tryExtract()) return;
    
            // observer
            observer = new MutationObserver(() => {
                tryExtract();
            });
    
            observer.observe(
                document.documentElement,
                {
                    childList: true,
                    subtree: true
                }
            );
    
            // polling híbrido
            interval = setInterval(() => {
    
                if (completed) return;
    
                tryExtract();
    
            }, 250);
    
            // timeout duro
            setTimeout(() => {
    
                if (!completed) {
    
                    send("");
    
                }
    
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
                        .join('');
    
                } catch (e) {
    
                    return "";
    
                }
            }
    
            return safe();
    
        })();
        """.trimIndent()
    }
}