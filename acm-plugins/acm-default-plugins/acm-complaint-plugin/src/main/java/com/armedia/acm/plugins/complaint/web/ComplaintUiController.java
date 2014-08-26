package com.armedia.acm.plugins.complaint.web;

import java.util.Properties;

import com.armedia.acm.services.authenticationtoken.service.AuthenticationTokenService;
import com.armedia.acm.web.AcmPageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/plugin/complaint")
public class ComplaintUiController
{
    private Logger log = LoggerFactory.getLogger(getClass());

    private AcmPageDescriptor pageDescriptor;
    private AcmPageDescriptor pageDescriptorWizard;
    private AcmPageDescriptor pageDescriptorList;
    private AuthenticationTokenService authenticationTokenService;
	private Properties formsProperties;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView openComplaints(Authentication auth) {
        ModelAndView retval = new ModelAndView();
        retval.setViewName("complaint");
        retval.addObject("pageDescriptor",  getPageDescriptorList());

        String token = this.authenticationTokenService.getTokenForAuthentication(auth);
        retval.addObject("token", token);
        return retval;
    }

    @RequestMapping(value = "/{complaintId}", method = RequestMethod.GET)
    public ModelAndView openComplaint(Authentication auth, @PathVariable(value = "complaintId") Long complaintId
    ) {
        ModelAndView retval = new ModelAndView();
        retval.setViewName("complaint");
        retval.addObject("complaintId", complaintId);
        retval.addObject("pageDescriptor",  getPageDescriptorList());

        String token = this.authenticationTokenService.getTokenForAuthentication(auth);
        retval.addObject("token", token);
        String roiFormUrl = getFormsProperties().get("form.roi.url").toString();
        retval.addObject("roiFormUrl", roiFormUrl);
        
        log.debug("Security token: " + token + "; ROI Form Url: " + roiFormUrl);
        return retval;
    }

    @RequestMapping(value = "/old", method = RequestMethod.GET)
    public ModelAndView openComplaintList(Authentication auth,
                                          @RequestParam(value = "initId", required = false) Integer initId
            ,@RequestParam(value = "initTab", required = false) String initTab
    ) {
        ModelAndView retval = new ModelAndView();
        retval.setViewName("complaintList");
        retval.addObject("pageDescriptor",  getPageDescriptorList());
        retval.addObject("initId",  initId);
        retval.addObject("initTab",  initTab);
        
        String token = this.authenticationTokenService.getTokenForAuthentication(auth);
        retval.addObject("token", token);
        String roiFormUrl = getFormsProperties().get("form.roi.url").toString();
        retval.addObject("roiFormUrl", roiFormUrl);
        
        log.debug("Security token: " + token + "; ROI Form Url: " + roiFormUrl);
        return retval;
    }

    @RequestMapping(value = "/old/{complaintId}", method = RequestMethod.GET)
    public ModelAndView openComplaintDetail(@PathVariable(value = "complaintId") Long complaintId)
    {
        ModelAndView retval = new ModelAndView();
        retval.setViewName("complaintList");
        retval.addObject("complaintId", complaintId);
        retval.addObject("pageDescriptor",  getPageDescriptorList());
        return retval;
    }

    @RequestMapping(value = "/wizard", method = RequestMethod.GET)
    public ModelAndView openComplaintWizard()
    {
        ModelAndView retval = new ModelAndView();
        retval.setViewName("complaintWizard");
        retval.addObject("pageDescriptor",  getPageDescriptorWizard());
        return retval;

    }

    public AcmPageDescriptor getPageDescriptor() {
        return pageDescriptor;
    }

    public void setPageDescriptor(AcmPageDescriptor pageDescriptor) {
        this.pageDescriptor = pageDescriptor;
    }

    public AcmPageDescriptor getPageDescriptorWizard() {
        return pageDescriptorWizard;
    }

    public void setPageDescriptorWizard(AcmPageDescriptor pageDescriptorWizard) {
        this.pageDescriptorWizard = pageDescriptorWizard;
    }

    public AcmPageDescriptor getPageDescriptorList() {
        return pageDescriptorList;
    }

    public void setPageDescriptorList(AcmPageDescriptor pageDescriptorList) {
        this.pageDescriptorList = pageDescriptorList;
    }

	public AuthenticationTokenService getAuthenticationTokenService() {
		return authenticationTokenService;
	}

	public void setAuthenticationTokenService(
			AuthenticationTokenService authenticationTokenService) {
		this.authenticationTokenService = authenticationTokenService;
	}

	public Properties getFormsProperties() {
		return formsProperties;
	}

	public void setFormsProperties(Properties formsProperties) {
		this.formsProperties = formsProperties;
	}

}