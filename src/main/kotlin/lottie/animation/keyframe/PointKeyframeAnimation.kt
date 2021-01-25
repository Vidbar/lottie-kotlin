package lottie.animation.keyframe

import android.PointF
import lottie.value.Keyframe

public class PointKeyframeAnimation(keyframes: List<Keyframe<PointF>>) : KeyframeAnimation<PointF?>(keyframes) {
    private val point: PointF = PointF()
    public fun getValue(keyframe: Keyframe<PointF>, keyframeProgress: Float): PointF {
        check(!(keyframe.startValue == null || keyframe.endValue == null)) { "Missing values for keyframe." }
        val startPoint: PointF = keyframe.startValue
        val endPoint: PointF = keyframe.endValue
        if (valueCallback != null) {
            val value: PointF = valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame, startPoint,
                endPoint, keyframeProgress, getLinearCurrentKeyframeProgress(), getProgress())
            if (value != null) {
                return value
            }
        }
        point.set(startPoint.x + keyframeProgress * (endPoint.x - startPoint.x),
            startPoint.y + keyframeProgress * (endPoint.y - startPoint.y))
        return point
    }
}