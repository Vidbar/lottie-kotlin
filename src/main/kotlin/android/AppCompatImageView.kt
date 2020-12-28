package android

public open class AppCompatImageView: ImageView() {
    public fun isInEditMode(): Boolean = false
    public fun getContext():Context = Context()
    public open fun setImageResource(resId: Int) {
        TODO("Not yet implemented")
    }
}
