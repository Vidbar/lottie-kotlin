package lottie.parser.moshi

import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import okio.Options
import java.io.Closeable
import java.io.IOException
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
    public class Options private constructor(public val strings: Array<String?>, public val doubleQuoteSuffix: okio.Options) {
        public companion object {
            public fun of(vararg strings: String?): Options {
                return try {
                    val result = arrayOfNulls<ByteString>(strings.size)
                    val buffer = Buffer()
                    for (i in strings.indices) {
                        JsonReader.string(buffer, strings[i])
                        buffer.readByte() // Skip the leading double quote (but leave the trailing one).
                        result[i] = buffer.readByteString()
                    }
                    Options(strings.clone(), of.of(*result))
                } catch (e: IOException) {
                    throw AssertionError(e)
                }
            }
        }
    }
}

public fun of(source: BufferedSource): JsonReader {
    return JsonUtf8Reader(source)
}