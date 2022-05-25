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
public class JobWatcherServiceTest {
  @KubernetesTestServer
  private KubernetesServer mockServer;
  @InjectMock
  private PersistenceService persistenceService;
  @Inject
  JobWatcherService jobWatcherService;


  @Test
  void watch_withJobCreation_jobIsPersisted() {
    mockServer.getKubernetesMockServer().expect()
      .withPath("/apis/batch/v1/namespaces/test/jobs?allowWatchBookmarks=true&watch=true")
      .andUpgradeToWebSocket().open()
      .waitFor(1000L)
      .andEmit(new WatchEvent(new JobBuilder().withNewMetadata().withName("job").endMetadata().build(), "ADDED"))
      .done().always();
    jobWatcherService.watch();
    verify(persistenceService, timeout(10_000L))
      .save(Watcher.Action.ADDED, new JobBuilder().withNewMetadata().withName("job").endMetadata().build());
  }
}
