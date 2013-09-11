<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<form id='exportPatient' method="GET"
	action="/openmrs/module/DeIdentifiedPatientDataExportModule/new.form">


	<table>
		<tr>
			<td><b>Select input type:</b></td>
		</tr>
		<tr>
			<td><input type="radio" name="patientInput" value="single"
				required>Enter patient name:</td>
			<td><openmrs_tag:personField formFieldName="patientId"
					formFieldId="patientId" /></td>
		</tr>
		<tr>
			<td><input type="radio" name="patientInput" value="multiple"
				required>Enter patient ids:</td>
			<td><input type="text" name="listPatientIds" id="listPatientIds" /></td>
		</tr>
		<tr>
			<td><input type="radio" name="patientInput" value="cohort"
				required>Select a Cohort:</td>
			<td><select name="cohort" id="cohort">
					<c:forEach var="cohort" items="${Cohort}" varStatus="rIndex">
						<option value="${cohort.id}">${cohort.name}</option>

					</c:forEach>
			</select></td>
		</tr>
		<tr>
			<td><b>Select export profile:</b></td>
		</tr>
		<tr>
			<td><select name="profileName" id="profileName">
					<c:forEach var="p" items="${pn}" varStatus="rIndex">
						<option value="${pn.get(rIndex.index)}">${pn.get(rIndex.index)}</option>

					</c:forEach>
			</select></td>
		</tr>
		<tr>
			<td><b>Select Export Format:</b></td>
		</tr>
		<tr>
			<td><input type="radio" name="format" value="xml" required>XML</td>
		</tr>
		<tr>
			<td><input type="radio" name="format" value="json" required>JSON</td>
		</tr>
		<tr>
			<td><input type="radio" name="format" value="sql" required>SQL</td>
		</tr>
		<tr>
			<td><input type="submit" value="Export Patient Data"></td>
		</tr>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>

