package com.android.chat

import android.webkit.JavascriptInterface
import com.android.chat.ui.call.CallActivity

class JavascriptInterface(val callActivity: CallActivity) {

    @JavascriptInterface
    public fun onPeerConnected() {
        callActivity.onPeerConnected()
        callActivity.callOther()
    }

    @JavascriptInterface
    public fun onAnswer(){
        callActivity.onCallAnswer()
    }

}