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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.ProfileName;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.api.DeIdentifiedExportService;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.api.impl.DeIdentifiedExportServiceImpl;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

/**
 * Controls the moving/deleting of {@link PersonAttributeType}s.
 */
@Controller
@RequestMapping(value = "/module/DeIdentifiedPatientDataExportModule/configureProfile", method = RequestMethod.GET)
public class ConfigureProfileController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Show the page to the user.
	 * 
	 * @should not fail if not authenticated
	 * @should put all attribute types into map
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public void displayPage(@ModelAttribute("profileName")
    ProfileName profileName, BindingResult result, ModelMap modelMap, HttpServletRequest request) throws Exception {
		manageProfileName(profileName, result, modelMap, request);
		DeIdentifiedExportService d = Context.getService(DeIdentifiedExportService.class);
		List<PersonAttributeType> attributeTypeList = new Vector<PersonAttributeType>();
		List<String> profileNames = new Vector<String>();
		//only fill the Object if the user has authenticated properly
		if (Context.isAuthenticated()) {
			PersonService ps = Context.getPersonService();
			attributeTypeList = ps.getAllPersonAttributeTypes(true);
			
		}
	 profileNames = d.getProfileNames();
	 modelMap.addAttribute("pn", profileNames);
	 
		modelMap.addAttribute("personAttributeTypeList", attributeTypeList);
		
		 manageSections(request, "PersonAttribute");
		manageSections(request, "Encounter");
		List<Concept> l = d.populateConceptSection("Encounter");
		
		
		
	}
	
	private void manageProfileName(@ModelAttribute("profileName")
    ProfileName profileName, BindingResult result, ModelMap model, HttpServletRequest request){
		
		DeIdentifiedExportService d = Context.getService(DeIdentifiedExportService.class);
		String pname=request.getParameter("profileName");
		if(pname!=null){
			profileName.setProfileName(pname);
			System.out.println("reached profile naem");
			d.saveProfile(profileName);
		}
		
	}
	private void manageSections(HttpServletRequest request , String section)
	{
		DeIdentifiedExportService d = Context.getService(DeIdentifiedExportService.class);
		String s = request.getParameter(section+"Counter");
		
		if(s!=null){
			Integer j = Integer.parseInt(s);
			List conceptIds = new ArrayList();
			if(j>0)
			{
				
			 for(int i=0;i<j;i++)
			 {
				 try{
					 
					 Integer conceptId = Integer.parseInt(request.getParameter(section+i+"_span_hid"));
					 conceptIds.add(conceptId);
				 }
				 catch(NumberFormatException n )
				 {
					 n.printStackTrace();
				 }
			 }
			}
			try
			 {
				 if(conceptIds.size() > 0)
					 d.saveConceptAsSections(conceptIds,section);
				 request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "");
			 }catch(DAOException e )
			 {
					request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
						    "ExportCCD.could.not.save"));
			 }catch(APIException e )
			 {
					request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
			 }
			 catch(Exception e )
			 {
					request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
			 }
		}
		
	}
	
	
		
}
