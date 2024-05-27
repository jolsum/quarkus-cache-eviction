package org.acme;

import static org.awaitility.Awaitility.await;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.smallrye.mutiny.Uni;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class CaffeineCacheTest {

  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @Test
  void testGetAsyncWithCompletedFuture() {
    AsyncCache<Object, Object> cache = Caffeine.newBuilder().maximumSize(10).buildAsync();
    for (int i = 0; i < 100; i++) {
      cache.asMap().computeIfAbsent(i, CompletableFuture::completedFuture);
    }
    await().atMost(5, TimeUnit.SECONDS).until(() -> cache.synchronous().estimatedSize() == 10);
  }

  @Test
  void testGetAsyncWithDelayedFuture() {
    AsyncCache<Object, Object> cache = Caffeine.newBuilder().maximumSize(10).buildAsync();
    for (int i = 0; i < 100; i++) {
      cache.asMap().computeIfAbsent(i, this::delayedFuture);
    }
    await().atMost(5, TimeUnit.SECONDS).until(() -> cache.synchronous().estimatedSize() == 10);
  }

  @Test
  void testGetAsyncWithDelayedFutureFromUni() {
    AsyncCache<Object, Object> cache = Caffeine.newBuilder().maximumSize(10).buildAsync();
    for (int i = 0; i < 100; i++) {
      cache.asMap().computeIfAbsent(i, this::delayedFutureFromUni);
    }
    await().atMost(5, TimeUnit.SECONDS).until(() -> cache.synchronous().estimatedSize() == 10);
  }

  private <T> CompletableFuture<T> delayedFuture(T value) {
    CompletableFuture<T> future = new CompletableFuture<>();
    scheduler.schedule(() -> future.complete(value), 100, TimeUnit.MILLISECONDS);
    return future;
  }

  private <T> CompletableFuture<T> delayedFutureFromUni(T o) {
    return Uni.createFrom()
        .item(o)
        .onItem()
        .delayIt()
        .by(Duration.ofMillis(100))
        .subscribeAsCompletionStage();
  }
}
