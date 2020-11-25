package org.ld.utils;

import akka.actor.ActorSystem;
import akka.dispatch.OnComplete;
import scala.concurrent.Future;

import java.util.concurrent.CompletableFuture;

/**
 * 将akka的Fucture转化成java8的CompletableFuture
 * https://github.com/Comcast/sirius/pull/133/files
 * https://stackoverflow.com/questions/47868480/akka-java-future-oncomplete-oncomplete-is-not-called
 */
public class SaFutureAdapter<T> extends CompletableFuture<T> {

    /**
     * 将Scala的Fucture 转换成java8 的 CompletableFuture
     */
    public static <T> CompletableFuture<T> of(Future<T> future) {
        return new SaFutureAdapter<>(future);
    }

    public static <T> CompletableFuture<T> of(Future<T> future, ActorSystem actorSystem) {
        return new SaFutureAdapter<>(future, actorSystem);
    }

    private SaFutureAdapter(Future<T> akkaFuture) {
        this(akkaFuture, ActorSystemHolder.ACTORSYSTEM);
    }

    @SuppressWarnings("unchecked")
    public SaFutureAdapter(Future<T> akkaFuture, ActorSystem actorSystem) {
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
                , actorSystem.dispatcher());
    }

    /**
     * Not implemented, you may not cancel an Akka Future
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new IllegalStateException("Not implemented");
    }
}
