package lottie.model

import android.PointF

public class CubicCurveData(
    private val controlPoint1: PointF,
    private val controlPoint2: PointF,
    private val vertex: PointF,
) {

    public constructor() : this(
        PointF(),
        PointF(),
        PointF()
    )

    public fun setControlPoint1(x: Float, y: Float) {
        controlPoint1.set(x, y)
    }

    public fun getControlPoint1(): PointF {
        return controlPoint1
    }

    public fun setControlPoint2(x: Float, y: Float) {
        controlPoint2.set(x, y)
    }

    public fun getControlPoint2(): PointF {
        return controlPoint2
    }

    public fun setVertex(x: Float, y: Float) {
        vertex.set(x, y)
    }

    public fun getVertex(): PointF {
        return vertex
    }
}
