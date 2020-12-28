package lottie

public interface LottieLogger {
    public fun debug(message: String)

    public fun debug(message: String, exception: Throwable?)

    public fun warning(message: String)

    public fun warning(message: String, exception: Throwable?)

    public fun error(message: String, exception: Throwable?)
}