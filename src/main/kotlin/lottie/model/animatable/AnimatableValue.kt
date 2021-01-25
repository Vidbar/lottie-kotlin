package lottie.model.animatable

import lottie.animation.keyframe.BaseKeyframeAnimation
import lottie.value.Keyframe

public interface AnimatableValue<K, A> {
    public val keyframes: List<Keyframe<K>>
    public val isStatic: Boolean
    public fun createAnimation(): BaseKeyframeAnimation<K, A>
}