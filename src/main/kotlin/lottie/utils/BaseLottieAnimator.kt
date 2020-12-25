package lottie.utils

import android.Animator
import android.ValueAnimator
import java.util.concurrent.CopyOnWriteArraySet

public abstract class BaseLottieAnimator : ValueAnimator() {
    private val listeners: Set<Animator.AnimatorListener> = CopyOnWriteArraySet<Animator.AnimatorListener>()

    public open fun notifyCancel() {
        for (listener in listeners) {
            listener.onAnimationCancel(this)
        }
    }
}
