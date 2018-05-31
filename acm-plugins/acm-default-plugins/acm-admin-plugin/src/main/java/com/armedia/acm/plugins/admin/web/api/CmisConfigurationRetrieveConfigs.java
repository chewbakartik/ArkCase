package com.armedia.acm.plugins.admin.web.api;

/*-
 * #%L
 * ACM Default Plugin: admin
 * %%
 * Copyright (C) 2014 - 2018 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.armedia.acm.plugins.admin.exception.AcmCmisConfigurationException;
import com.armedia.acm.plugins.admin.service.CmisConfigurationPropertiesService;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * Created by nick.ferguson on 3/22/2017.
 */
@Controller
@RequestMapping({ "/api/v1/plugin/admin", "/api/latest/plugin/admin" })
public class CmisConfigurationRetrieveConfigs
{
    private Logger log = LoggerFactory.getLogger(getClass());

    private CmisConfigurationPropertiesService cmisConfigurationPropertiesService;

    @RequestMapping(value = "/cmisconfiguration/config", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE
    })
    @ResponseBody
    public String retrieveCmisConfigs() throws IOException, AcmCmisConfigurationException
    {
        log.debug("Retrieving CMIS Properties");

        JSONArray cmisPropertiesArray = cmisConfigurationPropertiesService.retrieveProperties();
        return cmisPropertiesArray.toString();
    }

    public void setCmisConfigurationPropertiesService(CmisConfigurationPropertiesService cmisConfigurationPropertiesService)
    {
        this.cmisConfigurationPropertiesService = cmisConfigurationPropertiesService;
    }
}
