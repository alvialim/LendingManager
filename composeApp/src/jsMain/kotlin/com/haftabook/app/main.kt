package com.haftabook.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlin.js.js
import org.w3c.dom.Element

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val root = js("document.getElementById('composeRoot')").unsafeCast<Element?>()
        ?: error("composeRoot not found — check index.html has <div id=\"composeRoot\">")
    ComposeViewport(viewportContainer = root) {
        AppRoot()
    }
}
