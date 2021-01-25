package lottie.utils

import android.Choreographer
import lottie.L
import lottie.LottieComposition

/*public open class LottieValueAnimator : BaseLottieAnimator() {
    protected var running: Boolean = false
    private var composition: LottieComposition? = null
    private var minFrame = Int.MIN_VALUE.toFloat()
    private var maxFrame = Int.MAX_VALUE.toFloat()

    public override fun isRunning(): Boolean {
        return running
    }

    override fun cancel() {
        notifyCancel()
        removeFrameCallback()
    }

    protected open fun removeFrameCallback() {
        removeFrameCallback(true)
    }

    protected open fun removeFrameCallback(stopRunning: Boolean) {
        Choreographer.instance.removeFrameCallback(this)
        if (stopRunning) {
            running = false
        }
    }

    public fun clearComposition() {
        this.composition = null
        minFrame = Int.MIN_VALUE.toFloat()
        maxFrame = Int.MAX_VALUE.toFloat()
    }
}*/

public open class LottieValueAnimator : BaseLottieAnimator(), Choreographer.FrameCallback {
    public var speed: Float = 1f
    private var speedReversedForRepeatMode = false
    private var lastFrameTimeNs: Long = 0
    private var frame = 0f
    private var repeatCount = 0
    private var minFrame = Int.MIN_VALUE.toFloat()
    private var maxFrame = Int.MAX_VALUE.toFloat()

    private var composition: LottieComposition? = null

    protected var running: Boolean = false

    public val animatedValue: Any
        get() = animatedValueAbsolute

    public val animatedValueAbsolute: Float
        get() = if (composition == null) {
            0F
        } else (frame - composition!!.getStartFrame()) / (composition.getEndFrame() - composition!!.getStartFrame())

    public val animatedFraction: Float
        get() {
            if (composition == null) {
                return 0F
            }
            return if (isReversed) {
                (getMaxFrame() - frame) / (getMaxFrame() - getMinFrame())
            } else {
                (frame - getMinFrame()) / (getMaxFrame() - getMinFrame())
            }
        }

    public val duration: Long
        get() = if (composition == null) 0 else composition.getDuration() as Long

    public fun getFrame(): Float {
        return frame
    }

    override fun isRunning(): Boolean {
        return running
    }

    override fun doFrame(frameTimeNanos: Long) {
        postFrameCallback()
        if (composition == null || !isRunning()) {
            return
        }
        L.beginSection("LottieValueAnimator#doFrame")
        val now = frameTimeNanos
        val timeSinceFrame = if (lastFrameTimeNs == 0L) 0 else now - lastFrameTimeNs
        val frameDuration = frameDurationNs
        val dFrames = timeSinceFrame / frameDuration
        frame += if (isReversed) -dFrames else dFrames
        val ended = !MiscUtils.contains(frame, getMinFrame(), getMaxFrame())
        frame = MiscUtils.clamp(frame, getMinFrame(), getMaxFrame())
        lastFrameTimeNs = now
        notifyUpdate()
        if (ended) {
            if (getRepeatCount() !== INFINITE && repeatCount >= getRepeatCount()) {
                frame = if (speed < 0) getMinFrame() else getMaxFrame()
                removeFrameCallback()
                notifyEnd(isReversed)
            } else {
                notifyRepeat()
                repeatCount++
                if (getRepeatMode() === REVERSE) {
                    speedReversedForRepeatMode = !speedReversedForRepeatMode
                    reverseAnimationSpeed()
                } else {
                    frame = if (isReversed) getMaxFrame() else getMinFrame()
                }
                lastFrameTimeNs = now
            }
        }
        verifyFrame()
        L.endSection("LottieValueAnimator#doFrame")
    }

    private val frameDurationNs: Float
        private get() {
            return if (composition == null) {
                Float.MAX_VALUE
            } else Utils.SECOND_IN_NANOS / composition.getFrameRate() / Math.abs(speed)
        }

    public fun clearComposition() {
        composition = null
        minFrame = Int.MIN_VALUE.toFloat()
        maxFrame = Int.MAX_VALUE.toFloat()
    }

    public fun setComposition(composition: LottieComposition) {
        // Because the initial composition is loaded async, the first min/max frame may be set
        val keepMinAndMaxFrames = this.composition == null
        this.composition = composition
        if (keepMinAndMaxFrames) {
            setMinAndMaxFrames(
                Math.max(minFrame, composition.getStartFrame()) as Int.toFloat(),
                Math.min(maxFrame, composition.getEndFrame()) as Int
                .toFloat())
        } else {
            setMinAndMaxFrames(composition.getStartFrame() as Int.toFloat(), composition.getEndFrame() as Int.toFloat())
        }
        val frame = frame
        this.frame = 0f
        setFrame(frame as Int.toFloat())
        notifyUpdate()
    }

    public fun setFrame(frame: Float) {
        if (this.frame == frame) {
            return
        }
        this.frame = MiscUtils.clamp(frame, getMinFrame(), getMaxFrame())
        lastFrameTimeNs = 0
        notifyUpdate()
    }

    fun setMinFrame(minFrame: Int) {
        setMinAndMaxFrames(minFrame.toFloat(), maxFrame as Int.toFloat())
    }

    public fun setMaxFrame(maxFrame: Float) {
        setMinAndMaxFrames(minFrame, maxFrame)
    }

    private fun setMinAndMaxFrames(minFrame: Float, maxFrame: Float) {
        require(minFrame <= maxFrame) { String.format("minFrame (%s) must be <= maxFrame (%s)", minFrame, maxFrame) }
        val compositionMinFrame = if (composition == null) -Float.MAX_VALUE else composition!!.getStartFrame()
        val compositionMaxFrame = if (composition == null) Float.MAX_VALUE else composition.getEndFrame()
        this.minFrame = MiscUtils.clamp(minFrame, compositionMinFrame, compositionMaxFrame)
        this.maxFrame = MiscUtils.clamp(maxFrame, compositionMinFrame, compositionMaxFrame)
        setFrame(MiscUtils.clamp(frame, minFrame, maxFrame) as Int.toFloat())
    }

    public fun reverseAnimationSpeed() {
        speed = -speed
    }

    public fun setRepeatMode(value: Int) {
        super.setRepeatMode(value)
        if (value != REVERSE && speedReversedForRepeatMode) {
            speedReversedForRepeatMode = false
            reverseAnimationSpeed()
        }
    }

    public fun playAnimation() {
        running = true
        notifyStart(isReversed)
        setFrame((if (isReversed) getMaxFrame() else getMinFrame()) as Int.toFloat())
        lastFrameTimeNs = 0
        repeatCount = 0
        postFrameCallback()
    }

    public fun endAnimation() {
        removeFrameCallback()
        notifyEnd(isReversed)
    }

    public fun pauseAnimation() {
        removeFrameCallback()
    }

    public fun resumeAnimation() {
        running = true
        postFrameCallback()
        lastFrameTimeNs = 0
        if (isReversed && getFrame() == getMinFrame()) {
            frame = getMaxFrame()
        } else if (!isReversed && getFrame() == getMaxFrame()) {
            frame = getMinFrame()
        }
    }

    override fun cancel() {
        notifyCancel()
        removeFrameCallback()
    }

    private val isReversed: Boolean
        private get() = speed < 0

    public fun getMinFrame(): Float {
        if (composition == null) {
            return 0F
        }
        return if (minFrame == Int.MIN_VALUE.toFloat()) composition!!.getStartFrame() else minFrame
    }

    public fun getMaxFrame(): Float {
        if (composition == null) {
            return 0F
        }
        return if (maxFrame == Int.MAX_VALUE.toFloat()) composition.getEndFrame() else maxFrame
    }

    protected fun postFrameCallback() {
        if (isRunning()) {
            removeFrameCallback(false)
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    protected fun removeFrameCallback() {
        this.removeFrameCallback(true)
    }

    protected fun removeFrameCallback(stopRunning: Boolean) {
        Choreographer.getInstance().removeFrameCallback(this)
        if (stopRunning) {
            running = false
        }
    }

    private fun verifyFrame() {
        if (composition == null) {
            return
        }
        check(!(frame < minFrame || frame > maxFrame)) {
            String.format("Frame must be [%f,%f]. It is %f",
                minFrame,
                maxFrame,
                frame)
        }
    }
}
