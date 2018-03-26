package com.armedia.acm.plugins.alfrescorma.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.armedia.acm.muletools.mulecontextmanager.MuleContextManager;
import com.armedia.acm.web.api.MDCConstants;

import org.apache.chemistry.opencmis.client.api.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/spring/spring-alfresco-records-service-test.xml",
        "/spring/spring-library-alfresco-service.xml",
        "/spring/spring-library-acm-encryption.xml",
        "/spring/spring-library-property-file-manager.xml" })
public class SetRecordMetadataServiceIT
{
    @Autowired
    private MuleContextManager muleContextManager;

    @Autowired
    @Qualifier("declareRecordService")
    private AlfrescoService<String> declareRecordService;

    @Autowired
    @Qualifier("setRecordMetadataService")
    private AlfrescoService<String> service;

    private String ecmFileId;

    private CmisFileWriter cmisFileWriter = new CmisFileWriter();

    private transient final Logger LOG = LoggerFactory.getLogger(getClass());

    @Before
    public void setUp() throws Exception
    {
        MDC.put(MDCConstants.EVENT_MDC_REQUEST_ALFRESCO_USER_ID_KEY, "admin");
        MDC.put(MDCConstants.EVENT_MDC_REQUEST_ID_KEY, UUID.randomUUID().toString());

        Document testFile = cmisFileWriter.writeTestFile(muleContextManager);
        ecmFileId = testFile.getVersionSeriesId();
    }

    @Test
    public void setRecordMetadat() throws Exception
    {
        assertNotNull(declareRecordService);

        Map<String, Object> declareRecordContext = new HashMap<>();

        declareRecordContext.put("ecmFileId", ecmFileId);

        String actedOnId = declareRecordService.service(declareRecordContext);

        assertEquals(ecmFileId, actedOnId);

        Map<String, Object> context = new HashMap<>();
        context.put("ecmFileId", ecmFileId);
        context.put("publicationDate", new Date());
        context.put("originator", "Jerry Garcia");
        context.put("originatingOrganization", "Grateful Dead");
        context.put("dateReceived", new Date());

        String metadataId = service.service(context);
        assertEquals(ecmFileId, metadataId);

    }
}