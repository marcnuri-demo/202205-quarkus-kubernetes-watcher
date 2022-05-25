package org.acme;

import io.fabric8.kubernetes.client.Watch;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

@QuarkusMain
public class KubernetesWatcherApplication implements QuarkusApplication {

  @Inject
  JobWatcherService jobWatcherService;

  @Override
  public int run(String... args) throws Exception {
    try (Watch ignored = jobWatcherService.watch()) {
      jobWatcherService.await();
      Quarkus.waitForExit();
      return 0;
    } catch (ExecutionException ex) {
      return 1;
    }
  }
}
