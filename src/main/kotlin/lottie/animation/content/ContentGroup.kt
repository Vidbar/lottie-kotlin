package lottie.animation.content

import android.*
import lottie.LottieDrawable
import lottie.animation.keyframe.BaseKeyframeAnimation
import lottie.model.KeyPath
import lottie.model.KeyPathElement
import lottie.model.animatable.AnimatableTransform
import lottie.model.content.ContentModel
import lottie.model.content.ShapeGroup
import lottie.model.layer.BaseLayer
import lottie.utils.Utils
import lottie.value.LottieValueCallback
import java.util.ArrayList

public class ContentGroup internal constructor(
    private val lottieDrawable: LottieDrawable,
    layer: BaseLayer,
    private val name: String,
    private val hidden: Boolean,
    private val contents: List<Content>,
    transform: AnimatableTransform?,
) :
    DrawingContent, PathContent, BaseKeyframeAnimation.AnimationListener, KeyPathElement {
    private val offScreenPaint: Paint = LPaint()
    private val offScreenRectF: RectF = RectF()
    private val matrix: Matrix = Matrix()
    private val path: Path = Path()
    private val rect: RectF = RectF()

    private var pathContents: MutableList<PathContent>? = null

    private var transformAnimation: TransformKeyframeAnimation? = null

    public constructor(lottieDrawable: LottieDrawable, layer: BaseLayer, shapeGroup: ShapeGroup) : this(lottieDrawable,
        layer,
        shapeGroup.name,
        shapeGroup.isHidden,
        contentsFromModels(lottieDrawable, layer, shapeGroup.items),
        findTransform(shapeGroup.items)) {
    }

    override fun onValueChanged() {
        lottieDrawable.invalidateSelf()
    }

    override fun getName(): String {
        return name
    }

    public override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        // Do nothing with contents after.
        val myContentsBefore: MutableList<Content> = ArrayList(contentsBefore.size + contents.size)
        myContentsBefore.addAll(contentsBefore)
        for (i in contents.indices.reversed()) {
            val content = contents[i]
            content.setContents(myContentsBefore, contents.subList(0, i))
            myContentsBefore.add(content)
        }
    }

    public val pathList: List<Any>
        get() {
            if (pathContents == null) {
                pathContents = ArrayList<PathContent>()
                for (i in contents.indices) {
                    val content = contents[i]
                    if (content is PathContent) {
                        pathContents!!.add(content as PathContent)
                    }
                }
            }
            return pathContents
        }
    public val transformationMatrix: Matrix
        get() {
            if (transformAnimation != null) {
                return transformAnimation.getMatrix()
            }
            matrix.reset()
            return matrix
        }

    public fun getPath(): Path {
        // TODO: cache this somehow.
        matrix.reset()
        if (transformAnimation != null) {
            matrix.set(transformAnimation.getMatrix())
        }
        path.reset()
        if (hidden) {
            return path
        }
        for (i in contents.indices.reversed()) {
            val content = contents[i]
            if (content is PathContent) {
                path.addPath((content as PathContent).getPath(), matrix)
            }
        }
        return path
    }

    public override fun draw(canvas: Canvas, parentMatrix: Matrix, alpha: Int) {
        if (hidden) {
            return
        }
        matrix.set(parentMatrix)
        val layerAlpha: Int = if (transformAnimation != null) {
            matrix.preConcat(transformAnimation.getMatrix())
            val opacity =
                if (transformAnimation.getOpacity() == null) 100 else transformAnimation.getOpacity().getValue()
            (opacity / 100f * alpha / 255f * 255).toInt()
        } else {
            alpha
        }

        // Apply off-screen rendering only when needed in order to improve rendering performance.
        val isRenderingWithOffScreen =
            lottieDrawable.isApplyingOpacityToLayersEnabled() && hasTwoOrMoreDrawableContent() && layerAlpha != 255
        if (isRenderingWithOffScreen) {
            offScreenRectF.set(0, 0, 0, 0)
            getBounds(offScreenRectF, matrix, true)
            offScreenPaint.setAlpha(layerAlpha)
            Utils.saveLayerCompat(canvas, offScreenRectF, offScreenPaint)
        }
        val childAlpha = if (isRenderingWithOffScreen) 255 else layerAlpha
        for (i in contents.indices.reversed()) {
            val content: Any = contents[i]
            if (content is DrawingContent) {
                content.draw(canvas, matrix, childAlpha)
            }
        }
        if (isRenderingWithOffScreen) {
            canvas.restore()
        }
    }

    private fun hasTwoOrMoreDrawableContent(): Boolean {
        var drawableContentCount = 0
        for (i in contents.indices) {
            if (contents[i] is DrawingContent) {
                drawableContentCount += 1
                if (drawableContentCount >= 2) {
                    return true
                }
            }
        }
        return false
    }

    override fun getBounds(outBounds: RectF, parentMatrix: Matrix?, applyParents: Boolean) {
        matrix.set(parentMatrix)
        if (transformAnimation != null) {
            matrix.preConcat(transformAnimation.getMatrix())
        }
        rect.set(0, 0, 0, 0)
        for (i in contents.indices.reversed()) {
            val content = contents[i]
            if (content is DrawingContent) {
                content.getBounds(rect, matrix, applyParents)
                outBounds.union(rect)
            }
        }
    }

    public fun resolveKeyPath(
        keyPath: KeyPath, depth: Int, accumulator: MutableList<KeyPath>, currentPartialKeyPath: KeyPath,
    ) {
        var currentPartialKeyPath: KeyPath = currentPartialKeyPath
        if (!keyPath.matches(getName(), depth)) {
            return
        }
        if ("__container" != getName()) {
            currentPartialKeyPath = currentPartialKeyPath.addKey(getName())
            if (keyPath.fullyResolvesTo(getName(), depth)) {
                accumulator.add(currentPartialKeyPath.resolve(this))
            }
        }
        if (keyPath.propagateToChildren(getName(), depth)) {
            val newDepth: Int = depth + keyPath.incrementDepthBy(getName(), depth)
            for (i in contents.indices) {
                val content = contents[i]
                if (content is KeyPathElement) {
                    val element: KeyPathElement = content as KeyPathElement
                    element.resolveKeyPath(keyPath, newDepth, accumulator, currentPartialKeyPath)
                }
            }
        }
    }

    public override fun <T> addValueCallback(property: T, callback: LottieValueCallback<T>?) {
        if (transformAnimation != null) {
            transformAnimation.applyValueCallback(property, callback)
        }
    }

    companion object {
        private fun contentsFromModels(
            drawable: LottieDrawable, layer: BaseLayer,
            contentModels: List<ContentModel>,
        ): List<Content> {
            val contents: MutableList<Content> = ArrayList(contentModels.size)
            for (i in contentModels.indices) {
                val content: Content = contentModels[i].toContent(drawable, layer)
                if (content != null) {
                    contents.add(content)
                }
            }
            return contents
        }

        public fun findTransform(contentModels: List<ContentModel?>): AnimatableTransform? {
            for (i in contentModels.indices) {
                val contentModel: ContentModel? = contentModels[i]
                if (contentModel is AnimatableTransform) {
                    return contentModel as AnimatableTransform?
                }
            }
            return null
        }
    }

    init {
        if (transform != null) {
            transformAnimation = transform.createAnimation()
            transformAnimation.addAnimationsToLayer(layer)
            transformAnimation.addListener(this)
        }
        val greedyContents: MutableList<GreedyContent> = ArrayList<GreedyContent>()
        for (i in contents.indices.reversed()) {
            val content = contents[i]
            if (content is GreedyContent) {
                greedyContents.add(content as GreedyContent)
            }
        }
        for (i in greedyContents.indices.reversed()) {
            greedyContents[i].absorbContent(contents.listIterator(contents.size))
        }
    }
}

