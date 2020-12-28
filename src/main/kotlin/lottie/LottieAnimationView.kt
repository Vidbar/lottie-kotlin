package lottie

import android.AppCompatImageView
import android.Build
import android.Log
import android.View
import lottie.utils.Logger
import lottie.utils.Utils
import java.util.HashSet
import java.util.concurrent.Callable

public class LottieAnimationView : AppCompatImageView() {
    private val TAG = LottieAnimationView::class.java.simpleName

    private var animationName: String? = null
    private var animationResId = 0
    private val cacheComposition = true
    private var composition: LottieComposition? = null
    private val lottieDrawable: LottieDrawable = LottieDrawable()
    private var compositionTask: LottieTask<LottieComposition>? = null
    private val renderMode: RenderMode = RenderMode.AUTOMATIC
    private val lottieOnCompositionLoadedListeners: Set<LottieOnCompositionLoadedListener> =
        HashSet<LottieOnCompositionLoadedListener>()

    private val defaultFailureListener: LottieListener<Throwable> = object : LottieListener<Throwable> {
        override fun onResult(throwable: Throwable) {
            if (Utils.isNetworkException(throwable)) {
                Logger.warning("Unable to load composition.", throwable)
                return
            }
            throw IllegalStateException("Unable to parse composition", throwable)
        }
    }

    private val loadedListener: LottieListener<LottieComposition> = object : LottieListener<LottieComposition> {
        override fun onResult(result: LottieComposition) {
            setComposition(result)
        }
    }

    private val wrappedFailureListener: LottieListener<Throwable> = object : LottieListener<Throwable> {
        override fun onResult(result: Throwable) {
            if (fallbackResource != 0) {
                setImageResource(fallbackResource)
            }
            val l: LottieListener<Throwable> =
                failureListener ?: defaultFailureListener
            l.onResult(result)
        }
    }

    private val failureListener: LottieListener<Throwable>? = null
    private val fallbackResource = 0

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
        this.compositionTask = compositionTask
            .addListener(loadedListener)
            .addFailureListener(wrappedFailureListener)
    }

    private fun cancelLoaderTask() {
        compositionTask?.let {
            it.removeListener(loadedListener)
            it.removeFailureListener(wrappedFailureListener)
        }
    }

    public fun setComposition(composition: LottieComposition) {
        if (L.DBG) {
            Log.v(TAG, "Set Composition \n$composition")
        }
        lottieDrawable.setCallback(this)
        this.composition = composition
        val isNewComposition: Boolean = lottieDrawable.setComposition(composition)
        enableOrDisableHardwareLayer()
        if (getDrawable() === lottieDrawable && !isNewComposition) {
            return
        }

        onVisibilityChanged(this, getVisibility())
        requestLayout()
        for (lottieOnCompositionLoadedListener in lottieOnCompositionLoadedListeners) {
            lottieOnCompositionLoadedListener.onCompositionLoaded(composition)
        }
    }

    protected /*override*/ fun onVisibilityChanged(changedView: View, visibility: Int) {
        /*TODO
        if (!isInitialized) {
            return
        }
        if (isShown()) {
            if (wasAnimatingWhenNotShown) {
                resumeAnimation()
            } else if (playAnimationWhenShown) {
                playAnimation()
            }
            wasAnimatingWhenNotShown = false
            playAnimationWhenShown = false
        } else {
            if (isAnimating()) {
                pauseAnimation()
                wasAnimatingWhenNotShown = true
            }
        }*/
    }

    private fun clearComposition() {
        composition = null
        lottieDrawable.clearComposition()
    }

    public override fun setImageResource(resId: Int) {
        cancelLoaderTask()
        super.setImageResource(resId)
    }

    private fun enableOrDisableHardwareLayer() {
        var layerType: Int = LAYER_TYPE_SOFTWARE
        when (renderMode) {
            RenderMode.HARDWARE -> layerType = LAYER_TYPE_HARDWARE
            RenderMode.SOFTWARE -> layerType = LAYER_TYPE_SOFTWARE
            RenderMode.AUTOMATIC -> {
                var useHardwareLayer = true
                if (composition?.hasDashPattern == true && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    useHardwareLayer = false
                } else if (composition != null && composition!!.maskAndMatteCount > 4) {
                    useHardwareLayer = false
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    useHardwareLayer = false
                } else if (Build.VERSION.SDK_INT === Build.VERSION_CODES.N || Build.VERSION.SDK_INT === Build.VERSION_CODES.N_MR1) {
                    useHardwareLayer = false
                }
                layerType = if (useHardwareLayer) LAYER_TYPE_HARDWARE else LAYER_TYPE_SOFTWARE
            }
        }
        if (layerType != getLayerType()) {
            setLayerType(layerType, null)
        }
    }
}