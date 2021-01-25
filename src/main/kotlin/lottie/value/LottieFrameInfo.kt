package lottie.value

public class LottieFrameInfo<T> {
    public var startFrame: Float = 0f
        private set
    public var endFrame: Float = 0f
        private set
    public var startValue: T? = null
        private set
    public var endValue: T? = null
        private set
    public var linearKeyframeProgress: Float = 0f
        private set
    public var interpolatedKeyframeProgress: Float = 0f
        private set
    public var overallProgress: Float = 0f
        private set

    public operator fun set(
        startFrame: Float,
        endFrame: Float,
        startValue: T,
        endValue: T,
        linearKeyframeProgress: Float,
        interpolatedKeyframeProgress: Float,
        overallProgress: Float,
    ): LottieFrameInfo<T> {
        this.startFrame = startFrame
        this.endFrame = endFrame
        this.startValue = startValue
        this.endValue = endValue
        this.linearKeyframeProgress = linearKeyframeProgress
        this.interpolatedKeyframeProgress = interpolatedKeyframeProgress
        this.overallProgress = overallProgress
        return this
    }
}

