package lottie.parser.moshi

import okio.Buffer
import okio.BufferedSource

public class JsonUtf8Reader(private val source: BufferedSource) : JsonReader() {
    public companion object {
        public const val PEEKED_NONE: Int = 0
    }

    private var peeked: Int = PEEKED_NONE

    private val buffer: Buffer = source.buffer

    init {
        pushScope(EMPTY_DOCUMENT)
    }


    override fun close() {
        peeked = PEEKED_NONE
        scopes[0] = CLOSED
        stackSize = 1
        buffer.clear()
        source.close()
    }


}
