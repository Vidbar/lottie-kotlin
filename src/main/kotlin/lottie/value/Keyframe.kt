package lottie.value

import android.Interpolator
import android.PointF
import lottie.LottieComposition

public class Keyframe<T>(
    private val composition: LottieComposition?,
    public val startValue: T?,
    public var endValue: T?,
    public val interpolator: Interpolator?,
    public val startFrame: Float,
    public var endFrame: Float?
) {
    public constructor(value: T) : this(
        null,
        value,
        value,
        null,
        Float.MIN_VALUE,
        Float.MAX_VALUE,
    )

    public companion object {
        private const val UNSET_FLOAT = -3987645.78543923f
        private const val UNSET_INT = 784923401
    }

    public val startValueFloat: Float = UNSET_FLOAT
    public val endValueFloat: Float = UNSET_FLOAT

    public val startValueInt: Int = UNSET_INT
    public val endValueInt: Int = UNSET_INT

    public var startProgress: Float = Float.MIN_VALUE
        get() {
            if (composition == null) {
                return 0f
            }
            if (field == Float.MIN_VALUE) {
                field = (startFrame - composition.getStartFrame()) / composition.getDurationFrames()
            }
            return field
        }
        private set

    public var endProgress: Float = Float.MIN_VALUE
        get() {
            if (composition == null) {
                return 1f
            }
            if (field == Float.MIN_VALUE) {
                field = if (endFrame == null) {
                    1f
                } else {
                    val startProgress = startProgress
                    val durationFrames = endFrame!! - startFrame
                    val durationProgress: Float = durationFrames / composition.getDurationFrames()
                    startProgress + durationProgress
                }
            }
            return field
        }
        private set

    public var pathCp1: PointF? = null
    public var pathCp2: PointF? = null

    public val isStatic: Boolean
        get() = interpolator == null

    public fun containsProgress(progress: Float): Boolean {
        return progress >= startProgress && progress < endProgress
    }

    override fun toString(): String {
        return "Keyframe{" + "startValue=" + startValue +
                ", endValue=" + endValue +
                ", startFrame=" + startFrame +
                ", endFrame=" + endFrame +
                ", interpolator=" + interpolator +
                '}'
    }
}
