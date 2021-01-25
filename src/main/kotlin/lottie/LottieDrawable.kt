package lottie

import android.Drawable
import android.ImageView
import android.ValueAnimator
import lottie.manager.ImageAssetManager
import lottie.model.layer.CompositionLayer
import lottie.parser.LayerParser
import lottie.utils.LottieValueAnimator
import lottie.utils.MiscUtils
import java.util.ArrayList

public class LottieDrawable : Drawable(), Drawable.Callback {
    private interface LazyCompositionTask {
        fun run(composition: LottieComposition?)
    }

    private var outlineMasksAndMattes: Boolean = false
    private val lazyCompositionTasks = ArrayList<LazyCompositionTask>()

    private val performanceTrackingEnabled = false

    private val animator: LottieValueAnimator = LottieValueAnimator()
    private var composition: LottieComposition? = null
    private var compositionLayer: CompositionLayer? = null
    private var imageAssetManager: ImageAssetManager? = null
    private var isDirty = false
    private var scale = 1f

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
        setProgress(animator.animatedFraction)
        setScale(scale)
        updateBounds()

        // We copy the tasks to a new ArrayList so that if this method is called from multiple threads,
        // then there won't be two iterators iterating and removing at the same time.
        val it: MutableIterator<LazyCompositionTask> =
            ArrayList(lazyCompositionTasks).iterator()
        while (it.hasNext()) {
            val t: LazyCompositionTask = it.next()
            t.run(composition)
            it.remove()
        }
        lazyCompositionTasks.clear()
        composition.setPerformanceTrackingEnabled(performanceTrackingEnabled)

        // Ensure that ImageView updates the drawable width/height so it can
        // properly calculate its drawable matrix.
        val callback = getCallback()
        if (callback is ImageView) {
            callback.setImageDrawable(null)
            callback.setImageDrawable(this)
        }
        return true
    }

    public fun setScale(scale: Float) {
        this.scale = scale
        updateBounds()
    }

    public fun getScale(): Float {
        return scale
    }

    private fun updateBounds() {
        if (composition == null) {
            return
        }
        val scale: Float = getScale()
        setBounds(0, 0, (composition!!.getBounds()!!.width * scale).toInt(),
            (composition!!.getBounds()!!.height * scale).toInt())
    }

    public fun setProgress(progress: Float) {
        if (composition == null) {
            lazyCompositionTasks.add(object : LazyCompositionTask {
                override fun run(composition: LottieComposition?) {
                    setProgress(progress)
                }
            })
            return
        }
        L.beginSection("Drawable#setProgress")
        animator.setFrame(MiscUtils.lerp(composition!!.startFrame, composition!!.endFrame, progress))
        L.endSection("Drawable#setProgress")
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
            this, LayerParser.parse(composition!!), composition!!.getLayers(), composition!!
        )
        if (outlineMasksAndMattes) {
            compositionLayer!!.setOutlineMasksAndMattes(true)
        }
    }
}
