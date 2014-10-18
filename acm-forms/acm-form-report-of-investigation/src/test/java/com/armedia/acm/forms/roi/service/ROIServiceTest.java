/**
 * 
 */
package com.armedia.acm.forms.roi.service;

import static org.junit.Assert.*;

import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.armedia.acm.forms.roi.model.ROIForm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author riste.tutureski
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/spring/spring-roi-service-test.xml"    
})
public class ROIServiceTest {

	@Autowired
	private ROIService roiServiceTest;
	
	private Gson gson;
	
	@Before
    public void setUp() throws Exception {
		gson = new GsonBuilder().setDateFormat("M/dd/yyyy").create();
    }
	
	@Test
	public void testInitFormData() throws Exception {
		String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("txt/InitFormData.txt"));
		
		ROIForm form = gson.fromJson(expected, ROIForm.class);
		form.getReportInformation().setDate(new Date());
		
		assertEquals(gson.toJson(form), roiServiceTest.get("init-form-data").toString());
	}

}
