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
import java.util.List;
import java.util.Properties;
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

	private List<Obs> getOriginalObsList(Patient patient){
		ObsService obsService = Context.getObsService();
		List<Obs> originalObsList = obsService.getObservationsByPerson(patient);
		return originalObsList;
	}

	public void extractPatientData(Patient patient, HttpServletResponse response){

		List<Obs> ob = getOriginalObsList(patient);
		List<Obs> obs = getEncountersOfPatient(patient,ob); //New obs list - updated

		//Setting patient name 
		patient=setRandomPatientNames(patient);

		//Set patient DOB
		patient = setPatientDOB(patient.getBirthdate(), patient);

		//Remove patient identifier
		patient=removePatientIdentifer(patient);

		generatePatientXML(patient, response,obs);

	}
	/*
	 * gives relationships to be removed
	 */
	private Patient removeRelationships(Patient patient){
		try{
			List<Relationship> l=Context.getPersonService().getRelationshipsByPerson(patient);
			for( Relationship r : l){
				r.getRelationshipId();
				System.out.println("remove these person relations ");
			}
		}
		catch(APIException e){
			e.printStackTrace();
		}

		return patient;	
	}
	/*
	 * randomizes encounter dates
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
				System.out.println("Rnd en dates" + randomizedEncounterDateList.get(i));


			}
		}
		catch(APIException e){
			e.printStackTrace();
		}
		return en;
	}

	/*This method removes 
	 * all patient identifiers
	 */
	private Patient removePatientIdentifer(Patient patient){
		Set<PatientIdentifier> s=patient.getIdentifiers();
		for(PatientIdentifier pi : s)
			patient.removeIdentifier(pi);
		return patient;
	}
	/*This method sets random patient DOB
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
	 * Generates random numbers betwwen a given range
	 */
	private int randBetween(int start, int end) {
		return start + (int)Math.round(Math.random() * (end - start));
	}

	/*
	 * Sets random patient names
	 */
	private Patient setRandomPatientNames(Patient patient){
		patient = getName(patient);
		return patient;
	}
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
		System.out.println("name" +patient.getGivenName() +" "+ patient.getMiddleName() + " "+patient.getFamilyName());
		return patient;

	}
	/*
	 * Method to generate xml with all required data
	 */
	public void generatePatientXML(Patient patient, HttpServletResponse response, List<Obs> obs){
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

			Element encounters = doc.createElement("encounters");
			pes.appendChild(encounters);
			for(int i=0; i<obs.size();i++){
				Element encounter = doc.createElement("encounter");
				encounters.appendChild(encounter);
				System.out.println(obs.get(i).getLocation().getAddress1());
				Element location = doc.createElement("encounter-location");
				location.appendChild(doc.createTextNode(obs.get(i).getLocation().getAddress1()));
				encounter.appendChild(location);
				Element date = doc.createElement("encounter-date");
				date.appendChild(doc.createTextNode(obs.get(i).getEncounter().getEncounterDatetime().toLocaleString().toString()));
				encounter.appendChild(date);
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
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//Save sections into database
	private ExportEntity entity ;
	public boolean saveConceptAsSections(List<Integer> concepts , String category) throws DAOException , APIException
	{
		StringBuffer sb = new StringBuffer("");
		char c=',';
		for(int i = 0 ; i< concepts.size() ; i++)
		{
			sb.append(concepts.get(i).toString());
			sb.append(c);
		}
		entity = new ExportEntity();
		entity.setElementId(sb.toString());
		entity.setCategory(category);

		entity.setSectionEntity(sb.toString() + category);
		ExportEntity saved  = dao.saveConceptByCategory(entity);
		return true;
	}

	public List<String> getConceptByCategory(String category)
	{
		List<String> concept = dao.getConceptByCategory(category);
		return concept;
	}
	private List<PersonAttributeType> getSavedPersonAttributeList(){
		PersonService ps = Context.getPersonService();
		List<String> list = getConceptByCategory("PersonAttribute");

		List<PersonAttributeType> attributeTypeList = new Vector<PersonAttributeType>();
		for(int i=0; i< list.size();i++){
			char retval[] = list.get(i).toCharArray();
			//Integer t= Integer.parseInt(list.get(i));
			//attributeTypeList.add(ps.getPersonAttributeType(t));
			for(int j=0; j<retval.length; j+=2)
			{
				Integer t= Character.getNumericValue(retval[j]);
				attributeTypeList.add(ps.getPersonAttributeType(t));
				System.out.println(ps.getPersonAttributeType(t));
			}
		}
		return attributeTypeList;
	}
	//get random location
	public String accessLocationPropFile(){
		Properties prop = new Properties();
		String loc="";
		try {
			//load a properties file
			prop.load(new FileInputStream("C:/Users/SARA/Desktop/OpenMRS/de/DeIdentifiedPatientDataExportModule/api/src/main/resources/config1.properties"));
			Integer a = getRandomBetween(1, 3);
			loc = prop.getProperty(a.toString());

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return loc;
	}

	public void setDao(DeIdentifiedExportDAO dao) {
		this.dao = dao;
	}

	/*
	 * Return a list of concepts which user has configured as to not to export
	 */
	private List<Concept> getListOfRemoveConcept(String category){
		List<String> stringListOfConcepts = getConceptByCategory(category); //getting ids from db
		List<Concept> conceptListNotToBeExported= new ArrayList<Concept>(); 
		Integer tempConceptId;	
		for(int i=0; i<stringListOfConcepts.size();i++){
			for (String retval:  stringListOfConcepts.get(i).split(","))
			{
				tempConceptId = Integer.parseInt(retval);
				conceptListNotToBeExported.add(Context.getConceptService().getConcept(tempConceptId));
			}
		}
		return conceptListNotToBeExported;
	}
	/*
	 * This method returns new Obs List which need to be exported
	 */
	private List<Obs> getEncountersOfPatient(Patient patient, List<Obs> patientObsList){


		//List from db
		List<Concept> conceptListNotToBeExported = getListOfRemoveConcept("Encounter");
		Set<Concept> removedDuplicateConceptList = new HashSet<Concept>(conceptListNotToBeExported);

		for(int k=0; k<patientObsList.size();k++){
			Concept c = patientObsList.get(k).getConcept();
			if(removedDuplicateConceptList.contains(c)){

				patientObsList.remove(k);
			}
		}
		List<Encounter> randomizedEncounterList = randomizeEncounterDates(patientObsList);
		List<Date> randomizedEncounterDateList= new ArrayList<Date>();
		for(Encounter e : randomizedEncounterList){
			randomizedEncounterDateList.add(e.getEncounterDatetime());
		}
		for(int i=0; i<randomizedEncounterDateList.size();i++){
			Encounter e = patientObsList.get(i).getEncounter();
			e.setEncounterDatetime(randomizedEncounterDateList.get(i));
			patientObsList.get(i).setEncounter(e);
			System.out.println(randomizedEncounterDateList.get(i));
		}
		for(int i=0; i<patientObsList.size();i++){
			Location loc = new Location();
			loc.setAddress1(accessLocationPropFile());
			patientObsList.get(i).setLocation(loc);
		}
		return patientObsList;
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