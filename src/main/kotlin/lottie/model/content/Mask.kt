package lottie.model.content

import lottie.model.animatable.AnimatableShapeValue

public class Mask(
    public val maskMode: MaskMode,
    public val maskPath: AnimatableShapeValue,
    public val opacity: AnimatableIntegerValue,
    public val inverted: Boolean
) {
    public enum class MaskMode {
        MASK_MODE_ADD, MASK_MODE_SUBTRACT, MASK_MODE_INTERSECT, MASK_MODE_NONE
    }
}
