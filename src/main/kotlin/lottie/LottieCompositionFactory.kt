package lottie

import android.Context
import lottie.model.LottieCompositionCache
import lottie.parser.moshi.JsonReader
import lottie.utils.closeQuietly
import org.jetbrains.skija.Bitmap
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.util.concurrent.Callable
import java.util.zip.ZipInputStream

public class LottieCompositionFactory {
}

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
    cacheKey: String?
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
                val reader: JsonReader = of(buffer(source(inputStream)))
                composition = LottieCompositionFactory.fromJsonReaderSyncInternal(reader, null, false).getValue()
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
        val imageAsset: LottieImageAsset = LottieCompositionFactory.findImageAssetForFileName(composition, key)
        if (imageAsset != null) {
            imageAsset.setBitmap(Utils.resizeBitmapIfNeeded(value, imageAsset.getWidth(), imageAsset.getHeight()))
        }
    }

    // Ensure that all bitmaps have been set.
    for ((_, value) in composition.getImages().entrySet()) {
        if (value.getBitmap() == null) {
            return LottieResult(IllegalStateException("There is no image for " + value.getFileName()))
        }
    }
    if (cacheKey != null) {
        LottieCompositionCache.getInstance().put(cacheKey, composition)
    }
    return LottieResult(composition)
}

private fun fromJsonReaderSyncInternal(
    reader: com.airbnb.lottie.parser.moshi.JsonReader, cacheKey: String?, close: Boolean
): LottieResult<LottieComposition?>? {
    return try {
        val composition: LottieComposition = LottieCompositionMoshiParser.parse(reader)
        if (cacheKey != null) {
            LottieCompositionCache.getInstance().put(cacheKey, composition)
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

public fun fromJsonInputStreamSync(stream: InputStream, cacheKey: String?): LottieResult<LottieComposition?>? {
    return fromJsonInputStreamSync(stream, cacheKey, true)
}

private fun fromJsonInputStreamSync(
    stream: InputStream,
    cacheKey: String?,
    close: Boolean
): LottieResult<LottieComposition?>? {
    return try {
        fromJsonReaderSync(of(buffer(source(stream))), cacheKey)
    } finally {
        if (close) {
            closeQuietly(stream)
        }
    }
}

public fun fromJsonReaderSync(
    reader: JsonReader,
    cacheKey: String?
): LottieResult<LottieComposition?>? {
    return fromJsonReaderSyncInternal(reader, cacheKey, true)
}

private fun cache(
    cacheKey: String?, callable: Callable<LottieResult<LottieComposition>>
): LottieTask<LottieComposition> {
    val cachedComposition: LottieComposition? =
        if (cacheKey == null) null else LottieCompositionCache.instance[cacheKey]
    if (cachedComposition != null) {
        return LottieTask(Callable { LottieResult(cachedComposition) })
    }
    if (cacheKey != null && LottieCompositionFactory.taskCache.containsKey(cacheKey)) {
        return LottieCompositionFactory.taskCache.get(cacheKey)
    }
    val task: LottieTask<LottieComposition?> = LottieTask(callable)
    if (cacheKey != null) {
        task.addListener(object : LottieListener<LottieComposition?>() {
            fun onResult(result: LottieComposition?) {
                LottieCompositionFactory.taskCache.remove(cacheKey)
            }
        })
        task.addFailureListener(object : LottieListener<Throwable?>() {
            fun onResult(result: Throwable?) {
                LottieCompositionFactory.taskCache.remove(cacheKey)
            }
        })
        LottieCompositionFactory.taskCache.put(cacheKey, task)
    }
    return task
}