<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<form id='exportPatient' method="GET">
<openmrs_tag:personField formFieldName="patientId"  formFieldId="patientId" /> 
<input type="submit" value="Find Patient" >
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>

