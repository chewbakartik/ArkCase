package com.armedia.acm.services.notification.web.api;

import com.armedia.acm.services.notification.dao.NotificationDao;
import com.armedia.acm.services.notification.model.Notification;
import com.armedia.acm.services.notification.model.NotificationConstants;
import com.armedia.acm.services.search.model.SearchConstants;
import com.armedia.acm.services.search.model.SolrCore;
import com.armedia.acm.services.search.service.ExecuteSolrQuery;
import com.armedia.acm.services.search.service.SearchResults;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.api.MuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/api/v1/plugin/notification", "/api/latest/plugin/notification" })
public class GetUnreadPopupNotificationsForUserAPIController
{

    private NotificationDao notificationDao;
    private ExecuteSolrQuery executeSolrQuery;

    private Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = { "/{user}/popup" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String searchAdvancedObjectByType(
            @PathVariable(value = "user") String user,
            @RequestParam(value = "s", required = false, defaultValue = "") String sort,
            @RequestParam(value = "start", required = false, defaultValue = "0") int startRow,
            @RequestParam(value = "n", required = false, defaultValue = "10") int maxRows,
            Authentication authentication,
            HttpSession httpSession) throws MuleException
    {
        String query = "object_type_s:" + "NOTIFICATION AND type_lcs:popup AND -state_lcs:" + NotificationConstants.STATE_READ;

        if (user != null)
        {
            query += " AND owner_lcs:" + user;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Advanced Search: User '" + authentication.getName() + "' is searching for '" + query + "'");
        }

        String results = getExecuteSolrQuery().getResultsByPredefinedQuery(authentication, SolrCore.ADVANCED_SEARCH,
                query, startRow, maxRows, sort);

        setNotificationsAsRead(results);

        return results;
    }

    private void setNotificationsAsRead(String results)
    {
        SearchResults searchResults = new SearchResults();

        if (searchResults.getNumFound(results) > 0)
        {
            JSONArray docs = searchResults.getDocuments(results);

            for (int i = 0; i < docs.length(); i++)
            {
                try
                {
                    JSONObject doc = docs.getJSONObject(i);

                    if (doc.has(SearchConstants.PROPERTY_OBJECT_ID_S))
                    {
                        Long id = doc.getLong(SearchConstants.PROPERTY_OBJECT_ID_S);

                        Notification notification = getNotificationDao().find(id);
                        notification.setState(NotificationConstants.STATE_READ);

                        getNotificationDao().save(notification);
                    }
                }
                catch (Exception e)
                {
                    log.error("Cannot update notification status.", e);
                }
            }
        }
    }

    public NotificationDao getNotificationDao()
    {
        return notificationDao;
    }

    public void setNotificationDao(NotificationDao notificationDao)
    {
        this.notificationDao = notificationDao;
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