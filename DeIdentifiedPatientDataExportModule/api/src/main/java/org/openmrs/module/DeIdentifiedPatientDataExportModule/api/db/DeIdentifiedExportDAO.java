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
package org.openmrs.module.DeIdentifiedPatientDataExportModule.api.db;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.ExportEntity;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.api.DeIdentifiedExportService;

/**
 *  Database methods for {@link DeIdentifiedExportService}.
 */
public interface DeIdentifiedExportDAO {
	
	/*
	 * Add DAO methods here
	 */
	public ExportEntity saveRemovePersonAttrListDAO(ExportEntity exportEntity) throws DAOException, APIException;
	public ExportEntity saveConceptByCategory(ExportEntity e) throws DAOException, APIException;
	public List<String> getConceptByCategory(String category);
	public ExportEntity getConceptBySectionEntity(String category);
}