package lottie.utils

import android.Log
import lottie.L
import lottie.LottieLogger
import java.util.HashSet

public class LogcatLogger : LottieLogger {
    private val loggedMessages: MutableSet<String> = HashSet()


    public override fun debug(message: String) {
        debug(message, null)
    }

    public override fun debug(message: String, exception: Throwable?) {
        if (L.DBG) {
            Log.d(L.TAG, message, exception)
        }
    }

    override fun warning(message: String) {
        warning(message, null)
    }

    public override fun warning(message: String, exception: Throwable?) {
        if (loggedMessages.contains(message)) {
            return
        }
        Log.w(L.TAG, message, exception)
        loggedMessages.add(message)
    }

    public override fun error(message: String, exception: Throwable?) {
        if (L.DBG) {
            Log.d(L.TAG, message, exception)
        }
    }

}
