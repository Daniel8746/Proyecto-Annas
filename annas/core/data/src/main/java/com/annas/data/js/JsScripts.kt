package com.annas.data.js

object JsScripts {
    val AUTO_EXTRACT_AND_REDIRECT_SCRIPT by lazy {
        """
        (function () {
            
            // Evita doble instancia
            if (window.__annasExtractorInstalled) {
                return;
            }
        
            window.__annasExtractorInstalled = true;
        
            let finished = false;
            let observer = null;
            let valor = -1;

            function cleanup() {
    
                if (observer) {
                    observer.disconnect();
                    observer = null;
                }
            
                delete window.__annasExtractorInstalled;
            }
            
            function tryTiempoEspera() {
                const el = document.querySelector(".js-partner-countdown");
            
                if (!el) return false;
            
                const text = el.textContent.trim();
                const match = text.match(/\d+/);
            
                valor = match ? parseInt(match[0], 10) : -1;
                
                if (window.Android && window.Android.obtenerTiempoEspera) {
                    window.Android.obtenerTiempoEspera(valor);
                }
            
                return true;
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
        
                return (
                    style.display !== "none" &&
                    style.visibility !== "hidden" &&
                    style.opacity !== "0" &&
                    (el.offsetParent !== null || el.getClientRects().length > 0)
                );
            }
        
            function redirect(url) {
                if (!url || finished) {
                    return false;
                }
            
                finished = true;
            
                cleanup();
            
                location.replace(url);
            
                return true;
            }
        
            function extractFromText() {
        
                const node = document.querySelector(
                    "span.break-all, code.break-all, pre.break-all, textarea, input[value^='http']"
                );
        
                if (!node) return false;
        
                const url = normalizeUrl(
                    node.value || node.textContent
                );
        
                return redirect(url);
            }
        
            function extractFromAttributes() {
        
                const holder = document.querySelector(
                    "button[onclick*='http'], " +
                    "a[onclick*='http'], " +
                    "[data-clipboard-text*='http'], " +
                    "[data-copy*='http']"
                );
        
                if (!holder) return false;
        
                const url = normalizeUrl(
                    holder.getAttribute("onclick") ||
                    holder.getAttribute("data-clipboard-text") ||
                    holder.getAttribute("data-copy")
                );
        
                return redirect(url);
            }
        
            function extractFromLinks() {
        
                const link = document.querySelector(
                    "p.font-bold a, " +
                    ".js-download-link, " +
                    "a[href*='get.php'], " +
                    "a[href*='/get/'], " +
                    "a[href*='/download/']"
                );
        
                if (!link || !isVisible(link)) {
                    return false;
                }
        
                const href = normalizeUrl(
                    link.href || link.getAttribute("href")
                );
        
                if (href) {
                    return redirect(href);
                }
        
                cleanup();
        
                link.removeAttribute("target");
                link.click();
        
                return true;
            }
        
            function tryExtract() {
                if (finished) {
                    return true;
                }
                
                return (
                    extractFromText() ||
                    extractFromAttributes() ||
                    extractFromLinks()
                );
            }
                                
            // Intento inmediato
            if (tryExtract()) {
                return;
            }
            tryTiempoEspera();
        
            // Observer reactivo
            let scheduled = false;
        
            observer = new MutationObserver(() => {
        
                if (scheduled || finished) {
                    return;
                }
        
                scheduled = true;
        
                setTimeout(() => {
    
                    scheduled = false;
                
                    tryExtract();
                    
                    if (valor === -1) {
                        tryTiempoEspera();
                    }
                }, 50);
            });
        
            observer.observe(document.documentElement, {
                childList: true,
                subtree: true,
                characterData: true
            });
        
            // Retries ligeros para SPAs lentas
            setTimeout(tryExtract, 300);
            setTimeout(tryExtract, 1000);
            
            setTimeout(tryTiempoEspera, 300);
            setTimeout(tryTiempoEspera, 1000);
        
            // Cleanup hard timeout
            setTimeout(cleanup, 30000);
        
        })();
        """.trimIndent()
    }

    val HTML_CAPTURE_AND_SEND by lazy {
        """
        (function () {

            const selector = __SELECTOR__;
            const timeoutMs = __TIMEOUT__;
        
            if (window.__htmlCaptureRunning) {
                return;
            }
        
            window.__htmlCaptureRunning = true;
        
            let finished = false;
            let observer = null;
        
            function finish() {
        
                if (finished) return;
        
                finished = true;
        
                window.__htmlCaptureRunning = false;
        
                if (observer) {
                    observer.disconnect();
                    observer = null;
                }
            }
        
            function send(html) {
        
                if (finished) return;
        
                finish();
        
                if (window.Android && window.Android.onHtmlReady) {
                    window.Android.onHtmlReady(html);
                }
            }
        
            function tryExtract() {
        
                if (finished) return true;
        
                let elements;
        
                try {
                    elements = document.querySelectorAll(selector);
                } catch (e) {
                    send("");
                    return true;
                }
        
                if (elements.length === 0) {
                    return false;
                }
        
                const html = Array.from(elements)
                    .map(el => el.outerHTML)
                    .join("");
        
                send(html);
        
                return true;
            }
        
            // intento inmediato
            if (tryExtract()) return;
        
            let scheduled = false;
        
            observer = new MutationObserver(() => {
        
                if (scheduled || finished) return;
        
                scheduled = true;
        
                setTimeout(() => {
        
                    scheduled = false;
                    tryExtract();
        
                }, 50);
            });
        
            const target = document.documentElement || document.body;
        
            if (!target) {
                window.__htmlCaptureRunning = false;
                return;
            }
        
            observer.observe(target, {
                childList: true,
                subtree: true,
                characterData: true
            });
        
            // fallback ligero (solo 2 retries, no loop infinito)
            setTimeout(tryExtract, 300);
            setTimeout(tryExtract, 1000);
        
            // hard timeout
            setTimeout(() => {
                finish();
            }, timeoutMs);
        
        })();
        """.trimIndent()
    }

    val DOM_HTML_COLLECTOR by lazy {
        """
        (function () {

            const selector = __SELECTOR__;
        
            if (!selector) return "";
        
            try {
        
                const nodes = document.querySelectorAll(selector);
        
                if (nodes.length === 0) return "";
        
                let html = "";
        
                for (let i = 0; i < nodes.length; i++) {
                    html += nodes[i].outerHTML;
                }
        
                return html;
        
            } catch (e) {
                return "";
            }
        
        })();
        """.trimIndent()
    }
}
