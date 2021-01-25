package lottie

import android.Bitmap
import android.BitmapFactory
import android.Context
import lottie.model.LottieCompositionCache
import lottie.parser.LottieCompositionMoshiParser
import lottie.parser.moshi.JsonReader
import lottie.parser.moshi.of
import lottie.utils.Utils
import lottie.utils.Utils.closeQuietly
import okio.`-DeprecatedOkio`.buffer
import okio.`-DeprecatedOkio`.source
import okio.buffer
import okio.source
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.util.concurrent.Callable
import java.util.zip.ZipInputStream

public object LottieCompositionFactory {
    private val taskCache: MutableMap<String, LottieTask<LottieComposition>> = mutableMapOf()

    public fun fromAssetSync(context: Context, fileName: String): LottieResult<LottieComposition> {
        val cacheKey = "asset_$fileName"
        return fromAssetSync(context, fileName, cacheKey)
    }

    public fun fromAssetSync(context: Context, fileName: String, cacheKey: String?): LottieResult<LottieComposition> {
        return try {
            if (fileName.endsWith(".zip") || fileName.endsWith(".lottie")) {
                fromZipStreamSync(ZipInputStream(context.getAssets().open(fileName)), cacheKey)
            } else fromJsonInputStreamSync(context.getAssets().open(fileName), cacheKey)
        } catch (e: IOException) {
            LottieResult(e)
        }
    }

    public fun fromAsset(context: Context, fileName: String): LottieTask<LottieComposition> {
        val cacheKey = "asset_$fileName"
        return fromAsset(context, fileName, cacheKey)
    }

    public fun fromAsset(context: Context, fileName: String, cacheKey: String?): LottieTask<LottieComposition> {
        val appContext: Context = context.getApplicationContext()
        return cache(cacheKey,
            Callable<LottieResult<LottieComposition>> {
                fromAssetSync(
                    appContext,
                    fileName,
                    cacheKey
                )
            })
    }

    public fun fromZipStreamSync(inputStream: ZipInputStream, cacheKey: String?): LottieResult<LottieComposition> {
        return try {
            fromZipStreamSyncInternal(inputStream, cacheKey)
        } finally {
            closeQuietly(inputStream)
        }
    }

    private fun fromZipStreamSyncInternal(
        inputStream: ZipInputStream,
        cacheKey: String?,
    ): LottieResult<LottieComposition> {
        var composition: LottieComposition? = null
        val images: MutableMap<String, Bitmap> = HashMap<String, Bitmap>()
        try {
            var entry = inputStream.nextEntry
            while (entry != null) {
                val entryName = entry.name
                if (entryName.contains("__MACOSX")) {
                    inputStream.closeEntry()
                } else if (entry.name.equals("manifest.json", ignoreCase = true)) { //ignore .lottie manifest
                    inputStream.closeEntry()
                } else if (entry.name.contains(".json")) {
                    val reader: JsonReader = of(inputStream.source().buffer())
                    composition = fromJsonReaderSyncInternal(reader, null, false).getValue()
                } else if (entryName.contains(".png") || entryName.contains(".webp")) {
                    val splitName = entryName.split("/").toTypedArray()
                    val name = splitName[splitName.size - 1]
                    images[name] = BitmapFactory.decodeStream(inputStream)
                } else {
                    inputStream.closeEntry()
                }
                entry = inputStream.nextEntry
            }
        } catch (e: IOException) {
            return LottieResult(e)
        }
        if (composition == null) {
            return LottieResult(IllegalArgumentException("Unable to parse composition"))
        }
        for ((key, value) in images) {
            val imageAsset: LottieImageAsset? = findImageAssetForFileName(composition, key)
            if (imageAsset != null) {
                imageAsset.bitmap = Utils.resizeBitmapIfNeeded(value, imageAsset.width, imageAsset.height)
            }
        }

        // Ensure that all bitmaps have been set.
        for ((_, value) in composition.images?.entries!!) {
            if (value.bitmap == null) {
                return LottieResult(IllegalStateException("There is no image for " + value.fileName))
            }
        }
        if (cacheKey != null) {
            LottieCompositionCache.instance.put(cacheKey, composition)
        }
        return LottieResult(composition)
    }

    private fun findImageAssetForFileName(composition: LottieComposition, fileName: String): LottieImageAsset? {
        for (asset in composition.images?.values!!) {
            if (asset.fileName == fileName) {
                return asset
            }
        }
        return null
    }

    private fun fromJsonReaderSyncInternal(
        reader: JsonReader, cacheKey: String?, close: Boolean,
    ): LottieResult<LottieComposition> {
        return try {
            val composition: LottieComposition = LottieCompositionMoshiParser.parse(reader)
            if (cacheKey != null) {
                LottieCompositionCache.instance.put(cacheKey, composition)
            }
            LottieResult(composition)
        } catch (e: Exception) {
            LottieResult(e)
        } finally {
            if (close) {
                closeQuietly(reader)
            }
        }
    }

    public fun fromJsonInputStreamSync(stream: InputStream, cacheKey: String?): LottieResult<LottieComposition> {
        return fromJsonInputStreamSync(stream, cacheKey, true)
    }

    private fun fromJsonInputStreamSync(
        stream: InputStream,
        cacheKey: String?,
        close: Boolean,
    ): LottieResult<LottieComposition> {
        return try {
            fromJsonReaderSync(of(stream.source().buffer()), cacheKey)
        } finally {
            if (close) {
                closeQuietly(stream)
            }
        }
    }

    public fun fromJsonReaderSync(
        reader: JsonReader,
        cacheKey: String?,
    ): LottieResult<LottieComposition> {
        return fromJsonReaderSyncInternal(reader, cacheKey, true)
    }

    private fun cache(
        cacheKey: String?, callable: Callable<LottieResult<LottieComposition>>,
    ): LottieTask<LottieComposition> {
        val cachedComposition: LottieComposition? =
            if (cacheKey == null) null else LottieCompositionCache.instance[cacheKey]
        if (cachedComposition != null) {
            return LottieTask { LottieResult(cachedComposition) }
        }
        if (cacheKey != null && taskCache.containsKey(cacheKey)) {
            return taskCache[cacheKey]!!
        }
        val task: LottieTask<LottieComposition> = LottieTask(callable)
        if (cacheKey != null) {
            task.addListener(object : LottieListener<LottieComposition> {
                override fun onResult(result: LottieComposition) {
                    taskCache.remove(cacheKey)
                }
            })
            task.addFailureListener(object : LottieListener<Throwable> {
                override fun onResult(result: Throwable) {
                    taskCache.remove(cacheKey)
                }
            })
            taskCache[cacheKey] = task
        }
        return task
    }
}