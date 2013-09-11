<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>
<openmrs:htmlInclude file="/dwr/interface/DWRMyModuleService.js"/>
<openmrs:htmlInclude file="/dwr/interface/DWRVisitService.js"/>
<openmrs:htmlInclude file="/dwr/interface/DWREncounterService.js"/>
<openmrs:htmlInclude file="/dwr/interface/DWRProviderService.js" />


<form method="get" >
<input id="profileName" type="text" name="profileName"/>

</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>