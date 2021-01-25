package lottie.model

import lottie.model.content.ShapeGroup

public class FontCharacter(
    shapes: List<ShapeGroup>, character: Char, size: Double,
    width: Double, style: String, fontFamily: String,
) {
    private val shapes: List<ShapeGroup>
    private val character: Char
    public val size: Double
    public val width: Double
    public val style: String
    private val fontFamily: String
    public fun getShapes(): List<ShapeGroup> {
        return shapes
    }

    override fun hashCode(): Int {
        return hashFor(character, fontFamily, style)
    }

    public companion object {
        public fun hashFor(character: Char, fontFamily: String, style: String): Int {
            var result = 0
            result = 31 * result + character.toInt()
            result = 31 * result + fontFamily.hashCode()
            result = 31 * result + style.hashCode()
            return result
        }
    }

    init {
        this.shapes = shapes
        this.character = character
        this.size = size
        this.width = width
        this.style = style
        this.fontFamily = fontFamily
    }
}
