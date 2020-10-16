package org.ld.utils;

import akka.dispatch.OnComplete;
import org.ld.config.LucaConfig;
import scala.concurrent.Future;

import java.util.concurrent.CompletableFuture;

/**
 * 将akka的Fucture转化成java8的CompletableFuture
 * https://github.com/Comcast/sirius/pull/133/files
 * https://stackoverflow.com/questions/47868480/akka-java-future-oncomplete-oncomplete-is-not-called
 */
public class AkkaFutureAdapter<T> extends CompletableFuture<T> {

    @SuppressWarnings("unchecked")
    public AkkaFutureAdapter(Future<T> akkaFuture) {
        akkaFuture.onComplete(new OnComplete<>() {
                                  @Override
                                  public void onComplete(Throwable throwable, Object o) {
                                      try {
                                          complete((T) o);
                                      } catch (Throwable exception) {
                                          completeExceptionally(exception);
                                      }
                                  }
                              }
                , LucaConfig.ActorSystemHolder.ACTORSYSTEM.dispatcher());
    }

    /**
     * Not implemented, you may not cancel an Akka Future
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new IllegalStateException("Not implemented");
    }
}
