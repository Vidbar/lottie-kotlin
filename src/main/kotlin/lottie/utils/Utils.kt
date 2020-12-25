package lottie.utils

import java.io.Closeable
import java.lang.Exception

public class Utils {
}

public fun closeQuietly(closeable: Closeable?) {
    if (closeable != null) {
        try {
            closeable.close()
        } catch (rethrown: RuntimeException) {
            throw rethrown
        } catch (ignored: Exception) {
        }
    }
}