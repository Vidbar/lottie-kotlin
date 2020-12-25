package lottie.animation.content

public interface Content {
    public fun getName(): String

    public fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>)
}
