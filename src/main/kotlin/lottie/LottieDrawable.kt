package lottie

import android.Drawable
import lottie.manager.ImageAssetManager
import lottie.model.layer.CompositionLayer
import lottie.utils.LottieValueAnimator

public class LottieDrawable: Drawable(), Drawable.Callback {
    private val animator: LottieValueAnimator = LottieValueAnimator()
    private var composition: LottieComposition? = null
    private var compositionLayer: CompositionLayer? = null
    private var imageAssetManager: ImageAssetManager? = null
    private var isDirty = false

    override fun invalidateDrawable(who: Drawable) {
        TODO("Not yet implemented")
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        TODO("Not yet implemented")
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        TODO("Not yet implemented")
    }

    public fun clearComposition() {
        if (animator.isRunning()) {
            animator.cancel()
        }
        composition = null
        compositionLayer = null
        imageAssetManager = null
        animator.clearComposition()
        invalidateSelf()
    }

    public override fun invalidateSelf() {
        if (isDirty) {
            return
        }
        isDirty = true
        val callback: Callback = getCallback()
        if (callback != null) {
            callback.invalidateDrawable(this)
        }
    }

}
