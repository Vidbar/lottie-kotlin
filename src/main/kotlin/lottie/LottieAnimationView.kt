package lottie

import android.AppCompatImageView
import java.util.concurrent.Callable

public class LottieAnimationView: AppCompatImageView() {
    private var animationName: String? = null
    private var animationResId = 0
    private val cacheComposition = true

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
}