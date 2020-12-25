package lottie

import android.Context
import lottie.network.*
import java.io.File

public object L {
    public var DBG: Boolean = false
    public const val TAG: String = "LOTTIE"
    private const val MAX_DEPTH = 20
    private var traceEnabled = false
    private var sections: Array<String?> = arrayOfNulls(0)
    private var startTimeNs: LongArray = longArrayOf(0)
    private var traceDepth = 0
    private var depthPastMaxDepth = 0
    private var fetcher: LottieNetworkFetcher? = null
    private var cacheProvider: LottieNetworkCacheProvider? = null


    private var networkFetcher: NetworkFetcher? = null

    private var networkCache: NetworkCache? = null

    public fun setTraceEnabled(enabled: Boolean) {
        if (traceEnabled == enabled) {
            return
        }
        traceEnabled = enabled
        if (traceEnabled) {
            sections = arrayOfNulls(MAX_DEPTH)
            startTimeNs = LongArray(MAX_DEPTH)
        }
    }

    public fun beginSection(section: String?) {
        if (!traceEnabled) {
            return
        }
        if (traceDepth == MAX_DEPTH) {
            depthPastMaxDepth++
            return
        }
        sections[traceDepth] = section
        startTimeNs[traceDepth] = System.nanoTime()
        //TODO TraceCompat.beginSection(section)
        traceDepth++
    }

    public fun endSection(section: String): Float {
        if (depthPastMaxDepth > 0) {
            depthPastMaxDepth--
            return 0F
        }
        if (!traceEnabled) {
            return 0F
        }
        traceDepth--
        check(traceDepth != -1) { "Can't end trace section. There are none." }
        check(section == sections[traceDepth]) {
            "Unbalanced trace call " + section +
                    ". Expected " + sections[traceDepth] + "."
        }
        //TODO TraceCompat.endSection()
        return (System.nanoTime() - startTimeNs[traceDepth]) / 1000000f
    }

    public fun setFetcher(customFetcher: LottieNetworkFetcher?) {
        fetcher = customFetcher
    }

    public fun setCacheProvider(customProvider: LottieNetworkCacheProvider?) {
        cacheProvider = customProvider
    }


    public fun networkFetcher(context: Context): NetworkFetcher {
        var local: NetworkFetcher? = networkFetcher
        if (local == null) {
            synchronized(NetworkFetcher::class.java) {
                local = networkFetcher
                if (local == null) {
                    local = NetworkFetcher(
                        networkCache(context),
                        fetcher ?: DefaultLottieNetworkFetcher()
                    )
                    networkFetcher = local
                }
            }
        }
        return local!!
    }

    public fun networkCache(context: Context): NetworkCache {
        var local: NetworkCache? = networkCache
        if (local == null) {
            synchronized(NetworkCache::class.java) {
                local = networkCache
                if (local == null) {
                    local = NetworkCache(
                        cacheProvider ?: object : LottieNetworkCacheProvider {
                            override fun getCacheDir(): File {
                                return File(context.getCacheDir(), "lottie_network_cache")
                            }
                        })
                    networkCache = local
                }
            }
        }
        return local!!
    }
}
