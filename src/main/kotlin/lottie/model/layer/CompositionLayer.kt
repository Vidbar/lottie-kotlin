package lottie.model.layer

import lottie.LottieComposition
import lottie.LottieDrawable

public class CompositionLayer(
    lottieDrawable: LottieDrawable, layerModel: Layer, layerModels: List<Layer>,
    composition: LottieComposition
) : BaseLayer(lottieDrawable, layerModel) {

    private var timeRemapping: BaseKeyframeAnimation<Float, Float>? = null
    private val layers: List<BaseLayer?> = ArrayList<BaseLayer?>()
    private val rect: RectF = RectF()
    private val newClipRect: RectF = RectF()
    private val layerPaint: Paint = Paint()

    @Nullable
    private var hasMatte: Boolean? = null

    @Nullable
    private var hasMasks: Boolean? = null
    fun setOutlineMasksAndMattes(outline: Boolean) {
        super.setOutlineMasksAndMattes(outline)
        for (layer in layers) {
            layer.setOutlineMasksAndMattes(outline)
        }
    }

    fun drawLayer(canvas: Canvas, parentMatrix: Matrix, parentAlpha: Int) {
        L.beginSection("CompositionLayer#draw")
        newClipRect.set(0, 0, layerModel.getPreCompWidth(), layerModel.getPreCompHeight())
        parentMatrix.mapRect(newClipRect)

        // Apply off-screen rendering only when needed in order to improve rendering performance.
        val isDrawingWithOffScreen =
            lottieDrawable.isApplyingOpacityToLayersEnabled() && layers.size > 1 && parentAlpha != 255
        if (isDrawingWithOffScreen) {
            layerPaint.setAlpha(parentAlpha)
            Utils.saveLayerCompat(canvas, newClipRect, layerPaint)
        } else {
            canvas.save()
        }
        val childAlpha = if (isDrawingWithOffScreen) 255 else parentAlpha
        for (i in layers.indices.reversed()) {
            var nonEmptyClip = true
            if (!newClipRect.isEmpty()) {
                nonEmptyClip = canvas.clipRect(newClipRect)
            }
            if (nonEmptyClip) {
                val layer: BaseLayer? = layers[i]
                layer.draw(canvas, parentMatrix, childAlpha)
            }
        }
        canvas.restore()
        L.endSection("CompositionLayer#draw")
    }

    fun getBounds(outBounds: RectF, parentMatrix: Matrix?, applyParents: Boolean) {
        super.getBounds(outBounds, parentMatrix, applyParents)
        for (i in layers.indices.reversed()) {
            rect.set(0, 0, 0, 0)
            layers[i].getBounds(rect, boundsMatrix, true)
            outBounds.union(rect)
        }
    }

    fun setProgress(@FloatRange(from = 0f, to = 1f) progress: Float) {
        var progress = progress
        super.setProgress(progress)
        if (timeRemapping != null) {
            // The duration has 0.01 frame offset to show end of animation properly.
            // https://github.com/airbnb/lottie-android/pull/766
            // Ignore this offset for calculating time-remapping because time-remapping value is based on original duration.
            val durationFrames: Float = lottieDrawable.getComposition().getDurationFrames() + 0.01f
            val compositionDelayFrames: Float = layerModel.getComposition().getStartFrame()
            val remappedFrames: Float =
                timeRemapping.getValue() * layerModel.getComposition().getFrameRate() - compositionDelayFrames
            progress = remappedFrames / durationFrames
        }
        if (timeRemapping == null) {
            progress -= layerModel.getStartProgress()
        }
        if (layerModel.getTimeStretch() !== 0) {
            progress /= layerModel.getTimeStretch()
        }
        for (i in layers.indices.reversed()) {
            layers[i].setProgress(progress)
        }
    }

    fun hasMasks(): Boolean {
        if (hasMasks == null) {
            for (i in layers.indices.reversed()) {
                val layer: BaseLayer? = layers[i]
                if (layer is ShapeLayer) {
                    if (layer.hasMasksOnThisLayer()) {
                        hasMasks = true
                        return true
                    }
                } else if (layer is CompositionLayer && (layer as CompositionLayer?)!!.hasMasks()) {
                    hasMasks = true
                    return true
                }
            }
            hasMasks = false
        }
        return hasMasks!!
    }

    fun hasMatte(): Boolean {
        if (hasMatte == null) {
            if (hasMatteOnThisLayer()) {
                hasMatte = true
                return true
            }
            for (i in layers.indices.reversed()) {
                if (layers[i].hasMatteOnThisLayer()) {
                    hasMatte = true
                    return true
                }
            }
            hasMatte = false
        }
        return hasMatte
    }

    protected fun resolveChildKeyPath(
        keyPath: KeyPath?, depth: Int, accumulator: List<KeyPath?>?,
        currentPartialKeyPath: KeyPath?
    ) {
        for (i in layers.indices) {
            layers[i].resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath)
        }
    }

    fun <T> addValueCallback(property: T, @Nullable callback: LottieValueCallback<T?>?) {
        super.addValueCallback(property, callback)
        if (property === LottieProperty.TIME_REMAP) {
            if (callback == null) {
                if (timeRemapping != null) {
                    timeRemapping.setValueCallback(null)
                }
            } else {
                timeRemapping = ValueCallbackKeyframeAnimation(callback as LottieValueCallback<Float?>?)
                timeRemapping.addUpdateListener(this)
                addAnimation(timeRemapping)
            }
        }
    }

    init {
        val timeRemapping: AnimatableFloatValue = layerModel.getTimeRemapping()
        if (timeRemapping != null) {
            this.timeRemapping = timeRemapping.createAnimation()
            addAnimation(this.timeRemapping)
            this.timeRemapping.addUpdateListener(this)
        } else {
            this.timeRemapping = null
        }
        val layerMap: LongSparseArray<BaseLayer> = LongSparseArray(composition.getLayers().size())
        var mattedLayer: BaseLayer? = null
        for (i in layerModels.indices.reversed()) {
            val lm: Layer = layerModels[i]
            val layer: BaseLayer = BaseLayer.forModel(lm, lottieDrawable, composition) ?: continue
            layerMap.put(layer.getLayerModel().getId(), layer)
            if (mattedLayer != null) {
                mattedLayer.setMatteLayer(layer)
                mattedLayer = null
            } else {
                layers.add(0, layer)
                when (lm.getMatteType()) {
                    ADD, INVERT -> mattedLayer = layer
                }
            }
        }
        for (i in 0 until layerMap.size()) {
            val key: Long = layerMap.keyAt(i)
            val layerView: BaseLayer = layerMap.get(key) ?: continue
            // This shouldn't happen but it appears as if sometimes on pre-lollipop devices when
            // compiled with d8, layerView is null sometimes.
            // https://github.com/airbnb/lottie-android/issues/524
            val parentLayer: BaseLayer = layerMap.get(layerView.getLayerModel().getParentId())
            if (parentLayer != null) {
                layerView.setParentLayer(parentLayer)
            }
        }
    }
}