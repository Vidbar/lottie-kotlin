package lottie.model.animatable

import lottie.model.content.ShapeData
import lottie.value.Keyframe

public class AnimatableShapeValue(keyframes: List<Keyframe<ShapeData>>) :
    BaseAnimatableValue<ShapeData?, Path?>(keyframes) {
    public fun createAnimation(): BaseKeyframeAnimation<ShapeData, Path> {
        return ShapeKeyframeAnimation(keyframes)
    }
}
