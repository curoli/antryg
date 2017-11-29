package antryg.util

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}

import scala.concurrent.{Future, Promise}

object MoreJavaConverters {

  implicit class RichListenableFuture[T](listenableFuture: ListenableFuture[T]) {
    /**
      * Converts ListenableFuture to scala.concurrent.Future
      * Code taken and cleaned up from https://gist.github.com/chrisphelps/43fb1b2bd5d958728167
      */
    def asScala: Future[T] = {
      val promise = Promise[T]
      Futures.addCallback(listenableFuture,
        new FutureCallback[T] {
          def onSuccess(result: T): Unit = promise.success(result)

          def onFailure(throwable: Throwable): Unit = promise.failure(throwable)
        })
      promise.future
    }
  }

}
