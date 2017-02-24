/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.util;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage a queue of background tasks.  Each entry represents a class and a Map
 * to be presented to a method of the class as its sole argument.
 *
 * <p>
 * Tasks are executed by a pool of background threads.  The size of this pool
 * is configured as {@code background.threads}.  If unconfigured, the default
 * pool size is 1 thread.  NOTE that the pool can only be resized by restarting
 * DSpace, as it is statically configured at startup.
 *
 * <p>
 * Tasks are not monitored or timed out.  If your task can block or loop forever,
 * you should handle that in your task code.  If unending tasks accumulate, they
 * can eventually fill the pool and block all further task executions.
 *
 * @author mwood
 */
public class WorkQueue
{
    private static final Logger LOG = LoggerFactory.getLogger(Task.class);

    private static final ConfigurationService cfg
            = DSpaceServicesFactory.getInstance().getConfigurationService();

    /** The queue of Workers and thread pool. */
    private static final ExecutorService facilities;
    static {
        facilities = Executors.newFixedThreadPool(cfg.getIntProperty("background.threads", 1));
    }

    private WorkQueue() {}

    /**
     * Add a {@link Worker} to the queue.
     *
     * @param clazz the specific Worker implementation.
     * @param args arguments to this specific invocation of the Worker.
     */
    public static void enqueue(Class<Worker> clazz, Map<String, String> args)
    {
        facilities.submit(new Task(clazz, args));
    }

    /**
     * Represent a specific invocation of a {@link Worker} class.
     */
    private static class Task
            extends Thread
    {
        private final Class<Worker> clazz;
        private final Map<String, String> args;
        private String clazzName;
        private long threadId;

        private Task(Class<Worker> clazz, Map<String, String> args)
        {
            this.clazz = clazz;
            this.args = args;
            if (LOG.isDebugEnabled())
            {
                clazzName = clazz.getName();
                threadId = this.getId();
            }
            LOG.debug("Created Task from {} in thread {}", clazzName, threadId);
        }

        @Override
        public void run()
        {
            Worker worker;
            LOG.debug("Running Task {} in thread {}", clazzName, threadId);
            try {
                worker = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                LOG.error("Background task could not run:", ex);
                return;
            }
            worker.work(args);
            LOG.debug("Completed Task {} in thread {}", clazzName, threadId);
        }
    }
}
