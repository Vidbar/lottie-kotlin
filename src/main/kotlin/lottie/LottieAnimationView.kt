package lottie

import android.AppCompatImageView
import java.util.concurrent.Callable

public class LottieAnimationView: AppCompatImageView() {
    private var animationName: String? = null
    private var animationResId = 0
    private val cacheComposition = true
    private var composition: LottieComposition? = null
    private val lottieDrawable: LottieDrawable = LottieDrawable()
    private val compositionTask: LottieTask<LottieComposition>? = null
    private val loadedListener: LottieListener<LottieComposition> = object : LottieListener<LottieComposition> {
        override fun onResult(result: LottieComposition) {
            fun onResult(composition: LottieComposition?) {
                setComposition(composition)
            }
        }
    }

    public fun setAnimation(assetName: String) {
        this.animationName = assetName
        animationResId = 0
        setCompositionTask(fromAssets(assetName))
    }

    private fun fromAssets(assetName: String): LottieTask<LottieComposition> {
        return if (isInEditMode()) {
            val callable = Callable<LottieResult<LottieComposition>> {
                if (cacheComposition) fromAssetSync(getContext(), assetName)
                else fromAssetSync(getContext(), assetName, null)
            }
            LottieTask(callable, true)
        } else {
            if (cacheComposition) fromAsset(
                getContext(),
                assetName
            ) else fromAsset(getContext(), assetName, null)
        }
    }

    private fun setCompositionTask(compositionTask: LottieTask<LottieComposition>) {
        clearComposition()
        cancelLoaderTask()
        compositionTask = compositionTask
            .addListener(loadedListener)
            .addFailureListener(wrappedFailureListener)
    }

    private fun cancelLoaderTask() {
        if (compositionTask != null) {
            compositionTask.removeListener(loadedListener)
            compositionTask.removeFailureListener(wrappedFailureListener)
        }
    }

    public fun setComposition(composition: LottieComposition) {
        if (L.DBG) {
            Log.v(LottieAnimationView.TAG, "Set Composition \n$composition")
        }
        lottieDrawable.setCallback(this)
        this.composition = composition
        val isNewComposition: Boolean = lottieDrawable.setComposition(composition)
        enableOrDisableHardwareLayer()
        if (getDrawable() === lottieDrawable && !isNewComposition) {
            // We can avoid re-setting the drawable, and invalidating the view, since the composition
            // hasn't changed.
            return
        }

        // This is needed to makes sure that the animation is properly played/paused for the current visibility state.
        // It is possible that the drawable had a lazy composition task to play the animation but this view subsequently
        // became invisible. Comment this out and run the espresso tests to see a failing test.
        onVisibilityChanged(this, getVisibility())
        requestLayout()
        for (lottieOnCompositionLoadedListener in lottieOnCompositionLoadedListeners) {
            lottieOnCompositionLoadedListener.onCompositionLoaded(composition)
        }
    }

    private fun clearComposition() {
        composition = null
        lottieDrawable.clearComposition()
    }
}