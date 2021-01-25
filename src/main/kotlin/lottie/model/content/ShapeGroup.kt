package lottie.model.content

import lottie.LottieDrawable
import lottie.animation.content.Content
import lottie.animation.content.ContentGroup
import lottie.model.layer.BaseLayer
import java.util.*

public class ShapeGroup(
    public val name: String,
    public val items: List<ContentModel>,
    public val isHidden: Boolean,
) : ContentModel {

    public override fun toContent(drawable: LottieDrawable, layer: BaseLayer): Content {
        return ContentGroup(drawable, layer, this)
    }

    override fun toString(): String {
        return "ShapeGroup{" + "name='" + name + "\' Shapes: " + items.toTypedArray().contentToString() + '}'
    }
}
