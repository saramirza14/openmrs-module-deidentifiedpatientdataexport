<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>

	<li
		<c:if test='<%=request.getRequestURI().contains("/manage")%>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/DeIdentifiedPatientDataExportModule/createProfile.form"><spring:message
				code="DeIdentifiedPatientDataExportModule.find" /></a>
	</li>
	<li
		<c:if test='<%=request.getRequestURI().contains("/manage")%>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/DeIdentifiedPatientDataExportModule/configureProfile.form"><spring:message
				code="DeIdentifiedPatientDataExportModule.configure" /></a>
	</li>

	<!-- Add further links here -->
</ul>
<h2>
	<spring:message code="DeIdentifiedPatientDataExportModule.title" />
</h2>
