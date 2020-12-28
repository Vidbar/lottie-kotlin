package lottie

public class LottieComposition {
    public var hasDashPattern: Boolean = false
    private val startFrame = 0f
    private val endFrame = 0f
    public var maskAndMatteCount: Int = 0
        private set

    public fun getStartFrame(): Float {
        return startFrame
    }

    public fun getDurationFrames(): Float {
        return endFrame - startFrame
    }
}
