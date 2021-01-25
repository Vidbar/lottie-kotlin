package android

public abstract class Drawable {
    public interface Callback {
        public fun invalidateDrawable(who: Drawable)
        public fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long)
        public fun unscheduleDrawable(who: Drawable, what: Runnable)
    }

    public open fun invalidateSelf() {
        TODO("Not yet implemented")
    }

    public fun getCallback(): Callback {
        TODO("Not yet implemented")
    }

    public fun setCallback(cb: Callback) {
        TODO("Not yet implemented")
    }

    public fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        TODO("Not yet implemented")
    }
}
