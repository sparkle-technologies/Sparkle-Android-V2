package com.cyberflow.base.util

import android.view.View

class AdapterExt {
}




fun <T : View> T.click(
    block: View.OnClickListener,
) {
    setOnClickListener {
        block.onClick(this)
    }
}
