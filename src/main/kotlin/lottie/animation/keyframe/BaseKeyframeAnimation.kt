package lottie.animation.keyframe

public abstract class BaseKeyframeAnimation<K, A> {
    public interface AnimationListener {
        public fun onValueChanged()
    }
}
