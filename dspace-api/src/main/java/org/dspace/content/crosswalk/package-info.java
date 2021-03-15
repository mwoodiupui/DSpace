/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
/**
 * Provides an API and implementations of metadata crosswalks, which are
 * directional mappings from one schema to another, performed in the context of
 * Item ingestion or dissemination. Most crosswalks are driven by a mapping
 * in a file, which reside in <code>config/crosswalks</code>.</p>
 *
 * <h2>Crosswalk Interfaces</h2>
 *
 * The principal interfaces are for ingest and dissemination contexts, i.e.
 * the {@link IngestionCrosswalk} interface consists of the methods:
 *
 * <ul>
 *   <li>{@code public void ingest(Context context, DSpaceObject dso, List metadata)}</li>
 *   <li>{@code public void ingest(Context context, DSpaceObject dso, Element root)}</li>
 * </ul>
 *
 * <p>The DisseminationCrosswalk interface has methods:</p>
 *
 * <ul>
 *   <li>{@code public Namespace[] getNamespaces()}</li>
 *   <li>{@code public String getSchemaLocation()}</li>
 *   <li>{@code public boolean canDisseminate(DSpaceObject dso)}</li>
 *   <li>{@code public List disseminateList(DSpaceObject dso)}</li>
 *   <li>{@code public Element disseminateElement(DSpaceObject dso)}</li>
 * </ul>
 *
 * <h2>Crosswalk Implementations</h2>
 * <p>Crosswalks exist for many formats, including DC, QDC, METs, MODs, Premis,
 * and a general implementation employing an XSLT stylesheet.<p>
 *
 * @author Richard Rodgers
 */

package org.dspace.content.crosswalk;
