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
package org.openmrs.module.DeIdentifiedPatientDataExportModule.api.impl;

import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.api.impl.PersonServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.ExportEntity;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.api.DeIdentifiedExportService;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.api.RandomNameGenerator;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.api.db.DeIdentifiedExportDAO;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptSource;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.User;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.*;

import javax.servlet.http.HttpServletResponse;
import java.util.GregorianCalendar;
import java.text.DateFormat;
import org.openmrs.patient.impl.*;

/**
 * It is a default implementation of {@link DeIdentifiedExportService}.
 */
public class DeIdentifiedExportServiceImpl extends BaseOpenmrsService implements DeIdentifiedExportService, RandomNameGenerator {

	protected final Log log = LogFactory.getLog(this.getClass());

	private DeIdentifiedExportDAO dao;

	/**
	 * @param dao the dao to set
	 */

	/*
	 * Returns the Original Obs List of patient
	 */
	private List<Obs> getOriginalObsList(Patient patient){
		ObsService obsService = Context.getObsService();
		List<Obs> originalObsList = obsService.getObservationsByPerson(patient);
		return originalObsList;
	}

	public void extractPatientData(Patient patient, HttpServletResponse response){

		List<PersonAttributeType> pat = getSavedPersonAttributeList();
		List<Obs> ob = getOriginalObsList(patient);
		List<Obs> obs = getEncountersOfPatient(patient,ob); //New obs list - updated

		//Setting patient name 
		patient=setRandomPatientNames(patient);

		//Set patient DOB
		patient = setPatientDOB(patient.getBirthdate(), patient);

		//Remove patient identifier
		patient=removePatientIdentifer(patient);

		//Set random patient identifier
		patient = setPatientIdentifier(patient);

		generatePatientXML(patient, response,obs,pat);

	}

	/*
	 * gives relationships to be removed

	private Patient removeRelationships(Patient patient){
		try{
			List<Relationship> l=Context.getPersonService().getRelationshipsByPerson(patient);
			for( Relationship r : l){
				r.getRelationshipId();
			}
		}
		catch(APIException e){
			e.printStackTrace();
		}

		return patient;	
	}
	 */

	/*
	 * Returns a List of Encounters with Randomized Dates
	 */
	private List<Encounter> randomizeEncounterDates(List<Obs> patientObsList){
		Integer flag=0;
		List<Date> randomizedEncounterDateList = new ArrayList<Date>();
		List<Encounter> en = new ArrayList<Encounter>();
		for(Obs o : patientObsList){
			en.add(o.getEncounter());
		}
		int year,month,date;
		try{
			for(int i=0; i<patientObsList.size();i++){
				Date d= new Date();
				if(flag==0){
					d.setDate(en.get(0).getEncounterDatetime().getDate());
					d.setMonth(en.get(0).getEncounterDatetime().getMonth());
					d.setYear(en.get(0).getEncounterDatetime().getYear());
					flag=1;
				}
				else
				{
					d.setDate(en.get(i-1).getEncounterDatetime().getDate());
					d.setMonth(en.get(i-1).getEncounterDatetime().getMonth());
					d.setYear(en.get(i-1).getEncounterDatetime().getYear());
				}
				year=d.getYear()+1900;
				month=d.getMonth();
				date=d.getDate();

				//Randomize
				year= randBetween(year+1, year+2) ;
				month = randBetween(month, 12);
				date= randBetween(date, 30);

				//Set date
				d.setYear(year-1900);
				d.setMonth(month);
				d.setDate(date);
				en.get(i).setEncounterDatetime(d);
				randomizedEncounterDateList.add(d);
			}
		}
		catch(APIException e){
			log.error("Exception in randomizing encounter dates", e);
		}
		return en;
	}

	/*
	 * Returns patient object by removing all patient identifiers
	 */
	private Patient removePatientIdentifer(Patient patient){
		Set<PatientIdentifier> s=patient.getIdentifiers();
		for(PatientIdentifier pi : s)
			patient.removeIdentifier(pi);
		return patient;
	}

	/*
	 * Returns Patient Object with random patient identifier
	 */
	private Patient setPatientIdentifier(Patient patient){
		UUID u = new UUID(1,0);
		UUID randomUUID = u.randomUUID();
		PatientIdentifier pi = new PatientIdentifier();
		pi.setIdentifier(randomUUID.toString());
		patient.addIdentifier(pi);
		return patient;
	}

	/*
	 * Returns Patient object with random patient DOB
	 */
	private Patient setPatientDOB(Date date, Patient patient){
		int year=date.getYear()+1900;
		int age=patient.getAge();
		if(age>60){	//If patient age above 60 years then randomize the year also
			year=randBetween(1990,2010);
		}
		int dayOfMonth = randBetween(1, 31);
		int month = randBetween(0, 11);
		Date d = new Date(year-1900, month, dayOfMonth); //Random date
		patient.setBirthdate(d);
		return patient;
	}

	/*
	 * Returns a random number between a given range
	 */
	private int randBetween(int start, int end) {
		return start + (int)Math.round(Math.random() * (end - start));
	}

	/*
	 * Returns Patient object by setting random patient names
	 */
	private Patient setRandomPatientNames(Patient patient){
		patient = getName(patient);
		return patient;
	}

	/*
	 * Generates patient names
	 * (non-Javadoc)
	 * @see org.openmrs.module.DeIdentifiedPatientDataExportModule.api.RandomNameGenerator#getName(org.openmrs.Patient)
	 */
	public Patient getName(Patient patient){
		Set<PersonName> s=patient.getNames();
		PersonName p = PersonName.newInstance(patient.getPersonName());
		p.setGivenName(generatePatientName());
		p.setMiddleName(generatePatientName());
		p.setFamilyName(generatePatientName());
		Object ob[]=s.toArray();
		for (int i = 0; i < ob.length; i++) {
			PersonName p1=(PersonName)ob[i];
			patient.removeName(p1);
		}
		s.clear();
		s.add(p);
		patient.setNames(s);
		return patient;

	}

	/*
	 * Method to generate XML with all required data
	 */
	public void generatePatientXML(Patient patient, HttpServletResponse response, List<Obs> obs,  List<PersonAttributeType> pat){
		response.setHeader( "Content-Disposition", "attachment;filename="+patient.getGivenName()+".xml");	
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("patient-export-summary");
			doc.appendChild(rootElement);

			Element patients = doc.createElement("patients");
			rootElement.appendChild(patients);

			Element p = doc.createElement("patient");
			patients.appendChild(p);

			Element pes = doc.createElement("patient-demographic-data");
			p.appendChild(pes);

			Element name = doc.createElement("name");
			name.appendChild(doc.createTextNode(patient.getGivenName() + " " + patient.getMiddleName() + " "+ patient.getFamilyName()));
			pes.appendChild(name);

			Element DOB = doc.createElement("DOB");
			DOB.appendChild(doc.createTextNode(patient.getBirthdate().toString()));
			pes.appendChild(DOB);

			for(int i=0; i<pat.size();i++){

				PersonAttribute pa = patient.getAttribute(pat.get(i));
				String personAttributeType = pat.get(i).getName().toLowerCase().replaceAll("\\s","");

				Element personAttribute = doc.createElement(personAttributeType);
				personAttribute.appendChild(doc.createTextNode(pa.getValue().toString()));
				pes.appendChild(personAttribute);
			}

			Element encounters = doc.createElement("encounters");
			p.appendChild(encounters);

			List<ConceptSource> cs = getConceptMapping(obs);
			for(int i=0; i<obs.size();i++){
				Element encounter = doc.createElement("encounter");
				encounters.appendChild(encounter);
				Element location = doc.createElement("encounter-location");
				location.appendChild(doc.createTextNode(obs.get(i).getLocation().getAddress1()));
				encounter.appendChild(location);
				Element date = doc.createElement("encounter-date");
				date.appendChild(doc.createTextNode(obs.get(i).getEncounter().getEncounterDatetime().toLocaleString().toString()));
				encounter.appendChild(date);

				Element observation = doc.createElement("observation");
				encounter.appendChild(observation);
				Element concept = doc.createElement("concept");
				observation.appendChild(concept);
				Element conceptId = doc.createElement("concept-id");
				conceptId.appendChild(doc.createTextNode(obs.get(i).getConcept().toString()));
				concept.appendChild(conceptId);

				for(int j=0; j<cs.size();j++){
					Element conceptSourceId = doc.createElement("concept-source-id");
					conceptSourceId.appendChild(doc.createTextNode(cs.get(j).getHl7Code().toString()));
					concept.appendChild(conceptSourceId);

					Element conceptSource = doc.createElement("concept-source");
					conceptSource.appendChild(doc.createTextNode(cs.get(j).getName().toString()));
					concept.appendChild(conceptSource);
				}

				Element value = doc.createElement("value");
				observation.appendChild(value);

				if(obs.get(i).getValueCoded()!=null){
					Element valueCoded = doc.createElement("value-coded");
					valueCoded.appendChild(doc.createTextNode(obs.get(i).getValueCoded().toString()));
					value.appendChild(valueCoded);
				}

				if(obs.get(i).getValueNumeric()!=null){
					Element valueNumeric = doc.createElement("value-numeric");
					valueNumeric.appendChild(doc.createTextNode(obs.get(i).getValueNumeric().toString()));
					value.appendChild(valueNumeric);
				}

				if(obs.get(i).getValueBoolean()!=null){
					Element valueBoolean = doc.createElement("value-boolean");
					valueBoolean.appendChild(doc.createTextNode(obs.get(i).getValueBoolean().toString()));
					value.appendChild(valueBoolean);
				}

			}

			// Then write the doc into a StringWriter
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			//initialize StreamResult with StringWriter object to save to string
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

			String xmlString = result.getWriter().toString();
			System.out.println(xmlString);

			// Finally, send the response
			byte[] res = xmlString.getBytes(Charset.forName("UTF-8"));
			response.setCharacterEncoding("UTF-8");
			response.getOutputStream().write(res);
			response.flushBuffer();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("IO Exception in generating XML", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Exception in generating XML", e);
		}
	}

	/*
	 *  saves current configuration 
	 */
	//private ExportEntity entity ;
	public boolean saveConceptAsSections(List<Integer> concepts , String category) throws DAOException , APIException
	{
		ExportEntity entity = dao.getConceptBySectionEntity(category);

		StringBuffer sb = new StringBuffer("");
		char c=',';
		for(int i = 0 ; i< concepts.size() ; i++)
		{
			sb.append(concepts.get(i).toString());
			sb.append(c);
		}

		entity.setElementId(sb.toString());
		entity.setCategory(category);
		entity.setSectionEntity(sb.toString() + category);
		ExportEntity saved  = dao.saveConceptByCategory(entity);
		return true;
	}

	/*
	 * Returns List containing concept ids by category which are already saved
	 * (non-Javadoc)
	 * @see org.openmrs.module.DeIdentifiedPatientDataExportModule.api.DeIdentifiedExportService#getConceptByCategory(java.lang.String)
	 */
	public List<String> getConceptByCategory(String category)
	{
		List<String> concept = dao.getConceptByCategory(category);
		return concept;
	}

	/*
	 * Returns a list of Concepts which have been saved previously under a specific category
	 */
	public List<Concept> populateConceptSection(String category){
		ConceptService cs = Context.getConceptService();
		DeIdentifiedExportService d = Context.getService(DeIdentifiedExportService.class);
		List<String> list = d.getConceptByCategory(category);
		Integer temp;
		List<Concept> conceptList = new Vector<Concept>();
		for(int i=0; i<list.size();i++){
			for (String retval: list.get(i).split(",")){
				temp = Integer.parseInt(retval);
				conceptList.add(cs.getConcept(temp));
			}
		}
		Set set = new HashSet(conceptList);
		List list1 = new ArrayList(set);
		return list1;
	}

	/*
	 * Returns List of personAttributeType which have been saved
	 * @see org.openmrs.module.DeIdentifiedPatientDataExportModule.api.DeIdentifiedExportService#getSavedPersonAttributeList()
	 */
	public List<PersonAttributeType> getSavedPersonAttributeList(){
		PersonService ps = Context.getPersonService();
		DeIdentifiedExportService d = Context.getService(DeIdentifiedExportService.class);
		List<String> list = d.getConceptByCategory("PersonAttribute");

		List<PersonAttributeType> attributeTypeList = new Vector<PersonAttributeType>();
		for(int i=0; i< list.size();i++){
			char retval[] = list.get(i).toCharArray();
			for(int j=0; j<retval.length; j+=2)
			{
				Integer t= Character.getNumericValue(retval[j]);
				attributeTypeList.add(ps.getPersonAttributeType(t));

			}
		}
		return attributeTypeList;	
	}


	/*
	 * Returns a String which is a random location
	 * (non-Javadoc)
	 * @see org.openmrs.module.DeIdentifiedPatientDataExportModule.api.DeIdentifiedExportService#accessLocationPropFile()
	 */
	public String accessLocationPropFile(){
		Properties prop = new Properties();
		String loc="";
		try {
			//load a properties file
			prop.load(new FileInputStream("C:/Users/SARA/Desktop/OpenMRS/de/DeIdentifiedPatientDataExportModule/api/src/main/resources/config1.properties"));
			Integer a = getRandomBetween(1, 3);
			loc = prop.getProperty(a.toString());

		} catch (IOException ex) {
			log.error("IOException in accessing Location File", ex);
		}
		return loc;
	}

	public void setDao(DeIdentifiedExportDAO dao) {
		this.dao = dao;
	}


	/*
	 * Returns Obs List which need to be exported
	 * All Obs minus Black listed Obs
	 * for encounter section
	 */
	private List<Obs> getEncountersOfPatient(Patient patient, List<Obs> patientObsList){


		patientObsList = getFinalObsList(patient, patientObsList, "Encounter");		

		List<Encounter> randomizedEncounterList = randomizeEncounterDates(patientObsList);
		List<Date> randomizedEncounterDateList= new ArrayList<Date>();
		for(Encounter e : randomizedEncounterList){
			randomizedEncounterDateList.add(e.getEncounterDatetime());
		}
		for(int i=0; i<randomizedEncounterDateList.size();i++){
			Encounter e = patientObsList.get(i).getEncounter();
			e.setEncounterDatetime(randomizedEncounterDateList.get(i));
			patientObsList.get(i).setEncounter(e);
		}
		for(int i=0; i<patientObsList.size();i++){
			Location loc = new Location();
			loc.setAddress1(accessLocationPropFile());
			patientObsList.get(i).setLocation(loc);
		}
		return patientObsList;
	}


	/*
	 * Method to get new patientObs list ie Original Patient Obs list minus the black list
	 * for Obs section
	 */
	private List<Obs> getFinalObsList(Patient patient, List<Obs> patientObsList, String category){
		List<Concept> conceptListNotToBeExported = populateConceptSection(category);
		Set<Concept> removedDuplicateConceptList = new HashSet<Concept>(conceptListNotToBeExported);

		for(int k=0; k<patientObsList.size();k++){
			Concept c = patientObsList.get(k).getConcept();
			if(removedDuplicateConceptList.contains(c)){

				patientObsList.remove(k);
			}
		}
		return patientObsList;
	}

	/*
	 * Returns list of concept source for each obs
	 */
	private List<ConceptSource> getConceptMapping(List<Obs> patientObsList){
		List<ConceptSource> conceptSourceList = new ArrayList<ConceptSource>();
		for(Obs o : patientObsList){
			Concept c = o.getConcept();
			Collection<ConceptMap> conceptMap = c.getConceptMappings();
			Iterator it;
			it = conceptMap.iterator();
			while(it.hasNext()){
				ConceptMap cm = (ConceptMap) it.next();
				Integer mapId = cm.getConceptMapId();
				ConceptSource cs = cm.getSource();
				conceptSourceList.add(cs);
			}
		}
		return conceptSourceList;

	}
	/**
	 * @return the dao
	 */
	public DeIdentifiedExportDAO getDao() {
		return dao;
	}

	/*
	 * Method to generate Random Patient names
	 */
	public static String generatePatientName(){

		String retName = "";	// return this string

		// Seed random generator
		Random generator = new Random();

		int length = getRandomBetween(5,6);

		// CVCCVC or VCCVCV
		if(getRandomBetween(1,2) < 2)
		{
			retName += getRandomConsonant();
			retName = retName.toUpperCase();
			retName += getRandomVowel();
			retName += getRandomConsonant();
			retName += getRandomConsonant();
			if (length >= 5) { retName += getRandomVowel(); }
			if (length >= 6) { retName += getRandomConsonant(); }
		}
		else
		{
			retName += getRandomVowel();
			retName = retName.toUpperCase();
			retName += getRandomConsonant();
			retName += getRandomConsonant();
			retName += getRandomVowel();
			if (length >= 5) { retName += getRandomConsonant(); }
			if (length >= 6) { retName += getRandomVowel(); }
		}

		return retName;
	}

	// Returns a, e, i, o or u
	public static String getRandomVowel()
	{
		int randNum = getRandomBetween(1,4);

		switch(randNum)
		{
		case 1:
			return "a";
		case 2:
			return "e";
		case 3:
			return "i";
		case 4:
			return "o";
		}

		return "u";
	}

	public static String getRandomConsonant()
	{
		// Use the ascii values for a-z and convert to char
		char randLetter = (char) getRandomBetween(97,122);
		while (isCharVowel(randLetter))
		{
			randLetter = (char) getRandomBetween(97,122);
		}

		return Character.toString(randLetter);
	}

	public static boolean isCharVowel(char letter)
	{
		if (letter == 'a' || letter == 'e' || letter == 'i' || letter == 'o' || letter == 'u')
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// Returns a random number between lowerbound and upperbound inclusive
	public static int getRandomBetween(int lb, int ub)
	{
		Random generator = new Random();
		int ret = generator.nextInt(ub+1-lb) + lb;

		return ret;
	}

}