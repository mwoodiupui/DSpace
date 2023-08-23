/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.ScopeResolver;
import org.dspace.app.util.factory.UtilServiceFactory;
import org.dspace.app.util.service.OpenSearchService;
import org.dspace.core.Context;
import org.dspace.core.LogHelper;
import org.dspace.core.Utils;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverQuery.SORT_ORDER;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.IndexableObject;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoveryConfigurationService;
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.discovery.configuration.DiscoverySortConfiguration;
import org.dspace.discovery.configuration.DiscoverySortFieldConfiguration;
import org.dspace.discovery.indexobject.IndexableItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;

/**
 * This class provides a controller for OpenSearch support.
 * It creates a namespace {@code /opensearch} in the DSpace REST webapp.
 *
 * @author Oliver Goldschmidt (o.goldschmidt at tuhh.de)
 */
@Controller
@RequestMapping("/opensearch")
public class OpenSearchController {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();
    private static final String errorpath = "/error";
    private List<String> searchIndices = null;

    private OpenSearchService openSearchService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private DiscoveryConfigurationService searchConfigurationService;

    private Context context;

    @Autowired
    private ScopeResolver scopeResolver;

    /**
     * This method provides the OpenSearch query on the path {@code /search}.
     * It will pass the result as an OpenSearchDocument directly to the client.
     *
     * @param request current client request.
     * @param response developing response to current request.
     * @param query a Solr-style query.
     * @param start index of first result to return.
     * @param count maximum number of results returned.
     * @param format one of the OpenSearch report formats, e.g. "atom".
     * @param sort name of field on which to sort.
     * @param sortDirection "ASC" or "DESC".
     * @param dsoObject UUID of the container which is the scope of the search,
     *                  or omitted/{@code null} for global scope.
     * @param model not used.
     * @throws IOException passed through.
     * @throws ServletException if the Discovery result cannot be transformed
     *          to an OpenSearchDocument.
     */
    @GetMapping("/search")
    public void search(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestParam(name = "query", required = false) String query,
                         @RequestParam(name = "start", required = false) Integer start,
                         @RequestParam(name = "rpp", required = false) Integer count,
                         @RequestParam(name = "format", required = false) String format,
                         @RequestParam(name = "sort", required = false) String sort,
                         @RequestParam(name = "sort_direction", required = false) String sortDirection,
                         @RequestParam(name = "scope", required = false) String dsoObject,
                         Model model) throws IOException, ServletException {
        context = ContextUtil.obtainContext(request);
        if (start == null) {
            start = 0;
        }
        if (count == null) {
            count = -1;
        }
        if (openSearchService == null) {
            openSearchService = UtilServiceFactory.getInstance().getOpenSearchService();
        }
        if (openSearchService.isEnabled()) {
            init();
            // get enough request parameters to decide on action to take
            if (format == null || "".equals(format)) {
                // default to atom
                format = "atom";
            }

            log.debug("Searching for " + query + " in format " + format);

            // do some sanity checking
            if (!openSearchService.getFormats().contains(format)) {
                // Since we are returning error response as HTML, escape any HTML in "format" param
                String err = "Format " + Utils.addEntities(format) + " is not supported.";
                response.setContentType("text/html");
                response.setContentLength(err.length());
                response.getWriter().write(err);
            }

            // then the rest - we are processing the query
            IndexableObject container = null;

            // support pagination parameters
            DiscoverQuery queryArgs = new DiscoverQuery();
            if (query == null) {
                query = "";
            } else {
                queryArgs.setQuery(query);
            }
            queryArgs.setStart(start);
            queryArgs.setMaxResults(count);
            queryArgs.setDSpaceObjectFilter(IndexableItem.TYPE);

            if (sort != null) {
                DiscoveryConfiguration discoveryConfiguration =
                    searchConfigurationService.getDiscoveryConfiguration("");
                if (discoveryConfiguration != null) {
                    DiscoverySortConfiguration searchSortConfiguration = discoveryConfiguration
                        .getSearchSortConfiguration();
                    if (searchSortConfiguration != null) {
                        DiscoverySortFieldConfiguration sortFieldConfiguration = searchSortConfiguration
                            .getSortFieldConfiguration(sort);
                        if (sortFieldConfiguration != null) {
                            String sortField = searchService
                                .toSortFieldIndex(sortFieldConfiguration.getMetadataField(),
                                sortFieldConfiguration.getType());

                            if (sortDirection != null && sortDirection.equals("DESC")) {
                                queryArgs.setSortField(sortField, SORT_ORDER.desc);
                            } else {
                                queryArgs.setSortField(sortField, SORT_ORDER.asc);
                            }
                        } else {
                            throw new IllegalArgumentException(sort + " is not a valid sort field");
                        }
                    }
                }
            } else {
                // this is the default sort so we want to switch this to date accessioned
                queryArgs.setSortField("dc.date.accessioned_dt", SORT_ORDER.desc);
            }

            if (dsoObject != null) {
                container = scopeResolver.resolveScope(context, dsoObject);
                DiscoveryConfiguration discoveryConfiguration = searchConfigurationService
                        .getDiscoveryConfiguration(context,  container);
                queryArgs.setDiscoveryConfigurationName(discoveryConfiguration.getId());
                queryArgs.addFilterQueries(discoveryConfiguration.getDefaultFilterQueries()
                        .toArray(
                                new String[discoveryConfiguration.getDefaultFilterQueries()
                                        .size()]));
            }

            // Perform the search
            DiscoverResult qResults = null;
            try {
                qResults = SearchUtils.getSearchService().search(context,
                    container, queryArgs);
            } catch (SearchServiceException e) {
                log.error(LogHelper.getHeader(context, "opensearch", "query="
                            + queryArgs.getQuery()
                            + ",error=" + e.getMessage()), e);
                throw new RuntimeException(e.getMessage(), e);
            }

            // Log
            log.info("opensearch done, query=\"" + query + "\",results="
                        + qResults.getTotalSearchResults());

            List<IndexableObject> dsoResults = qResults.getIndexableObjects();
            Document resultsDoc = openSearchService.getResultsDoc(context, format, query,
                (int) qResults.getTotalSearchResults(), qResults.getStart(),
                qResults.getMaxResults(), container, dsoResults);
            try {
                Transformer xf = TransformerFactory.newInstance().newTransformer();
                response.setContentType(openSearchService.getContentType(format));
                xf.transform(new DOMSource(resultsDoc),
                    new StreamResult(response.getWriter()));
            } catch (TransformerException e) {
                log.error(e);
                throw new ServletException(e.toString());
            }
        } else {
            log.debug("OpenSearch Service is disabled");
            String err = "OpenSearch Service is disabled";
            response.setStatus(404);
            response.setContentType("text/html");
            response.setContentLength(err.length());
            response.getWriter().write(err);
        }
    }

    /**
     * This method provides the OpenSearch {@code servicedescription} document
     * on the path {@code /service}.
     * It will pass the result as an OpenSearchDocument directly to the client.
     *
     * @param request the current client request.
     * @param response the developing response to the current request.
     * @throws IOException passed through.
     */
    @GetMapping("/service")
    public void service(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        log.debug("Show OpenSearch Service document");
        if (openSearchService == null) {
            openSearchService = UtilServiceFactory.getInstance().getOpenSearchService();
        }
        if (openSearchService.isEnabled()) {
            String svcDescrip = openSearchService.getDescription(null);
            log.debug("opensearchdescription is " + svcDescrip);
            response.setContentType(openSearchService
                .getContentType("opensearchdescription"));
            response.setContentLength(svcDescrip.length());
            response.getWriter().write(svcDescrip);
        } else {
            log.debug("OpenSearch Service is disabled");
            String err = "OpenSearch Service is disabled";
            response.setStatus(404);
            response.setContentType("text/html");
            response.setContentLength(err.length());
            response.getWriter().write(err);
        }
    }

    /**
     * Internal method for controller initialization
     */
    private void init() {
        if (searchIndices == null) {
            searchIndices = new ArrayList<>();
            DiscoveryConfiguration discoveryConfiguration = SearchUtils
                    .getDiscoveryConfiguration();
            searchIndices.add("any");
            for (DiscoverySearchFilter sFilter : discoveryConfiguration.getSearchFilters()) {
                searchIndices.add(sFilter.getIndexFieldName());
            }
        }
    }

    public void setOpenSearchService(OpenSearchService oSS) {
        openSearchService = oSS;
    }
}
