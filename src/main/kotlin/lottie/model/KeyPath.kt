package lottie.model

public class KeyPath(vararg keys: String) {
    public companion object {
        public val COMPOSITION: KeyPath = KeyPath("COMPOSITION")
    }

    private val keys: MutableList<String> = keys.toMutableList()

    public var resolvedElement: KeyPathElement? = null
        private set

    private constructor(keyPath: KeyPath) : this(*keyPath.keys.toTypedArray()) {
        resolvedElement = keyPath.resolvedElement
    }

    public fun addKey(key: String): KeyPath {
        val newKeyPath = KeyPath(this)
        newKeyPath.keys.add(key)
        return newKeyPath
    }

    public fun resolve(element: KeyPathElement?): KeyPath {
        val keyPath = KeyPath(this)
        keyPath.resolvedElement = element
        return keyPath
    }

    public fun matches(key: String, depth: Int): Boolean {
        if (isContainer(key)) {
            return true
        }
        if (depth >= keys.size) {
            return false
        }
        return keys[depth] == key || keys[depth] == "**" || keys[depth] == "*"
    }

    public fun incrementDepthBy(key: String, depth: Int): Int {
        if (isContainer(key)) {
            return 0
        }
        if (keys[depth] != "**") {
            return 1
        }
        if (depth == keys.size - 1) {
            return 0
        }
        return if (keys[depth + 1] == key) {
            2
        } else 0
    }

    public fun fullyResolvesTo(key: String, depth: Int): Boolean {
        if (depth >= keys.size) {
            return false
        }
        val isLastDepth = depth == keys.size - 1
        val keyAtDepth = keys[depth]
        val isGlobstar = keyAtDepth == "**"
        if (!isGlobstar) {
            val matches = keyAtDepth == key || keyAtDepth == "*"
            return (isLastDepth || depth == keys.size - 2 && endsWithGlobstar()) && matches
        }
        val isGlobstarButNextKeyMatches = !isLastDepth && keys[depth + 1] == key
        if (isGlobstarButNextKeyMatches) {
            return depth == keys.size - 2 ||
                    depth == keys.size - 3 && endsWithGlobstar()
        }
        if (isLastDepth) {
            return true
        }
        return if (depth + 1 < keys.size - 1) {
            false
        } else keys[depth + 1] == key
    }

    public fun propagateToChildren(key: String, depth: Int): Boolean {
        return if ("__container" == key) {
            true
        } else depth < keys.size - 1 || keys[depth] == "**"
    }

    private fun isContainer(key: String): Boolean {
        return "__container" == key
    }

    private fun endsWithGlobstar(): Boolean {
        return keys[keys.size - 1] == "**"
    }

    public fun keysToString(): String {
        return keys.toString()
    }

    override fun toString(): String {
        return "KeyPath{" + "keys=" + keys + ",resolved=" + (resolvedElement != null) + '}'
    }
}
