package com.example.integralcalculator.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

@Composable
fun LatexTextView(
    latex: String,
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {}
) {
    var webView: WebView? = null

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onReady()
                        renderLatex(latex)
                    }
                }

                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="utf-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body { 
                                margin: 0; 
                                padding: 8px; 
                                color: #ffffff; 
                                font-size: 20px; 
                                text-align: center; 
                                background-color: transparent;
                                font-family: 'Times New Roman', serif;
                            }
                        </style>
                        <script>
                            window.MathJax = {
                                tex: {
                                    inlineMath: [['$', '$'], ['\\(', '\\)']],
                                    displayMath: [['$$', '$$'], ['\\[', '\]']]
                                }
                            };
                        </script>
                        <script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js"></script>
                        <script>
                            function renderLatex(latex) {
                                var container = document.getElementById('math-container');
                                if (!container) return;
                                if (!latex || latex === '') {
                                    container.innerHTML = '';
                                    return;
                                }
                                container.innerHTML = '\\[' + latex + '\\]';
                                if (window.MathJax) {
                                    MathJax.typesetPromise([container]).catch(function(err) {
                                        container.innerHTML = latex;
                                    });
                                }
                            }
                        </script>
                    </head>
                    <body>
                        <div id="math-container"></div>
                    </body>
                    </html>
                """.trimIndent()

                loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                webView = this
            }
        },
        modifier = modifier.height(80.dp)
    ) { view ->
        webView = view
        if (latex.isNotEmpty()) {
            view.renderLatex(latex)
        }
    }
}

private fun WebView.renderLatex(latex: String) {
    val safeLatex = latex
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", " ")
        .replace("\r", " ")
    evaluateJavascript("if(window.renderLatex) renderLatex('$safeLatex');", null)
}