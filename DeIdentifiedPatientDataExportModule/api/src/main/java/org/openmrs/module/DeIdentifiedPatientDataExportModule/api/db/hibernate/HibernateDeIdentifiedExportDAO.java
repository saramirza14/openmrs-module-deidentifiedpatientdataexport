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
package org.openmrs.module.DeIdentifiedPatientDataExportModule.api.db.hibernate;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.ExportEntity;
import org.openmrs.module.DeIdentifiedPatientDataExportModule.api.db.DeIdentifiedExportDAO;

/**
 * It is a default implementation of  {@link DeIdentifiedExportDAO}.
 */
public class HibernateDeIdentifiedExportDAO implements DeIdentifiedExportDAO {
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
     * @param sessionFactory the sessionFactory to set
     */
	public ExportEntity saveRemovePersonAttrListDAO(ExportEntity exportEntity) throws DAOException, APIException
	{
		sessionFactory.getCurrentSession().save(exportEntity);
		return exportEntity;
	}
	public ExportEntity saveConceptByCategory(ExportEntity e) throws DAOException, APIException {
		try
		{
				sessionFactory.getCurrentSession().saveOrUpdate(e);
		}catch(ConstraintViolationException c)
		{
			throw new APIException("Concept Already Exists");
		}
		
		return e;
	}

	@Override
	public java.util.List<String> getConceptByCategory(String category) {
		
		
		Criteria c =  sessionFactory.getCurrentSession().createCriteria(ExportEntity.class);
		ProjectionList projList =Projections.projectionList();

		projList.add(Projections.property("elementId"));
		c.setProjection(projList);

		c.add(Restrictions.eq("category", category)).list();
		
		List<String> l = c.list();
		
		return l; 
		
		
	}

	@Override
	public ExportEntity getConceptBySectionEntity(String category) {
		// TODO Aut1-generated method stub
		ExportEntity e = new ExportEntity();
		if(sessionFactory.getCurrentSession().createCriteria(ExportEntity.class).add(Restrictions.eq("category",category)).list().isEmpty()){
			System.out.println("empty");
		}
		else{
			e = (ExportEntity)sessionFactory.getCurrentSession().createCriteria(ExportEntity.class).add(Restrictions.eq("category",category)).list().get(0);
		}
		 return e;
	}
    
    public void setSessionFactory(SessionFactory sessionFactory) {
	    this.sessionFactory = sessionFactory;
    }
    
	/**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
	    return sessionFactory;
    }
}