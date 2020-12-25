package lottie.parser.moshi


public const val EMPTY_ARRAY: Int = 1

public const val NONEMPTY_ARRAY: Int = 2

public const val EMPTY_OBJECT: Int = 3

public const val DANGLING_NAME: Int = 4

public const val NONEMPTY_OBJECT: Int = 5

public const val EMPTY_DOCUMENT: Int = 6

public const val NONEMPTY_DOCUMENT: Int = 7

public const val CLOSED: Int = 8

public fun getPath(stackSize: Int, stack: IntArray, pathNames: Array<String?>, pathIndices: IntArray): String {
    val result = StringBuilder().append('$')
    for (i in 0 until stackSize) {
        when (stack[i]) {
            EMPTY_ARRAY, NONEMPTY_ARRAY -> result.append(
                '['
            ).append(
                pathIndices[i]
            ).append(']')
            EMPTY_OBJECT, DANGLING_NAME, NONEMPTY_OBJECT -> {
                result.append('.')
                if (pathNames[i] != null) {
                    result.append(pathNames[i])
                }
            }
            NONEMPTY_DOCUMENT, EMPTY_DOCUMENT, CLOSED -> {
            }
        }
    }
    return result.toString()
}
