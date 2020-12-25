package lottie.model;

import android.LruCache

import lottie.LottieComposition

public class LottieCompositionCache private constructor() {
    public companion object {
        public val instance: LottieCompositionCache = LottieCompositionCache()
    }

    private val cache: LruCache<String, LottieComposition> = LruCache(20)

    public operator fun get(cacheKey: String?): LottieComposition? {
        return if (cacheKey == null) {
            null
        } else cache.get(cacheKey)
    }

    public fun put(cacheKey: String?, composition: LottieComposition) {
        if (cacheKey == null) {
            return
        }
        cache.put(cacheKey, composition)
    }

    public fun clear() {
        cache.evictAll()
    }

    public fun resize(size: Int) {
        cache.resize(size)
    }
}
