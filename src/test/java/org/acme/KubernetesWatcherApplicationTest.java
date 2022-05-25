package org.acme;

import io.fabric8.kubernetes.api.model.WatchEvent;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@QuarkusTest
@WithKubernetesTestServer(crud = false)
class KubernetesWatcherApplicationTest {

  @KubernetesTestServer
  private KubernetesServer mockServer;
  @InjectMock
  private PersistenceService persistenceService;
  @Inject
  KubernetesWatcherApplication application;

  @Test
  void jobCreationIsPersisted() {
    mockServer.getKubernetesMockServer().expect()
      .withPath("/apis/batch/v1/namespaces/test/jobs?allowWatchBookmarks=true&watch=true")
      .andUpgradeToWebSocket().open()
      .waitFor(1000L)
      .andEmit(new WatchEvent(new JobBuilder().withNewMetadata().withName("job").endMetadata().build(), "ADDED"))
      .done().always();
    var exec = Executors.newSingleThreadExecutor();
    try  {
      exec.submit(() -> application.run());
      verify(persistenceService, timeout(10_000L))
        .save(Watcher.Action.ADDED, new JobBuilder().withNewMetadata().withName("job").endMetadata().build());
    } finally {
      exec.shutdownNow();
    }
  }
}
