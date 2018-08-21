package com.armedia.acm.plugins.report.web.api;

import com.armedia.acm.core.exceptions.AcmEncryptionException;
import com.armedia.acm.plugins.report.service.ReportService;
import com.armedia.acm.services.search.service.ExecuteSolrQuery;

import org.mule.api.MuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping({ "/api/v1/plugin/report", "/api/latest/plugin/report" })
public class GetReportToRolesMapAPIController
{

    private Logger LOG = LoggerFactory.getLogger(getClass());

    private ReportService reportService;
    private ExecuteSolrQuery executeSolrQuery;

    @RequestMapping(value = "/reporttorolesmap", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, List<String>> getReportToRolesMap()
    {
        LOG.debug("Getting report to roles map ...");
        Map<String, List<String>> retval = getReportService().getReportToRolesMap();
        if (null == retval)
        {
            LOG.warn("Properties not available..");
        }
        LOG.debug("Reports to roles map : {}", retval);
        return retval;
    }

    @RequestMapping(value = "/reportstoroles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<String> getReportsPaged(
            @RequestParam(value = "sortBy", required = false, defaultValue = "name_lcs") String sortBy,
            @RequestParam(value = "dir", required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(value = "start", required = false, defaultValue = "0") int startRow,
            @RequestParam(value = "n", required = false, defaultValue = "1000") int maxRows) throws IOException
    {
        LOG.debug("Getting reports ...");

        List<String> retval = getReportService().getReportToRolesPaged(sortDirection, startRow, maxRows);
        if (null == retval)
        {
            LOG.warn("Properties not available..");
        }
        LOG.debug("Reports to roles : {}", retval);
        return retval;
    }

    @RequestMapping(value = "/reportstoroles", params = { "fq" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<String> getReportsByName(
            @RequestParam(value = "fq") String filterQuery,
            @RequestParam(value = "sortBy", required = false, defaultValue = "name_lcs") String sortBy,
            @RequestParam(value = "dir", required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(value = "start", required = false, defaultValue = "0") int startRow,
            @RequestParam(value = "n", required = false, defaultValue = "1000") int maxRows) throws IOException
    {
        LOG.debug("Getting reports ...");

        List<String> retval = getReportService().getReportToRolesByName(sortDirection, startRow, maxRows, filterQuery);
        if (null == retval)
        {
            LOG.warn("Properties not available..");
        }
        LOG.debug("Reports to roles : {}", retval);
        return retval;
    }

    @RequestMapping(value = "/{reportId:.+}/roles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<String> findRolesForReport(@PathVariable("reportId") String reportId,
            @RequestParam(value = "authorized") Boolean authorized,
            @RequestParam(value = "start", required = false, defaultValue = "0") int startRow,
            @RequestParam(value = "n", required = false, defaultValue = "10000") int maxRows,
            @RequestParam(value = "s", required = false, defaultValue = "name_lcs") String sortBy,
            @RequestParam(value = "dir", required = false, defaultValue = "ASC") String sortDirection,
            Authentication auth) throws MuleException, AcmEncryptionException
    {

        LOG.debug("Taking roles from property file for specific report");
        return reportService.getRolesForReport(authorized, reportId);
    }

    public ReportService getReportService()
    {
        return reportService;
    }

    public void setReportService(ReportService reportService)
    {
        this.reportService = reportService;
    }

    public ExecuteSolrQuery getExecuteSolrQuery()
    {
        return executeSolrQuery;
    }

    public void setExecuteSolrQuery(ExecuteSolrQuery executeSolrQuery)
    {
        this.executeSolrQuery = executeSolrQuery;
    }
}
