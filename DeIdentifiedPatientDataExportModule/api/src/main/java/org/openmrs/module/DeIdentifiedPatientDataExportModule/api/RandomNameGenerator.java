package org.openmrs.module.DeIdentifiedPatientDataExportModule.api;

import org.openmrs.Patient;

public interface RandomNameGenerator {
	
	public Patient getName(Patient patient);
		

}
