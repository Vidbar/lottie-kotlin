package android

public class Choreographer private constructor() {
    public fun removeFrameCallback(callback: Choreographer.FrameCallback) {

    }

    public interface FrameCallback {
        public fun doFrame(frameTimeNanos: Long)
    }

    public companion object {
        public val instance: Choreographer = Choreographer()
    }
}
