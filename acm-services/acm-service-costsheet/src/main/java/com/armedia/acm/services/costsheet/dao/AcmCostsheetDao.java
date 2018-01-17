/**
 * 
 */
package com.armedia.acm.services.costsheet.dao;

import com.armedia.acm.data.AcmAbstractDao;
import com.armedia.acm.services.costsheet.model.AcmCostsheet;
import com.armedia.acm.services.costsheet.model.CostsheetConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;

import java.util.List;

/**
 * @author riste.tutureski
 *
 */
public class AcmCostsheetDao extends AcmAbstractDao<AcmCostsheet>
{

    private Logger LOG = LoggerFactory.getLogger(getClass());

    @Override
    protected Class<AcmCostsheet> getPersistenceClass()
    {
        return AcmCostsheet.class;
    }

    public List<AcmCostsheet> findByObjectIdAndType(Long objectId, String objectType, int startRow, int maxRows, String sortParams)
    {
        String orderByQuery = "";
        if (sortParams != null && !"".equals(sortParams))
        {
            orderByQuery = " ORDER BY costsheet." + sortParams;
        }

        Query selectQuery = getEm().createQuery("SELECT costsheet "
                + "FROM AcmCostsheet costsheet "
                + "WHERE costsheet.parentId = :parentId "
                + "AND costsheet.parentType = :parentType"
                + orderByQuery);

        selectQuery.setParameter("parentId", objectId);
        selectQuery.setParameter("parentType", objectType);
        selectQuery.setFirstResult(startRow);
        selectQuery.setMaxResults(maxRows);

        @SuppressWarnings("unchecked")
        List<AcmCostsheet> costsheets = (List<AcmCostsheet>) selectQuery.getResultList();

        return costsheets;
    }

    @Override
    public String getSupportedObjectType()
    {
        return CostsheetConstants.OBJECT_TYPE;
    }

}
