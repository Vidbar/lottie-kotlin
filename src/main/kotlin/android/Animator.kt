package android

public abstract class Animator {
    public interface AnimatorListener {
        public fun onAnimationCancel(animation: Animator)
        public fun onAnimationEnd(animation: Animator)
        public fun onAnimationEnd(animation: Animator, isReverse: Boolean)
        public fun onAnimationRepeat(animation: Animator)
        public fun onAnimationStart(animation: Animator)
        public fun onAnimationStart(animation: Animator, isReverse: Boolean)
    }
}