package lottie.animation.content

import android.Canvas
import android.Matrix
import android.RectF

public interface DrawingContent : Content {
    public fun draw(canvas: Canvas, parentMatrix: Matrix, alpha: Int)
    public fun getBounds(outBounds: RectF, parentMatrix: Matrix, applyParents: Boolean)
}
