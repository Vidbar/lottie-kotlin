package lottie

import android.LongSparseArray
import android.Rect
import android.SparseArrayCompat
import lottie.model.Font
import lottie.model.FontCharacter
import lottie.model.Marker
import lottie.model.layer.Layer
import lottie.utils.Logger
import java.util.*
import kotlin.collections.HashSet

public class LottieComposition {
    private val performanceTracker: PerformanceTracker = PerformanceTracker()
    private val warnings = HashSet<String>()
    private var precomps: Map<String, List<Layer>>? = null
    public var images: Map<String, LottieImageAsset>? = null
        private set

    private var fonts: Map<String, Font> = mapOf()
    private var markers: List<Marker> = listOf()
    private var characters: SparseArrayCompat<FontCharacter>? = null
    private var layerMap: LongSparseArray<Layer>? = null
    private var layers: List<Layer> = listOf()


    private var bounds: Rect = TODO()
    public var startFrame: Float = 0f
        private set
    public var endFrame: Float = 0f
        private set
    public var frameRate: Float = 0f
        private set

    public var hasDashPattern: Boolean = false

    public var maskAndMatteCount: Int = 0
        private set

    public fun init(
        bounds: Rect,
        startFrame: Float,
        endFrame: Float,
        frameRate: Float,
        layers: List<Layer>,
        layerMap: LongSparseArray<Layer>,
        precomps: Map<String, List<Layer>>,
        images: Map<String, LottieImageAsset>,
        characters: SparseArrayCompat<FontCharacter>,
        fonts: Map<String, Font>,
        markers: List<Marker>,
    ) {
        this.bounds = bounds
        this.startFrame = startFrame
        this.endFrame = endFrame
        this.frameRate = frameRate
        this.layers = layers
        this.layerMap = layerMap
        this.precomps = precomps
        this.images = images
        this.characters = characters
        this.fonts = fonts
        this.markers = markers
    }

    public fun addWarning(warning: String) {
        Logger.warning(warning)
        warnings.add(warning)
    }

    public fun incrementMatteOrMaskCount(amount: Int) {
        maskAndMatteCount += amount
    }

    public fun getWarnings(): ArrayList<String> {
        return ArrayList(listOf(*warnings.toTypedArray()))
    }

    public fun setPerformanceTrackingEnabled(enabled: Boolean) {
        performanceTracker.setEnabled(enabled)
    }

    public fun getPerformanceTracker(): PerformanceTracker {
        return performanceTracker
    }

    public fun layerModelForId(id: Long): Layer {
        return layerMap?.get(id)!!
    }

    public fun getBounds(): Rect {
        return bounds!!
    }

    public val duration: Long
        get() = (durationFrames / frameRate * 1000).toLong()

    public fun getLayers(): List<Layer> {
        return layers
    }

    public fun getPrecomps(id: String): List<Layer>? {
        return precomps!![id]!!
    }

    public fun getCharacters(): SparseArrayCompat<FontCharacter> {
        return characters!!
    }

    public fun getFonts(): Map<String, Font> {
        return fonts
    }

    public fun getMarkers(): List<Marker> {
        return markers
    }

    public fun getMarker(markerName: String?): Marker? {
        val size = markers!!.size
        for (i in markers!!.indices) {
            val marker: Marker = markers!![i]
            if (marker.matchesName(markerName)) {
                return marker
            }
        }
        return null
    }

    public fun hasImages(): Boolean {
        return images!!.isNotEmpty()
    }

    public val durationFrames: Float
        get() = endFrame - startFrame

    override fun toString(): String {
        val sb = StringBuilder("LottieComposition:\n")
        for (layer in layers!!) {
            sb.append(layer.toString("\t"))
        }
        return sb.toString()
    }
}

