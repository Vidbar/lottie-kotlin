package lottie.animation.keyframe

import lottie.value.Keyframe

public abstract class KeyframeAnimation<T>(keyframes: List<Keyframe<T>>) :
    BaseKeyframeAnimation<T, T>(keyframes)
