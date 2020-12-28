package android

public open class View : Drawable.Callback {
    public companion object {
        public const val LAYER_TYPE_NONE: Int = 0x00000000
        public const val LAYER_TYPE_SOFTWARE: Int = 0x00000001
        public const val LAYER_TYPE_HARDWARE: Int = 0x00000002
    }

    override fun invalidateDrawable(who: Drawable) {
        TODO("Not yet implemented")
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        TODO("Not yet implemented")
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        TODO("Not yet implemented")
    }

    public fun getLayerType(): Int {
        TODO("Not yet implemented")
    }

    public fun setLayerType(layerType: Int, paint: Paint?): Int {
        TODO("Not yet implemented")
    }

    public fun getVisibility(): Int {
        TODO("Not yet implemented")
    }

    public fun requestLayout() {
        TODO("Not yet implemented")
    }
}
