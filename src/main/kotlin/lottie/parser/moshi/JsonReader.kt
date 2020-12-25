package lottie.parser.moshi

import okio.BufferedSource
import java.io.Closeable
import java.util.*

public abstract class JsonReader : Closeable {
    protected var stackSize: Int = 0
    protected var scopes: IntArray = IntArray(32)
    protected var pathNames: Array<String?> = arrayOfNulls(32)
    protected var pathIndices: IntArray = IntArray(32)

    public fun pushScope(newTop: Int) {
        if (stackSize == scopes.size) {
            if (stackSize == 256) {
                throw Error("Nesting too deep at " + getPath())
            }
            scopes = scopes.copyOf(scopes.size * 2)
            //pathNames = pathNames.copyOf<String>(pathNames.size * 2)
            pathIndices = pathIndices.copyOf(pathIndices.size * 2)
        }
        scopes[stackSize++] = newTop
    }

    public fun getPath(): String {
        return getPath(stackSize, scopes, pathNames, pathIndices)
    }
}

public fun of(source: BufferedSource): JsonReader {
    return JsonUtf8Reader(source)
}