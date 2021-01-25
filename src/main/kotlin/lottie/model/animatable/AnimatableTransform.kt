package lottie.model.animatable

import android.PointF
import lottie.model.content.ContentModel

public class AnimatableTransform(
    public val anchorPoint: AnimatablePathValue? = null,
    public val position: AnimatableValue<PointF, PointF>? = null,
    public val scale: AnimatableScaleValue? = null,
    public val rotation: AnimatableFloatValue? = null,
    public val opacity: AnimatableIntegerValue? = null,
    public val startOpacity: AnimatableFloatValue? = null,
    public val endOpacity: AnimatableFloatValue? = null,
    public val skew: AnimatableFloatValue? = null,
    public val skewAngle: AnimatableFloatValue? = null,
) :
    ModifierContent, ContentModel {

    @Nullable
    private val position: AnimatableValue<PointF, PointF>

    @Nullable
    private val scale: AnimatableScaleValue?

    @Nullable
    private val rotation: AnimatableFloatValue?

    @Nullable
    private val opacity: AnimatableIntegerValue?

    @Nullable
    private val skew: AnimatableFloatValue?

    @Nullable
    private val skewAngle: AnimatableFloatValue?

    // Used for repeaters
    @Nullable
    private val startOpacity: AnimatableFloatValue?

    @Nullable
    private val endOpacity: AnimatableFloatValue?
    @Nullable
    fun getAnchorPoint(): AnimatablePathValue? {
        return anchorPoint
    }

    @Nullable
    fun getPosition(): AnimatableValue<PointF, PointF> {
        return position
    }

    @Nullable
    fun getScale(): AnimatableScaleValue? {
        return scale
    }

    @Nullable
    fun getRotation(): AnimatableFloatValue? {
        return rotation
    }

    @Nullable
    fun getOpacity(): AnimatableIntegerValue? {
        return opacity
    }

    @Nullable
    fun getStartOpacity(): AnimatableFloatValue? {
        return startOpacity
    }

    @Nullable
    fun getEndOpacity(): AnimatableFloatValue? {
        return endOpacity
    }

    @Nullable
    fun getSkew(): AnimatableFloatValue? {
        return skew
    }

    @Nullable
    fun getSkewAngle(): AnimatableFloatValue? {
        return skewAngle
    }

    fun createAnimation(): TransformKeyframeAnimation {
        return TransformKeyframeAnimation(this)
    }

    @Nullable
    fun toContent(drawable: LottieDrawable?, layer: BaseLayer?): Content? {
        return null
    }

    init {
        this.anchorPoint = anchorPoint
        this.position = position
        this.scale = scale
        this.rotation = rotation
        this.opacity = opacity
        this.startOpacity = startOpacity
        this.endOpacity = endOpacity
        this.skew = skew
        this.skewAngle = skewAngle
    }
}
