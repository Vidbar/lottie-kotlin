package lottie.utils

import java.io.Closeable
import java.io.InterruptedIOException
import java.lang.Exception
import java.net.ProtocolException
import java.net.SocketException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.nio.channels.ClosedChannelException
import javax.net.ssl.SSLException

public object Utils {
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

    public fun isNetworkException(e: Throwable): Boolean {
        return e is SocketException || e is ClosedChannelException ||
                e is InterruptedIOException || e is ProtocolException ||
                e is SSLException || e is UnknownHostException ||
                e is UnknownServiceException
    }
}

