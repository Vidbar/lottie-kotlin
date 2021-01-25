package lottie

import android.Log
import lottie.utils.MeanCalculator
import java.util.*
import kotlin.collections.HashMap

public class PerformanceTracker {
    public interface FrameListener {
        public fun onFrameRendered(renderTimeMs: Float)
    }

    private var enabled = false
    private val frameListeners: MutableSet<FrameListener> = mutableSetOf()
    private val layerRenderTimes: MutableMap<String, MeanCalculator?> = HashMap<String, MeanCalculator?>()
    private val floatComparator: Comparator<Pair<String, Float>> =
        Comparator<Pair<String, Float>> { (_, r1), (_, r2) ->
            if (r2 > r1) {
                return@Comparator 1
            } else if (r1 > r2) {
                return@Comparator -1
            }
            0
        }

    public fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    public fun recordRenderTime(layerName: String, millis: Float) {
        if (!enabled) {
            return
        }
        var meanCalculator: MeanCalculator? = layerRenderTimes[layerName]
        if (meanCalculator == null) {
            meanCalculator = MeanCalculator()
            layerRenderTimes[layerName] = meanCalculator
        }
        meanCalculator.add(millis)
        if (layerName == "__container") {
            for (listener in frameListeners) {
                listener.onFrameRendered(millis)
            }
        }
    }

    public fun addFrameListener(frameListener: FrameListener) {
        frameListeners.add(frameListener)
    }

    public fun removeFrameListener(frameListener: FrameListener) {
        frameListeners.remove(frameListener)
    }

    public fun clearRenderTimes() {
        layerRenderTimes.clear()
    }

    public fun logRenderTimes() {
        if (!enabled) {
            return
        }
        val sortedRenderTimes = sortedRenderTimes
        Log.d(L.TAG, "Render times:")
        for (i in sortedRenderTimes.indices) {
            val (first, second) = sortedRenderTimes[i]
            Log.d(L.TAG, java.lang.String.format("\t\t%30s:%.2f", first, second))
        }
    }

    public val sortedRenderTimes: List<Pair<String, Float>>
        get() {
            if (!enabled) {
                return emptyList()
            }
            val sortedRenderTimes: MutableList<Pair<String, Float>> = ArrayList(layerRenderTimes.size)
            for ((key, value) in layerRenderTimes) {
                if (value != null) {
                    sortedRenderTimes.add(Pair(key, value.mean))
                }
            }
            Collections.sort(sortedRenderTimes, floatComparator)
            return sortedRenderTimes
        }
}