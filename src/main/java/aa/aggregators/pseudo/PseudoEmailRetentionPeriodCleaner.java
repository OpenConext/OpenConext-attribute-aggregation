package aa.aggregators.pseudo;

import aa.repository.PseudoEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

@Service
public class PseudoEmailRetentionPeriodCleaner {

    private final static Logger LOG = LoggerFactory.getLogger(PseudoEmailRetentionPeriodCleaner.class);
    private final PseudoEmailRepository pseudoEmailRepository;
    private final int retentionPeriodDays;

    @Autowired
    public PseudoEmailRetentionPeriodCleaner(@Value("${pseudo_emails_retention_days_period}") int retentionPeriodDays,
                                             PseudoEmailRepository pseudoEmailRepository,
                                             @Value("${cron_job_responsible}") boolean cronJobResponsible) {
        this.pseudoEmailRepository = pseudoEmailRepository;
        this.retentionPeriodDays = retentionPeriodDays;
        if (cronJobResponsible) {
            newScheduledThreadPool(1).scheduleAtFixedRate(() -> clean(), 0, 1, TimeUnit.DAYS);
        }
    }

    protected int clean() {
        LOG.info("Start deleting inactive pseudo emails older than {} days.", this.retentionPeriodDays);
        int deleted = pseudoEmailRepository.deleteOlderThenRetentionDays(retentionPeriodDays);
        LOG.info("Finished deleting pseudo emails older than {} days. Deleted {} pseudo emails",
            retentionPeriodDays, deleted);
        return deleted;
    }
}
