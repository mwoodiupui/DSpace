/**
 * Provides classes and methods to interface with the
 * <a href="http://www.handle.net" target=_new>CNRI Handle System</a>.
 * <p>
 * The {@link HandleServiceImpl} acts as the main entry point.  It depends on
 * DSpace's database for mapping between Handles and {@code DSpaceObject}s.
 * <p>
 * The <a href="HandlePlugin.html">HandlePlugin</a> class is intended to be
 * loaded into the CNRI Handle Server. It acts as an adapter, translating
 * Handle Server API calls into DSpace ones.  It is used with
 * {@link HandleServiceImpl}.
 * <p>
 * The {@link ExternalHandleServiceImpl} acts as a client of an unmodified
 * Handle server via the normal Handle service protocol.  It can replace the
 * tightly-integrated {@link HandleService}.
 *
 * <h2>Using the Handle API</h2>
 *
 * <p>
 * An example use of the Handle API is shown below:
 * <pre>
 *     Item item;
 *
 *     // Create or obtain a context object
 *     Context context;
 *
 *     // Create a Handle for an Item
 *     String handle = HandleManager.createHandle(context, item);
 *     // The canonical form, which can be used for citations
 *     String canonical = HandleManager.getCanonicalForm(handle);
 *     // A URL pointing to the Item
 *     String url = HandleManager.resolveToURL(context, handle);
 *
 *     // Resolve the handle back to an object
 *     Item resolvedItem = (Item) HandleManager.resolveToObject(context, handle);
 *     // From the object, find its handle
 *     String rhandle = HandleManager.findHandle(context, resolvedItem);
 * </pre>
 * </p>
 *
 * <h2>Using the HandlePlugin with CNRI Handle Server</h2>
 *
 * In the CNRI Handle Server configuration file, set storage_type to
 * <em>CUSTOM</em> and storage_class to
 * <em>org.dspace.handle.HandlePlugin</em>.
 *
 * <!-- FIXME: Can we get a sample config file? -->
 *
 * @Author Peter Breton
 */
package org.dspace.handle;
