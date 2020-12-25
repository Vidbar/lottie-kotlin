package lottie.utils

import android.Choreographer
import lottie.LottieComposition

public open class LottieValueAnimator : BaseLottieAnimator() {
    protected var running: Boolean = false
    private var composition: LottieComposition? = null
    private var minFrame = Int.MIN_VALUE.toFloat()
    private var maxFrame = Int.MAX_VALUE.toFloat()

    public override fun isRunning(): Boolean {
        return running
    }

    override fun cancel() {
        notifyCancel()
        removeFrameCallback()
    }

    protected open fun removeFrameCallback() {
        removeFrameCallback(true)
    }

    protected open fun removeFrameCallback(stopRunning: Boolean) {
        Choreographer.instance.removeFrameCallback(this)
        if (stopRunning) {
            running = false
        }
    }

    public fun clearComposition() {
        this.composition = null
        minFrame = Int.MIN_VALUE.toFloat()
        maxFrame = Int.MAX_VALUE.toFloat()
    }
}
