/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.DeIdentifiedPatientDataExportModule.web.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.api.DeIdentifiedExportService;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The main controller.
 */
@Controller
public class  DeIdentifiedPatientDataExportModuleManageController {

	protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping(value = "/module/DeIdentifiedPatientDataExportModule/createProfile", method = RequestMethod.GET)
	public void manage(ModelMap model, Patient patient) {
		model.addAttribute("user", Context.getAuthenticatedUser());
		DeIdentifiedExportService d = Context.getService(DeIdentifiedExportService.class);
		List<Cohort> attributeTypeList = new Vector<Cohort>();
		//only fill the Object if the user has authenticated properly
		if (Context.isAuthenticated()) {
			CohortService cs = Context.getCohortService();
			attributeTypeList = cs.getAllCohorts();
		}
		model.addAttribute("Cohort", attributeTypeList);
		List<String> profileNames = new Vector<String>();
		profileNames = d.getProfileNames();
		 model.addAttribute("pn", profileNames);
		 
	}

	@RequestMapping(value = "/module/DeIdentifiedPatientDataExportModule/new", method = RequestMethod.GET)
	public void new_manage( HttpServletResponse response, HttpServletRequest request) {

		String ids="";
		String ab = request.getParameter("patientId");
		System.out.println("ids" + ids);
		DeIdentifiedExportService d = Context.getService(DeIdentifiedExportService.class);
		ids= request.getParameter("listPatientIds");
		int flag=0;
		String a = request.getParameter("format");
		String c = request.getParameter("cohort");
		String pn= request.getParameter("profileName");
		int idP = d.getProfileIdByName(pn);
		List<String> list =  d.getConceptsByCategoryByPid("PersonAttribute", idP);
		
		String b = request.getParameter("patientInput");
		List<Integer> idList = new ArrayList<Integer>();
		 StringBuffer sb = new StringBuffer("");
		//d.exportCohort();
		if(b.contentEquals("single")){
			ids=request.getParameter("patientId");
		}
		else if (b.contentEquals("multiple")){
			ids = request.getParameter("listPatientIds");
		}
		
		else if(b.contentEquals("cohort")){
			flag=1;
			int id = Integer.parseInt(c);
			CohortService cs = Context.getCohortService();
			Cohort cohort =  cs.getCohort(id);
			Set<Integer> cohortSet = cohort.getMemberIds();
			
		       
			for(Integer i: cohortSet){
				sb.append(i.toString());
				sb.append(",");
			  	}
			
		}
		
		if(a.contentEquals("json")){
			if(flag==1)
				ids=sb.toString();
			d.exportJson(response, ids, idP);
			
		}
		
		else if(a.contentEquals("sql")){
			if(flag==1)
				ids=sb.toString();
			d.generatePatientSQL(response, ids, idP);
		}
			
		else{
			if(flag==1)
				ids=sb.toString();
			d.extractPatientData(response, ids, idP);
		}

	}
}
