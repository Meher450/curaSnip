package com.curapaste.jobs;


import com.curapaste.repository.PasteRepository;
import com.curapaste.services.CacheService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Set;

@Component
public class AnalyticsSyncJob {
    private final CacheService cacheService;
    private  final PasteRepository pasteRepository;

    public AnalyticsSyncJob(CacheService cacheService, PasteRepository pasteRepository) {
        this.cacheService = cacheService;
        this.pasteRepository = pasteRepository;
    }

    @Scheduled(fixedRateString = "${analytics-sync.expiry.interval-ms}")
    @SchedulerLock(
            name = "analyticsSync",
            lockAtMostFor = "1m"
    )
    public void syncAnalytics(){
        System.out.println("SYNCING ANALYTICS FROM CACHE TO DB");

        Set<String> dirtyPastes = cacheService.getDirtyPasteIds();

        for(String shortId : dirtyPastes){
            long viewCount =
                    cacheService.getViewCount(shortId);

            Instant lastViewedAt =
                    cacheService.getLastViewedAt(shortId);

            int updated =
                    pasteRepository.updateAnalytics(
                            shortId,
                            lastViewedAt,
                            viewCount
                    );

            if (updated > 0) {
                /*
                 * Don't blindly remove the dirty marker.
                 * A new view may have arrived while
                 * PostgreSQL was being updated.
                 */
                long latestViewCount =
                        cacheService.getViewCount(shortId);

                Instant latestLastViewedAt =
                        cacheService.getLastViewedAt(shortId);

                if (latestViewCount == viewCount
                        && sameInstant(
                        latestLastViewedAt,
                        lastViewedAt
                )) {

                    cacheService.markAnalyticsSynced(shortId);
                }
            }
        }
    }

    private boolean sameInstant(
            Instant first,
            Instant second
    ) {
        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.equals(second);
    }
}
