/*
 * Copyright 2019 Indiana University.  All rights reserved.
 *
 * Mark H. Wood, IUPUI University Library, Apr 9, 2019
 */

/*
 * Copyright 2019 Indiana University.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dspace.app.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.TimeZone;
import javax.inject.Inject;
import javax.inject.Named;

import com.opencsv.CSVWriter;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.statistics.StatisticsGateway;

/**
 * Periodically sample the statistics of all caches.
 *
 * @author Mark H. Wood <mwood@iupui.edu>
 */
@Named
public class CacheSnapshooter {
    /** Take samples this many seconds apart. */
    @Inject
    private Integer interval;

    /** Write samples in CSV to this file. */
    @Inject
    private OutputStream output;

    public CacheSnapshooter() {
        if (interval > 0) {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            ScheduledFuture<?> future
                    = executor.scheduleAtFixedRate(new Sampler(output), 0,
                            interval, TimeUnit.SECONDS);
        }
    }

    private static class Sampler implements Runnable {
        private final CSVWriter writer;
        private final SimpleDateFormat dateFormat;
        private static final String[] header = {"time", "manager", "cache", };

        private Sampler(OutputStream output) {
            writer = new CSVWriter(new OutputStreamWriter(output));
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
            writer.writeNext(header);
        }

        @Override
        public void run() {
            String now = dateFormat.format(new Date());
            String[] nextLine = new String[header.length];
            nextLine[0] = now;
            for (CacheManager manager : CacheManager.ALL_CACHE_MANAGERS) {
                nextLine[1] = manager.getName();
                for (String cacheName : manager.getCacheNames()) {
                    nextLine[2] = cacheName;
                    Cache cache = manager.getCache(cacheName);
                    StatisticsGateway statistics = cache.getStatistics();
                    // Write a statistics line TODO with statistics
                    writer.writeNext(nextLine);
                }
            }
            writer.flushQuietly();
        }
    }
}
