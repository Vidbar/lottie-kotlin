package lottie.model.content

import android.PointF
import lottie.model.CubicCurveData
import lottie.utils.Logger
import lottie.utils.MiscUtils

public class ShapeData(
    private val initialPoint: PointF,
    closed: Boolean,
    private val curves: MutableList<CubicCurveData>,
) {
    public constructor() : this(PointF(), false, mutableListOf())

    public var isClosed: Boolean = false
        private set

    private fun setInitialPoint(x: Float, y: Float) {
        initialPoint.set(x, y)
    }

    public fun getInitialPoint(): PointF {
        return initialPoint
    }

    public fun getCurves(): List<CubicCurveData> {
        return curves
    }

    public fun interpolateBetween(shapeData1: ShapeData, shapeData2: ShapeData, percentage: Float) {
        isClosed = shapeData1.isClosed || shapeData2.isClosed
        if (shapeData1.getCurves().size != shapeData2.getCurves().size) {
            Logger.warning("Curves must have the same number of control points. Shape 1: " +
                    shapeData1.getCurves().size + "\tShape 2: " + shapeData2.getCurves().size)
        }
        val points = shapeData1.getCurves().size.coerceAtMost(shapeData2.getCurves().size)
        if (curves.size < points) {
            for (i in curves.size until points) {
                curves.add(CubicCurveData())
            }
        } else if (curves.size > points) {
            for (i in curves.size - 1 downTo points) {
                curves.removeAt(curves.size - 1)
            }
        }
        val initialPoint1: PointF = shapeData1.getInitialPoint()
        val initialPoint2: PointF = shapeData2.getInitialPoint()
        setInitialPoint(MiscUtils.lerp(initialPoint1.x, initialPoint2.x, percentage),
            MiscUtils.lerp(initialPoint1.y, initialPoint2.y, percentage))
        for (i in curves.indices.reversed()) {
            val curve1: CubicCurveData = shapeData1.getCurves()[i]
            val curve2: CubicCurveData = shapeData2.getCurves()[i]
            val cp11: PointF = curve1.getControlPoint1()
            val cp21: PointF = curve1.getControlPoint2()
            val vertex1: PointF = curve1.getVertex()
            val cp12: PointF = curve2.getControlPoint1()
            val cp22: PointF = curve2.getControlPoint2()
            val vertex2: PointF = curve2.getVertex()
            curves[i].setControlPoint1(
                MiscUtils.lerp(cp11.x, cp12.x, percentage), MiscUtils.lerp(cp11.y, cp12.y,
                    percentage))
            curves[i].setControlPoint2(
                MiscUtils.lerp(cp21.x, cp22.x, percentage), MiscUtils.lerp(cp21.y, cp22.y,
                    percentage))
            curves[i].setVertex(
                MiscUtils.lerp(vertex1.x, vertex2.x, percentage), MiscUtils.lerp(vertex1.y, vertex2.y,
                    percentage))
        }
    }

    override fun toString(): String {
        return "ShapeData{" + "numCurves=" + curves.size +
                "closed=" + isClosed +
                '}'
    }
}

