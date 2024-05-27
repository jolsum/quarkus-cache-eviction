package org.acme;

import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.Duration;

@Path("/hello")
public class GreetingResource {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/fast")
  public Uni<String> fast() {
    return getHelloFast(System.currentTimeMillis());
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/delayed")
  public Uni<String> delayed() {
    return getHelloDelayed(System.currentTimeMillis());
  }

  @CacheResult(cacheName = "delayed")
  Uni<String> getHelloDelayed(long key) {
    return Uni.createFrom().item("Hello with a delay").onItem().delayIt().by(Duration.ofMillis(10));
  }

  @CacheResult(cacheName = "fast")
  Uni<String> getHelloFast(long key) {
    return Uni.createFrom().item("Hello");
  }
}
