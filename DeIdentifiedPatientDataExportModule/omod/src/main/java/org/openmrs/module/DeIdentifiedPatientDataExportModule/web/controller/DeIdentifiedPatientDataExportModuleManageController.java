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

import javax.servlet.http.HttpServletResponse;
import javax.swing.text.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
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
		System.out.println("magae");
		
	}
	
	@RequestMapping(value = "/module/DeIdentifiedPatientDataExportModule/new", method = RequestMethod.GET)
	public void new_manage(@RequestParam(value="patientId",required=true)Patient patient, HttpServletResponse response) {
		
		System.out.println("new_manage"+patient.getId());
		DeIdentifiedExportService d = Context.getService(DeIdentifiedExportService.class);
		d.extractPatientData(patient, response);
		
		
	}
	}
