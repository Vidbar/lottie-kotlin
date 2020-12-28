package lottie

import android.Drawable
import lottie.manager.ImageAssetManager
import lottie.model.layer.CompositionLayer
import lottie.utils.LottieValueAnimator
import java.util.ArrayList

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

    public fun setComposition(composition: LottieComposition): Boolean {
        if (this.composition === composition) {
            return false
        }
        isDirty = false
        clearComposition()
        this.composition = composition
        buildCompositionLayer()
        animator.setComposition(composition)
        setProgress(animator.getAnimatedFraction())
        setScale(scale)
        updateBounds()

        // We copy the tasks to a new ArrayList so that if this method is called from multiple threads,
        // then there won't be two iterators iterating and removing at the same time.
        val it: MutableIterator<com.airbnb.lottie.LottieDrawable.LazyCompositionTask> =
            ArrayList<com.airbnb.lottie.LottieDrawable.LazyCompositionTask>(lazyCompositionTasks).iterator()
        while (it.hasNext()) {
            val t: com.airbnb.lottie.LottieDrawable.LazyCompositionTask = it.next()
            t.run(composition)
            it.remove()
        }
        lazyCompositionTasks.clear()
        composition.setPerformanceTrackingEnabled(performanceTrackingEnabled)

        // Ensure that ImageView updates the drawable width/height so it can
        // properly calculate its drawable matrix.
        val callback = getCallback()
        if (callback is ImageView) {
            (callback as ImageView).setImageDrawable(null)
            (callback as ImageView).setImageDrawable(this)
        }
        return true
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

    private fun buildCompositionLayer() {
        compositionLayer = CompositionLayer(
            this, LayerParser.parse(composition), composition.getLayers(), composition
        )
        if (outlineMasksAndMattes) {
            compositionLayer.setOutlineMasksAndMattes(true)
        }
    }
}
