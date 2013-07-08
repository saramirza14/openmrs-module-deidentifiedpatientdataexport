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

import org.openmrs.api.impl.BaseOpenmrsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.api.DeIdentifiedExportService;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.api.db.DeIdentifiedExportDAO;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import java.io.File;
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
/**
 * It is a default implementation of {@link DeIdentifiedExportService}.
 */
public class DeIdentifiedExportServiceImpl extends BaseOpenmrsService implements DeIdentifiedExportService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private DeIdentifiedExportDAO dao;
	
	/**
     * @param dao the dao to set
     */
    
	public void extractPatientData(Patient patient, HttpServletResponse response){
		
		//Setting patient name 
		patient=setRandomPatientNames(patient);
		
		//Set patient DOB
		String randomDOB = setPatientDOB(patient.getBirthdate(), patient);
		
		//Remove patient identifier
		patient=removePatientIdentifer(patient);
		
		//Generate XML
		generatePatientXML(patient, response, randomDOB);
	}
	
	private Patient removePatientIdentifer(Patient patient){
		Set<PatientIdentifier> s=patient.getIdentifiers();
		for(PatientIdentifier pi : s)
		patient.removeIdentifier(pi);
		return patient;
	}
	
	private String setPatientDOB(Date date, Patient patient){
		
		 GregorianCalendar gc = new GregorianCalendar();
		 String y=date.toString();	//Extract year from date object
		 y= y.substring(0, 3);
		 int year=Integer.parseInt(y);
		 int currYear=gc.get(gc.YEAR);  //Finding current Year
		 int age=currYear-year;
		 if(age>60)	//If patient age above 60 years then randomize the year also
			 year=randBetween(1990,2010);
		 int dayOfMonth = randBetween(1, 31);
		 int month = randBetween(0, 11);
	     String randomAge=year+"-"+month+"-"+dayOfMonth;
	     return randomAge;
	}
	
	private int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }

	private Patient setRandomPatientNames(Patient patient){
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
	
	public void generatePatientXML(Patient patient, HttpServletResponse response, String randomDOB){
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
					DOB.appendChild(doc.createTextNode(randomDOB));
					pes.appendChild(DOB);
					
					Element gender = doc.createElement("gender");
					gender.appendChild(doc.createTextNode(patient.getGender()));
					pes.appendChild(gender);

					Element salary = doc.createElement("citizenship");
					if(patient.getAttribute(3)!=null)
						salary.appendChild(doc.createTextNode(patient.getAttribute(3).toString()));
					pes.appendChild(salary);
			 
					Element civilStatus = doc.createElement("civil-status");
					if(patient.getAttribute(5)!=null)
						civilStatus.appendChild(doc.createTextNode(patient.getAttribute(5).toString()));
					pes.appendChild(civilStatus);
			 		 
					Element race = doc.createElement("race");
					if(patient.getAttribute(1)!=null)
					race.appendChild(doc.createTextNode(patient.getAttribute(1).toString()));
					pes.appendChild(race);
					
					Element birthplace = doc.createElement("birth-place");
					if(patient.getAttribute(2)!=null)
						birthplace.appendChild(doc.createTextNode(patient.getAttribute(2).toString()));
					pes.appendChild(birthplace);
					
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
	
	//Method to generate Random Patient names
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
		
	public void setDao(DeIdentifiedExportDAO dao) {
	    this.dao = dao;
    }
    

    /**
     * @return the dao
     */
    public DeIdentifiedExportDAO getDao() {
	    return dao;
    }
}