package lottie.utils

import lottie.LottieLogger

public object Logger {
    private var INSTANCE: LottieLogger = LogcatLogger()

    public fun setInstance(instance: LottieLogger) {
        INSTANCE = instance
    }

    public fun debug(message: String) {
        INSTANCE.debug(message)
    }

    public fun debug(message: String, exception: Throwable) {
        INSTANCE.debug(message, exception)
    }

    public fun warning(message: String) {
        INSTANCE.warning(message)
    }

    public fun warning(message: String, exception: Throwable) {
        INSTANCE.warning(message, exception)
    }

    public fun error(message: String, exception: Throwable) {
        INSTANCE.error(message, exception)
    }
}
