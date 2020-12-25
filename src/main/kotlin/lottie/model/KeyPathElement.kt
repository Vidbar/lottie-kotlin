package lottie.model

import lottie.value.LottieValueCallback

public interface KeyPathElement {
    public fun resolveKeyPath(keyPath: KeyPath, depth: Int, accumulator: List<KeyPath>, currentPartialKeyPath: KeyPath)
    public fun <T> addValueCallback(property: T, callback: LottieValueCallback<T>?)
}
