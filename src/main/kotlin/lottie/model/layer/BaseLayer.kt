package lottie.model.layer

import lottie.animation.content.DrawingContent
import lottie.animation.keyframe.BaseKeyframeAnimation
import lottie.model.KeyPathElement

public abstract class BaseLayer: DrawingContent, BaseKeyframeAnimation.AnimationListener, KeyPathElement {

}
