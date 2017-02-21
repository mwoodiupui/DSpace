
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
 * is configured as {@code background.threads}.  NOTE that the pool can only
 * be resized by restarting DSpace, as it is statically configured at startup.
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

        private Task(Class<Worker> clazz, Map<String, String> args)
        {
            this.clazz = clazz;
            this.args = args;
        }

        @Override
        public void run()
        {
            // Instantiate the class and execute it.
            Worker worker;
            try {
                worker = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                LOG.error("Background task could not run:", ex);
                return;
            }
            worker.work(args);
        }

        private Class getClazz() { return clazz; }

        private Map<String, String> getArgs() { return args; }
    }
}
