package lottie.model

public class Marker(private val name: String, public val startFrame: Float, public val durationFrames: Float) {
    public fun matchesName(name: String?): Boolean {
        if (this.name.equals(name, ignoreCase = true)) {
            return true
        }

        return this.name.endsWith(CARRIAGE_RETURN) && this.name.substring(0, this.name.length - 1)
            .equals(name, ignoreCase = true)
    }

    public companion object {
        private const val CARRIAGE_RETURN = "\r"
    }
}
