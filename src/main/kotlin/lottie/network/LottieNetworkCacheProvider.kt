package lottie.network

import java.io.File

public interface LottieNetworkCacheProvider {
    public fun getCacheDir(): File
}
