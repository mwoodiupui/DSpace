/**
 * Provides classes and methods for browsing Items in DSpace by whatever
 * is specified in the configuration.  The standard method by which you 
 * would perform a browse is as follows:
 *
 * <ol>
 *   <li>Create a {@link BrowserScope} object.
 *       This object holds all of the parameters of your browse request.</li>
 *   <li>Pass the {@link BrowserScope} object
 *       into the {@link BrowseEngine} object. This
 *       object should be invoked through either the browse() method or the
 *       browseMini() method.</li>
 *   <li>The {@link BrowseEngine} will pass back a {@link BrowseInfo} object
 *       which contains all the relevant details of your request.</li>
 * </ol>
 *
 * <p>
 * Browses only return archived Items; other Items (e.g. those 
 * in the workflow system) are ignored.
 * </p>
 *
 * <h2>Using the Browse API</h2>
 *
 * <p>
 * An example use of the Browse API is shown below:
 * </p>
 * <pre>{@code
 *  // Create or obtain a context object
 *  Context context = new Context();
 *
 *  // Create a BrowseScope object within the context
 *  BrowserScope scope = new BrowserScope(context);
 *
 *  // The browse is limited to the test collection
 *  Collection test = Collection.find(context, someID);
 *  scope.setBrowseContainer(test);
 *
 *  // Set the focus
 *  scope.setFocus("Test Title");
 *
 *  // A maximum of 30 items will be returned
 *  scope.setResultsPerPage(30);
 *
 *  // set ordering to DESC
 *  scope.setOrder("DESC");
 *
 *  // now execute the browse
 *  BrowseEngine be = new BrowseEngine();
 *  BrowseInfo results = be.browse(scope);
 * }</pre>
 *
 * <p>
 * In this case, the results might be Items with titles like:
 * </p>
 *
 * <pre>
 * Tehran, City of the Ages
 * Ten Little Indians
 * Tenchi Universe
 * Tension
 * Tennessee Williams
 * Test Title              (the focus)
 * Thematic Alignment
 * Thesis and Antithesis
 * ...
 * </pre>
 *
 * <h2>Browse Indexes</h2>
 *
 * <p>
 * The Browse API uses database tables to index Items based on the supplied
 * configuration.  When an Item is added to DSpace, modified or removed via the
 * <a href="../content/package-summary.html">Content Management API</a>, the
 * indexes are automatically updated.
 * </p>
 *
 * <p>
 * To rebuild the database tables for the browse (on configuration change), or
 * to re-index just the contents of the existing tables, use the following 
 * commands from <a href="IndexBrowse.html">IndexBrowse</a>:
 * </p>
 *
 * <p>A complete rebuild of the database and the indices:</p>
 *
 * <pre>
 * [dspace]/dsrun org.dspace.browse.IndexBrowse -f -r
 * </pre>
 *
 * <p>A complete re-index of the archive contents:</p>
 *
 * <pre>
 * [dspace]/dsrun org.dspace.browse.IndexBrowse -i
 * </pre>
 *
 * @author Peter Breton
 * @author Richard Jones
 * @author Mark H. Wood
 */
package org.dspace.browse;
