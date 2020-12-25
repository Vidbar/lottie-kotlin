package lottie.network

import java.io.Closeable
import java.io.IOException
import java.io.InputStream

public interface LottieFetchResult : Closeable {
    public fun isSuccessful(): Boolean

    @Throws(IOException::class)
    public fun bodyByteStream(): InputStream

    public fun contentType(): String?

    public fun error(): String?
}
