package com.armedia.acm.plugins.casefile.web.api;

import com.armedia.acm.plugins.casefile.model.CaseFile;
import com.armedia.acm.plugins.casefile.model.SplitCaseOptions;
import com.armedia.acm.plugins.casefile.service.SplitCaseService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/spring/spring-web-split-case-api-test.xml"})
public class SplitCaseFilesAPIControllerTest extends EasyMockSupport {
    @Autowired
    WebApplicationContext wac;
    @Autowired
    MockHttpSession session;
    @Autowired
    MockHttpServletRequest request;

    private Authentication mockAuthentication;

    private MockMvc mockMvc;
    @Autowired
    private ExceptionHandlerExceptionResolver exceptionResolver;

    @Autowired
    SplitCaseFilesAPIController splitCaseFilesAPIController;

    private SplitCaseService splitCaseService;

    @Before
    public void setup() {
        splitCaseService = createMock(SplitCaseService.class);
        mockAuthentication = createMock(Authentication.class);
        splitCaseFilesAPIController.setSplitCaseService(splitCaseService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(splitCaseFilesAPIController).setHandlerExceptionResolvers(exceptionResolver).build();
    }

    @Test
    public void testSplitCaseFiles() throws Exception {


        expect(mockAuthentication.getName()).andReturn("user").times(2);

        SplitCaseOptions options = new SplitCaseOptions();
        options.setCaseFileId(1l);

        Capture<SplitCaseOptions> capture = new Capture<SplitCaseOptions>();
        EasyMock.expect(splitCaseService.splitCase(eq(mockAuthentication), eq("127.0.0.1"), EasyMock.capture(capture))).andAnswer(new IAnswer<CaseFile>() {
            public CaseFile answer() throws Throwable {
                CaseFile caseFile = new CaseFile();
                caseFile.setId(capture.getValue().getCaseFileId());
                return caseFile;
            }
        });


        session.setAttribute("acm_ip_address", "127.0.0.1");

        replayAll();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String content = objectMapper.writeValueAsString(options);

        MvcResult result = mockMvc.perform(
                post("/api/latest/plugin/copyCaseFile")
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockAuthentication)
                        .content(content))
                .andExpect(status().isOk())
                .andReturn();

        CaseFile item = objectMapper.readValue(result.getResponse().getContentAsString(), CaseFile.class);
        assertEquals(item.getId().longValue(), options.getCaseFileId().longValue());

    }
}