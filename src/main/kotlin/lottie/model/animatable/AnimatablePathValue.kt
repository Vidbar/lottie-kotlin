package lottie.model.animatable

import android.PointF
import lottie.animation.keyframe.BaseKeyframeAnimation
import lottie.value.Keyframe

public class AnimatablePathValue(
    public override val keyframes: List<Keyframe<PointF>> = listOf(Keyframe(PointF(0F, 0F)))
    ) : AnimatableValue<PointF, PointF> {

    override val isStatic: Boolean
        get() = keyframes.size == 1 && keyframes[0].isStatic

    override fun createAnimation(): BaseKeyframeAnimation<PointF, PointF> {
        return if (keyframes[0].isStatic) {
            lottie.animation.keyframe.PointKeyframeAnimation(keyframes)
        } else PathKeyframeAnimation(keyframes)
    }
}
