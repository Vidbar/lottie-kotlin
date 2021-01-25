package lottie.utils

public class MeanCalculator {
    private var sum = 0f
    private var n = 0
    public fun add(number: Float) {
        sum += number
        n++
        if (n == Int.MAX_VALUE) {
            sum /= 2f
            n /= 2
        }
    }

    public val mean: Float
        get() = if (n == 0) {
            0F
        } else sum / n.toFloat()
}
