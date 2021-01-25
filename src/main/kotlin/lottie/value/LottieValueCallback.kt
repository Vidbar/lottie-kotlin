package lottie.value

import lottie.animation.keyframe.BaseKeyframeAnimation

public class LottieValueCallback<T>(staticValue: T? = null) {
    private val frameInfo: LottieFrameInfo<T> = LottieFrameInfo()

    private var animation: BaseKeyframeAnimation<*, *>? = null

    protected var value: T? = staticValue
        set(value) {
            field = value
            animation?.notifyListeners()
        }

    public fun getValueInternal(
        startFrame: Float,
        endFrame: Float,
        startValue: T,
        endValue: T,
        linearKeyframeProgress: Float,
        interpolatedKeyframeProgress: Float,
        overallProgress: Float,
    ): T? {
        //getValue has parameter, why?
        /*return getValue(
            frameInfo.set(
                startFrame,
                endFrame,
                startValue,
                endValue,
                linearKeyframeProgress,
                interpolatedKeyframeProgress,
                overallProgress
            )
        )*/
        frameInfo.set(
            startFrame,
            endFrame,
            startValue,
            endValue,
            linearKeyframeProgress,
            interpolatedKeyframeProgress,
            overallProgress
        )
        return this.value
    }

    public fun setAnimation(animation: BaseKeyframeAnimation<*, *>?) {
        this.animation = animation
    }
}

