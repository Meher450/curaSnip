package com.curapaste.jobs;


import com.curapaste.config.CleanupJobProperties;
import com.curapaste.entities.Paste;
import com.curapaste.repository.PasteRepository;
import com.curapaste.services.CacheService;
import com.curapaste.services.storage.ContentStorageService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ExpiryCleanupJob {
    private final PasteRepository pasteRepository;
    private final ContentStorageService contentStorageService;
    private final CacheService cacheService;

    private final CleanupJobProperties cleanupJobProperties;


    public ExpiryCleanupJob(
            PasteRepository pasteRepository,
            ContentStorageService contentStorageService,
            CacheService  cacheService,
            CleanupJobProperties cleanupJobProperties){
        this.cacheService = cacheService;
        this.contentStorageService = contentStorageService;
        this.pasteRepository = pasteRepository;
        this.cleanupJobProperties = cleanupJobProperties;
    }

    @Scheduled(fixedRateString = "${cleanup.expiry.interval-ms}")
    @SchedulerLock(name = "expiryCleanup", lockAtMostFor = "1m")
    public void sweep(){
        System.out.println("EXPIRY CLEANUP RUNNING AT: "+ Instant.now());
        System.out.println(System.getProperty("PID")
                + " time="
                + Instant.now());
        List<Paste> expired = pasteRepository.findExpiredBatch(
                Instant.now(),
                PageRequest.of(
                        0,
                        cleanupJobProperties.getBatchSize()
                )
        );

        for(Paste p: expired){
            if(p.getContentLocation()!=null){
                contentStorageService.delete(p.getContentLocation());
            }

            cacheService.evict(p.getShortId());
        }

        pasteRepository.deleteAllInBatch(expired);

    }
}
