/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;

import com.opencsv.CSVWriter;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.statistics.StatisticsGateway;

/**
 * Periodically sample the statistics of all caches.
 * This is a Bean.  Configure it in Spring to use it.  Samples are written to a
 * CSV OutputStream which is injected by the container.  Samples are taken at
 * intervals the length of which is injected by the container.
 *
 * @author Mark H. Wood <mwood@iupui.edu>
 */
@Named
public class CacheSnapshooter {
    /** Take samples this many seconds apart. If less than or equal to 0, do nothing.  Required. */
    protected int interval;

    /** Write samples in CSV to this stream.  Required. */
    protected OutputStream output;

    /** Set up the timer and task, and start them. */
    @PostConstruct
    private void init() {
        if (interval > 0) {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            ScheduledFuture<?> future
                    = executor.scheduleAtFixedRate(new Sampler(output), 0,
                            interval, TimeUnit.SECONDS);
        }
    }

    /**
     * Take samples this many seconds apart. If less than or equal to 0, do nothing.  Required.
     * @param interval the interval to set
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * Write samples in CSV to this stream.  Required.
     * @param output the output to set
     */
    public void setOutput(OutputStream output) {
        this.output = output;
    }

    /**
     * Task to sample the cache statistics and write them out.
     */
    private static class Sampler
            implements Runnable {
        /** CSV column header labels */
        private static final String[] HEADER = {
            "time",
            "manager",
            "cache",
            "hitRatio",
            "hitCount",
            "putCount",
            "evictedCount",
        };

        private final CSVWriter writer;
        private final SimpleDateFormat dateFormat;
        private final String[] nextLine = new String[HEADER.length];

        /**
         * Prepare for writing, and write the CSV column headers.
         * @param output all output will be written here.
         */
        private Sampler(OutputStream output) {
            writer = new CSVWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(output, StandardCharsets.UTF_8)));
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
            writer.writeNext(HEADER);
        }

        @Override
        public void run() {
            nextLine[0] = dateFormat.format(new Date());
            for (CacheManager manager : CacheManager.ALL_CACHE_MANAGERS) {
                nextLine[1] = manager.getName();
                for (String cacheName : manager.getCacheNames()) {
                    nextLine[2] = cacheName;
                    Cache cache = manager.getCache(cacheName);
                    StatisticsGateway statistics = cache.getStatistics();
                    nextLine[3] = String.valueOf(statistics.cacheHitRatio());
                    nextLine[4] = String.valueOf(statistics.cacheHitCount());
                    nextLine[5] = String.valueOf(statistics.cachePutCount());
                    nextLine[6] = String.valueOf(statistics.cacheEvictedCount());
                    writer.writeNext(nextLine);
                }
            }
            writer.flushQuietly();
        }

        /**
         * When the container is shutting down, close the CSV stream.
         */
        @PreDestroy
        private void close()
                throws IOException {
            writer.close();
        }
    }
}
