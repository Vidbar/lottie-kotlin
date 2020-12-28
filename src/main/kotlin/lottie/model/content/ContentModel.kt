package lottie.model.content

import lottie.LottieDrawable
import lottie.animation.content.Content
import lottie.model.layer.BaseLayer

public interface ContentModel {
    public fun toContent(drawable: LottieDrawable, layer: BaseLayer): Content?
}
