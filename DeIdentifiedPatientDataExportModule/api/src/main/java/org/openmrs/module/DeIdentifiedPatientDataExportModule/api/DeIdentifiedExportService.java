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
package org.openmrs.module.DeIdentifiedPatientDataExportModule.api;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.swing.text.Document;

import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.ExportEntity;
import org.springframework.transaction.annotation.Transactional;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(DeIdentifiedExportService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface DeIdentifiedExportService extends OpenmrsService {
     
	public void generatePatientXML(Patient patient, HttpServletResponse response, List<Obs> obs);
	public void extractPatientData(Patient patient, HttpServletResponse response);
	public boolean saveConceptAsSections(List<Integer> concepts , String category) throws DAOException , APIException;
	List<String> getConceptByCategory(String category);
	public String accessLocationPropFile();
}