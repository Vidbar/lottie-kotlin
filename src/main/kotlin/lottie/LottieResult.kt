package lottie

public class LottieResult<V> private constructor(
    private val value: V? = null,
    private val exception: Throwable? = null
) {
    public constructor(value: V) : this(value, null)

    public constructor(exception: Throwable) : this(null, exception)

    public fun getValue(): V? {
        return value
    }

    public fun getException(): Throwable? {
        return exception
    }

    override fun hashCode(): Int {
        return arrayOf(getValue(), getException()).contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is LottieResult<*>) {
            return false
        }
        if (getValue() != null && getValue() == other.getValue()) {
            return true
        }
        return if (getException() != null && other.getException() != null) {
            getException().toString() == getException().toString()
        } else false
    }
}
