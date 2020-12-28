package lottie

import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

public class LottieTask<T>(runnable: Callable<LottieResult<T>>, runNow: Boolean) {
    public val EXECUTOR: Executor = Executors.newCachedThreadPool()

    private var result: LottieResult<T>? = null
    private val successListeners: MutableSet<LottieListener<T>> = LinkedHashSet(1)
    private val failureListeners: MutableSet<LottieListener<Throwable>> = LinkedHashSet(1)

    init {
        if (runNow) {
            try {
                runnable.call()?.let { setResult(it) }
            } catch (e: Throwable) {
                setResult(LottieResult(exception = e))
            }
        } else {
            //TODO EXECUTOR.execute(LottieFutureTask(runnable))
        }
    }

    public fun addListener(listener: LottieListener<T>): LottieTask<T> {
        result?.getValue()?.let { listener.onResult(it) }
        successListeners.add(listener)
        return this
    }

    public fun removeListener(listener: LottieListener<T>): LottieTask<T> {
        successListeners.remove(listener)
        return this
    }

    public fun addFailureListener(listener: LottieListener<Throwable>): LottieTask<T> {
        result?.getException()?.let { listener.onResult(it) }
        failureListeners.add(listener)
        return this
    }

    public fun removeFailureListener(listener: LottieListener<Throwable>): LottieTask<T> {
        failureListeners.remove(listener)
        return this
    }

    private fun setResult(result: LottieResult<T>) {
        check(result == null) { "A task may only be set once." }
        this.result = result
        notifyListeners()
    }

    private fun notifyListeners() {
        // Listeners should be called on the main thread.
        /*TODO handler.post(Runnable {
            if (result == null) {
                return@Runnable
            }
            // Local reference in case it gets set on a background thread.
            val result = result
            if (result.getValue() != null) {
                notifySuccessListeners(result.getValue())
            } else {
                notifyFailureListeners(result.getException())
            }
        })*/
    }

    /*TODO private class LottieFutureTask(callable: Callable<LottieResult<T?>?>?) :
        FutureTask<LottieResult<T?>?>(callable) {
        override fun done() {
            if (isCancelled) {
                // We don't need to notify and listeners if the task is cancelled.
                return
            }
            try {
                setResult(get())
            } catch (e: InterruptedException) {
                setResult(LottieResult<T>(e))
            }
        }
    }*/
}


