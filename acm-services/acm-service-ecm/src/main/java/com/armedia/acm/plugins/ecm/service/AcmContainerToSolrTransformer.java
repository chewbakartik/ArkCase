package com.armedia.acm.plugins.ecm.service;

/*-
 * #%L
 * ACM Service: Enterprise Content Management
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

import com.armedia.acm.plugins.ecm.dao.AcmContainerDao;
import com.armedia.acm.plugins.ecm.model.AcmContainer;
import com.armedia.acm.services.search.model.solr.SolrAdvancedSearchDocument;
import com.armedia.acm.services.search.model.solr.SolrDocument;
import com.armedia.acm.services.search.service.AcmObjectToSolrDocTransformer;

import java.util.Date;
import java.util.List;

/**
 * Created by armdev on 3/23/15.
 */
public class AcmContainerToSolrTransformer implements AcmObjectToSolrDocTransformer<AcmContainer>
{
    private AcmContainerDao dao;

    @Override
    public List<AcmContainer> getObjectsModifiedSince(Date lastModified, int start, int pageSize)
    {
        return getDao().findModifiedSince(lastModified, start, pageSize);
    }

    @Override
    public SolrAdvancedSearchDocument toSolrAdvancedSearch(AcmContainer in)
    {
        // no implementation needed yet
        return null;
    }

    @Override
    public SolrDocument toSolrQuickSearch(AcmContainer in)
    {

        SolrDocument doc = new SolrDocument();

        // no access control on folders (yet)
        doc.setPublic_doc_b(true);

        doc.setAuthor_s(in.getCreator());
        doc.setAuthor(in.getCreator());
        doc.setObject_type_s(in.getObjectType());
        doc.setObject_id_s("" + in.getId());
        doc.setCreate_tdt(in.getCreated());
        doc.setId(in.getId() + "-" + in.getObjectType());
        doc.setLast_modified_tdt(in.getModified());
        doc.setName(in.getContainerObjectTitle());
        doc.setModifier_s(in.getModifier());
        doc.setParent_object_id_i(in.getContainerObjectId());
        doc.setParent_object_id_s("" + in.getContainerObjectId());
        doc.setParent_object_type_s(in.getContainerObjectType());
        doc.setTitle_parseable(in.getContainerObjectTitle());
        doc.setTitle_t(in.getContainerObjectTitle());

        // folder id will be used to find files and folders that belong to this container
        doc.setFolder_id_i(in.getFolder().getId());
        doc.setFolder_name_s(in.getFolder().getName());

        // need an _lcs field for sorting
        doc.setName_lcs(in.getContainerObjectTitle());

        return doc;
    }

    @Override
    public boolean isAcmObjectTypeSupported(Class acmObjectType)
    {
        return AcmContainer.class.equals(acmObjectType);
    }

    public AcmContainerDao getDao()
    {
        return dao;
    }

    public void setDao(AcmContainerDao dao)
    {
        this.dao = dao;
    }

    @Override
    public Class<?> getAcmObjectTypeSupported()
    {
        return AcmContainer.class;
    }
}
