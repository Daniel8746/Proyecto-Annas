package com.pmdm.annas.data.js

object JsScripts {
    val AUTO_EXTRACT_AND_REDIRECT_SCRIPT by lazy {
        """
        (function() {
            function tryExtract() {
                const span = document.querySelector('span.break-all');
                
                if (span) {
                    const text = span.innerText.trim();
    
                    if (text.startsWith('http')) {
                        window.location.href = text;
                        return true;
                    }
                }
    
                const copyBtn =
                    document.querySelector(
                        'button[onclick*="http"]'
                    );
    
                if (copyBtn) {
                    const match =
                        copyBtn.getAttribute('onclick')
                        ?.match(/https?:\/\/[^']+/);
    
                    if (match) {
                        window.location.href = match[0];
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
                    btn.click();
                    return true;
                }
    
                return false;
            }
    
            const observer = new MutationObserver(() => {
                if (tryExtract()) {
                    observer.disconnect();
                }
            });
    
            observer.observe(document.body, {
                childList: true,
                subtree: true
            });
            
            // intento inicial inmediato
            if (tryExtract()) {
                observer.disconnect();
            }
    
            setTimeout(() => {
                observer.disconnect();
            }, 30000);
        })();
        """.trimIndent()
    }

    val HTML_CAPTURE_AND_SEND by lazy {
        """
            (function() {

                const selector = "__VAR__";

                function send() {

                    const el =
                        document.querySelectorAll(selector);

                    if (el.length > 0) {

                        Android.onHtmlReady(
                            Array.from(el)
                                .map(d => d.outerHTML)
                                .join('')
                        );

                        return true;
                    }

                    return false;
                }

                if (!send()) {

                    const observer =
                        new MutationObserver(() => {

                            if (send()) observer.disconnect();

                        });

                    observer.observe(
                        document.body,
                        { childList: true }
                    );

                    setTimeout(() => {

                        if (send())
                            observer.disconnect();

                    }, __TIMEOUT__);
                }

            })();
        """.trimIndent()
    }

    val DOM_HTML_COLLECTOR by lazy {
        "(function(){return Array.from(document.querySelectorAll(__SELECTOR__)).map(d=>d.outerHTML).join('');})();"
    }
}