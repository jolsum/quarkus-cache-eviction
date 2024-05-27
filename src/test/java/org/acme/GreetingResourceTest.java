package org.acme;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@QuarkusTest
class GreetingResourceTest {

  @Inject
  @CacheName("fast")
  Cache cacheFast;

  @Inject
  @CacheName("delayed")
  Cache cacheDelayed;

  @Test
  void testFastEndpoint() {
    for (int i = 0; i < 20; i++) {
      given().when().get("/hello/fast").then().statusCode(200).body(is("Hello"));
    }
    await()
        .atMost(5, TimeUnit.SECONDS)
        .until(() -> ((CaffeineCache) cacheFast).keySet().size() == 10);
  }

  @Test
  void testDelayedEndpoint() {
    for (int i = 0; i < 20; i++) {
      given().when().get("/hello/delayed").then().statusCode(200).body(is("Hello with a delay"));
    }
    await()
        .atMost(5, TimeUnit.SECONDS)
        .until(() -> ((CaffeineCache) cacheDelayed).keySet().size() == 10);
  }
}
