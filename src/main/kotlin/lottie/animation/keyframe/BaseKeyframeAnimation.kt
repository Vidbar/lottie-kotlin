package lottie.animation.keyframe

import lottie.L
import lottie.value.Keyframe
import lottie.value.LottieValueCallback
import java.util.ArrayList

public abstract class BaseKeyframeAnimation<K, A>(keyframes: List<Keyframe<K>>) {
    public interface AnimationListener {
        public fun onValueChanged()
    }

    public companion object {
        private fun <T> wrap(keyframes: List<Keyframe<T>>): KeyframesWrapper<T> {
            if (keyframes.isEmpty()) {
                return EmptyKeyframeWrapper()
            }
            return if (keyframes.size == 1) {
                SingleKeyframeWrapper(keyframes)
            } else KeyframesWrapperImpl(keyframes)
        }
    }

    private val keyframesWrapper: KeyframesWrapper<K> = wrap(keyframes)


    // This is not a Set because we don't want to create an iterator object on every setProgress.
    public val listeners: MutableList<AnimationListener> = ArrayList(1)
    private var isDiscrete = false
    protected var progress: Float = 0f
        set(value) {
            var currentValue = value
            if (keyframesWrapper.isEmpty) {
                return
            }
            if (currentValue < startDelayProgress) {
                currentValue = startDelayProgress
            } else if (currentValue > endProgress) {
                currentValue = endProgress
            }
            if (currentValue == field) {
                return
            }
            field = currentValue
            if (keyframesWrapper.isValueChanged(currentValue)) {
                notifyListeners()
            }
        }

    protected var valueCallback: LottieValueCallback<A>? = null
        set(value: LottieValueCallback<A>?) {
            field?.setAnimation(null)
            field = value
            value?.setAnimation(this)
        }

    private var cachedGetValue: A? = null
    private var cachedStartDelayProgress = -1f
    private var cachedEndProgress = -1f
    public fun setIsDiscrete() {
        isDiscrete = true
    }

    public fun addUpdateListener(listener: AnimationListener) {
        listeners.add(listener)
    }

    public fun notifyListeners() {
        for (i in listeners.indices) {
            listeners[i].onValueChanged()
        }
    }

    protected val currentKeyframe: Keyframe<K>
        get() {
            L.beginSection("BaseKeyframeAnimation#getCurrentKeyframe")
            val keyframe = keyframesWrapper.currentKeyframe
            L.endSection("BaseKeyframeAnimation#getCurrentKeyframe")
            return keyframe
        }


    public val linearCurrentKeyframeProgress: Float
        get() {
            if (isDiscrete) {
                return 0f
            }
            val keyframe = currentKeyframe
            if (keyframe.isStatic) {
                return 0f
            }
            val progressIntoFrame: Float = progress - keyframe.startProgress
            val keyframeProgress: Float = keyframe.endProgress - keyframe.startProgress
            return progressIntoFrame / keyframeProgress
        }

    protected val interpolatedCurrentKeyframeProgress: Float
        get() {
            val keyframe = currentKeyframe
            return if (keyframe.isStatic) {
                0f
            } else keyframe.interpolator!!.getInterpolation(linearCurrentKeyframeProgress)
        }

    private val startDelayProgress: Float
        get() {
            if (cachedStartDelayProgress == -1f) {
                cachedStartDelayProgress = keyframesWrapper.startDelayProgress
            }
            return cachedStartDelayProgress
        }

    public val endProgress: Float
        get() {
            if (cachedEndProgress == -1f) {
                cachedEndProgress = keyframesWrapper.endProgress
            }
            return cachedEndProgress
        }

    public val value: A?
        get() {
            val progress = interpolatedCurrentKeyframeProgress
            if (valueCallback == null && keyframesWrapper.isCachedValueEnabled(progress)) {
                return cachedGetValue
            }
            val keyframe = currentKeyframe
            val value = getValue(keyframe, progress)
            cachedGetValue = value
            return value
        }

    public abstract fun getValue(keyframe: Keyframe<K>?, keyframeProgress: Float): A

    private interface KeyframesWrapper<T> {
        val isEmpty: Boolean

        fun isValueChanged(progress: Float): Boolean
        val currentKeyframe: Keyframe<T>

        val startDelayProgress: Float

        val endProgress: Float

        fun isCachedValueEnabled(interpolatedProgress: Float): Boolean
    }

    private class EmptyKeyframeWrapper<T> : KeyframesWrapper<T> {
        override val isEmpty: Boolean
            get() = true

        override fun isValueChanged(progress: Float): Boolean {
            return false
        }

        override val currentKeyframe: Keyframe<T>
            get() {
                throw IllegalStateException("not implemented")
            }
        override val startDelayProgress: Float
            get() = 0f
        override val endProgress: Float
            get() = 1f

        override fun isCachedValueEnabled(interpolatedProgress: Float): Boolean {
            throw IllegalStateException("not implemented")
        }
    }

    private class SingleKeyframeWrapper<T>(keyframes: List<Keyframe<T>>) : KeyframesWrapper<T> {
        override val currentKeyframe: Keyframe<T> = keyframes[0]
        private var cachedInterpolatedProgress = -1f
        override val isEmpty: Boolean
            get() = false

        override fun isValueChanged(progress: Float): Boolean {
            return !currentKeyframe.isStatic
        }

        override val startDelayProgress: Float
            get() = currentKeyframe.startProgress
        override val endProgress: Float
            get() = currentKeyframe.endProgress

        override fun isCachedValueEnabled(interpolatedProgress: Float): Boolean {
            if (cachedInterpolatedProgress == interpolatedProgress) {
                return true
            }
            cachedInterpolatedProgress = interpolatedProgress
            return false
        }

    }

    private class KeyframesWrapperImpl<T>(private val keyframes: List<Keyframe<T>>) : KeyframesWrapper<T> {
        override lateinit var currentKeyframe: Keyframe<T>
            private set
        private var cachedCurrentKeyframe: Keyframe<T>? = null
        private var cachedInterpolatedProgress = -1f
        override val isEmpty: Boolean
            get() = false

        override fun isValueChanged(progress: Float): Boolean {
            if (currentKeyframe.containsProgress(progress)) {
                return !currentKeyframe.isStatic
            }
            currentKeyframe = findKeyframe(progress)
            return true
        }

        private fun findKeyframe(progress: Float): Keyframe<T> {
            var keyframe = keyframes[keyframes.size - 1]
            if (progress >= keyframe.startProgress) {
                return keyframe
            }
            for (i in keyframes.size - 2 downTo 1) {
                keyframe = keyframes[i]
                if (currentKeyframe === keyframe) {
                    continue
                }
                if (keyframe.containsProgress(progress)) {
                    return keyframe
                }
            }
            return keyframes[0]
        }

        override val startDelayProgress: Float
            get() = keyframes[0].startProgress
        override val endProgress: Float
            get() = keyframes[keyframes.size - 1].endProgress

        override fun isCachedValueEnabled(interpolatedProgress: Float): Boolean {
            if (cachedCurrentKeyframe === currentKeyframe
                && cachedInterpolatedProgress == interpolatedProgress
            ) {
                return true
            }
            cachedCurrentKeyframe = currentKeyframe
            cachedInterpolatedProgress = interpolatedProgress
            return false
        }

        init {
            currentKeyframe = findKeyframe(0f)
        }
    }
}

