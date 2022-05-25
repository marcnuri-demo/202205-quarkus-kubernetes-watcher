package org.acme;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class JobWatcherService implements Watcher<Job> {

  @Inject
  KubernetesClient client;
  @Inject
  PersistenceService persistenceService;

  private final AtomicBoolean started = new AtomicBoolean(false);
  private final CompletableFuture<Void> future = new CompletableFuture<>();
  private Watch watch;

  public Watch watch() {
    if (!started.getAndSet(true)) {
      watch = client.batch().v1().jobs().watch(this);
    }
    return watch;
  }

  public void await() throws ExecutionException, InterruptedException {
    future.get();
  }
  @Override
  public void eventReceived(Action action, Job resource) {
    persistenceService.save(action, resource);
  }

  @Override
  public void onClose(WatcherException cause) {
    future.completeExceptionally(cause);
  }
}
