package com.haftabook.app

import android.content.Context
import androidx.activity.ComponentActivity

internal object AndroidAppContext {
    var applicationContext: Context? = null

    /** Set from [MainActivity] for SMS / UI that needs an Activity [Context]. */
    var activity: ComponentActivity? = null
}
