package lottie.utils

import android.Path
import android.PointF
import lottie.animation.content.KeyPathElementContent
import lottie.model.CubicCurveData
import lottie.model.KeyPath
import lottie.model.content.ShapeData

public object MiscUtils {
    private val pathFromDataCurrentPoint: PointF = PointF()
    public fun addPoints(p1: PointF, p2: PointF): PointF {
        return PointF(p1.x + p2.x, p1.y + p2.y)
    }

    public fun getPathFromData(shapeData: ShapeData, outPath: Path) {
        outPath.reset()
        val initialPoint: PointF = shapeData.getInitialPoint()
        outPath.moveTo(initialPoint.x, initialPoint.y)
        pathFromDataCurrentPoint.set(initialPoint.x, initialPoint.y)
        for (curveData in shapeData.getCurves()) {
            val cp1: PointF = curveData.getControlPoint1()
            val cp2: PointF = curveData.getControlPoint2()
            val vertex: PointF = curveData.getVertex()
            if (cp1 == pathFromDataCurrentPoint && cp2 == vertex) {
                outPath.lineTo(vertex.x, vertex.y)
            } else {
                outPath.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, vertex.x, vertex.y)
            }
            pathFromDataCurrentPoint.set(vertex.x, vertex.y)
        }
        if (shapeData.isClosed) {
            outPath.close()
        }
    }

    public fun lerp(a: Float, b: Float, percentage: Float): Float {
        return a + percentage * (b - a)
    }

    public fun lerp(a: Double, b: Double, percentage: Double): Double {
        return a + percentage * (b - a)
    }

    public fun lerp(a: Int, b: Int, percentage: Float): Int {
        return (a + percentage * (b - a)).toInt()
    }

    public fun floorMod(x: Float, y: Float): Int {
        return floorMod(x.toInt(), y.toInt())
    }

    private fun floorMod(x: Int, y: Int): Int {
        return x - y * floorDiv(x, y)
    }

    private fun floorDiv(x: Int, y: Int): Int {
        var r = x / y
        val sameSign = x xor y >= 0
        val mod = x % y
        if (!sameSign && mod != 0) {
            r--
        }
        return r
    }

    public fun clamp(number: Int, min: Int, max: Int): Int {
        return min.coerceAtLeast(max.coerceAtMost(number))
    }

    public fun clamp(number: Float, min: Float, max: Float): Float {
        return min.coerceAtLeast(max.coerceAtMost(number))
    }

    public fun clamp(number: Double, min: Double, max: Double): Double {
        return min.coerceAtLeast(max.coerceAtMost(number))
    }

    public fun contains(number: Float, rangeMin: Float, rangeMax: Float): Boolean {
        return number in rangeMin..rangeMax
    }

    public fun resolveKeyPath(
        keyPath: KeyPath,
        depth: Int,
        accumulator: MutableList<KeyPath>,
        currentPartialKeyPath: KeyPath,
        content: KeyPathElementContent,
    ) {
        if (keyPath.fullyResolvesTo(content.getName(), depth)) {
            val newKeyPath = currentPartialKeyPath.addKey(content.getName())
            accumulator.add(newKeyPath.resolve(content))
        }
    }
}

