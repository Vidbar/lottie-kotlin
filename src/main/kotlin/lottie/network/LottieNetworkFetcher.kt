package lottie.network

import java.io.IOException

public interface LottieNetworkFetcher {
    @Throws(IOException::class)
    public fun fetchSync(url: String): LottieFetchResult
}
