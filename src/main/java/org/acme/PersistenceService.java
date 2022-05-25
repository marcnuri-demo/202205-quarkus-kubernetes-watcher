package org.acme;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.Watcher;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PersistenceService {

  @Inject
  Logger logger;

  public void save(Watcher.Action action, Job job) {
    logger.infov("Saving {} job: {}",
      action.name(),
      job.getMetadata().getName());
  }
}
