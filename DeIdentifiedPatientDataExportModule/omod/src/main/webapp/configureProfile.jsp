<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>
<openmrs:htmlInclude file="/dwr/interface/DWRMyModuleService.js"/>
<openmrs:htmlInclude file="/dwr/interface/DWRVisitService.js"/>
<openmrs:htmlInclude file="/dwr/interface/DWREncounterService.js"/>
<openmrs:htmlInclude file="/dwr/interface/DWRProviderService.js" />

<script type="text/javascript">
$j(document).ready(function(){
	$j('#PersonAttribute').data("counter", $j('#PersonAttributeCounter').val());
	$j('#Encounter').data("counter", $j('#EncounterCounter').val());
	
 $j('#save').click(function(){
    
               $j('#PersonAttributeCounter').val($j('#PersonAttribute').data("counter"));
               $j('#EncounterCounter').val($j('#Encounter').data("counter"));
          $j('form').submit();  
          
 });
 $j(':button.but').click(function(){
	var parent=$j(this).parent();
	var count=$j(parent).data("counter");
	var parentId=$j(parent).attr('id');
	var e = document.getElementById(parentId+"_conceptId");
	var strUser = e.options[e.selectedIndex].text;
	var generatedId=parentId+count;
	count= parseInt(count);
	count = count+ 1;
	$j(parent).data("counter",count);
	var spanId=generatedId+"_span";
	var v='</br><span id="'+spanId+'">'+strUser+'<input type="hidden" id="'+spanId+'_hid" name="'+spanId+'_hid" value="'+$j('#'+parentId+'_conceptId').val()+'"/><input id="'+spanId+'_remove" type="button" value="remove" onClick="$j(this).parent().remove();refresh(\''+parentId+'\')"/></span>';
	$j(parent).append(v);
	  
	 });
 $j(':button.addButton').click(function(){
	 
	 
	  var parent=$j(this).parent();
	  var count=$j(parent).data("counter");
	  
	  var parentId=$j(parent).attr('id');
	  var generatedId=parentId+count;

	  var mappingExists = false;
	  
	  DWRMyModuleService.getConceptMappings($j('#'+parentId+'_conceptId').val(),function (map){
	 
		count= parseInt(count);
		count = count+ 1;
	  $j(parent).data("counter",count);
	 
	  var spanId=generatedId+"_span";
	  var v='</br><span id="'+spanId+'">'+$j('#'+parentId+'_conceptId_selection').val()+'<input type="hidden" id="'+spanId+'_hid" name="'+spanId+'_hid" value="'+$j('#'+parentId+'_conceptId').val()+'"/><input id="'+spanId+'_remove" type="button" value="remove" onClick="$j(this).parent().remove();refresh(\''+parentId+'\')"/></span>';
	  
	  $j(parent).append(v);
	  
	  
	  });
	 });
 });

function refresh (superParentId) {
	var flag=true;
	$j('#'+superParentId+' span').each(function(index) {
		$j('#'+superParentId).data("counter",index+1);
		flag=false;
	    var spanId=this.id;
	    var newSpanId=superParentId+index+"_span";
	    this.id=newSpanId;
	    $j('#'+spanId+'_hid').attr('name',newSpanId+'_hid').attr('id',newSpanId+'_hid');
 		 $j('#'+spanId+'_remove').removeAttr('onclick',null).unbind('click').attr('id',newSpanId+'_remove').click(function() {
		 $j('#'+newSpanId).remove();
		 refresh(superParentId);
		 });
  });
}
</script>

<form method="get" >
<div class="boxHeader">Patient attributes to be added</div>
<div id="PersonAttribute" class="box">
Select attributes:
</br>
<select id="PersonAttribute_conceptId" name="PersonAttribute_conceptId" >
		<c:forEach var="personAttributeType" items="${personAttributeTypeList}">
		<option value="${personAttributeType.personAttributeTypeId}" id="PersonAttribute_conceptId"  >
					${personAttributeType.name}
		</option>
		</c:forEach>
</select>

<input type="button" value="ADD" id="but" class="but" />
<input type="hidden" id="PersonAttributeCounter" name="PersonAttributeCounter" value="${fn:length(FamilyHistory)}"/>
</br>
<c:forEach var="personAttributeType" items="${PersonAttribute}" varStatus="rIndex">
 
</br>
<span id="PersonAttribute${rIndex.index}_span"> ${personAttributeType.name}
<input type="hidden" id="PersonAttribute${rIndex.index}_span_hid" name="PersonAttribute${rIndex.index}_span_hid" value="${personAttributeType.personAttributeTypeId}"/>
<input id="'PersonAttribute'${rIndex.index}'_remove" type="button" value="remove" onClick="$j(this).parent().remove();refresh('PersonAttribute')"/>
</span>
</c:forEach>
</div>
</br>
<div class="boxHeader">Observation Filter</div>
<div id="Encounter" class="box">
<openmrs_tag:conceptField formFieldName="Encounter_conceptId"  formFieldId="Encounter_conceptId" />
<input type="button" value="ADD" id="addButton" class="addButton"/>
 <input type="hidden" id="EncounterCounter" name="EncounterCounter" value="${fn:length(Encounter)}"/>
 <c:forEach var="concept" items="${Encounter}" varStatus="rIndex">
 
</br>
<span id="Encounter${rIndex.index}_span"> ${concept.getName().getName()} 
<input type="hidden" id="Encounter${rIndex.index}_span_hid" name="Encounter${rIndex.index}_span_hid" value="${concept.getId()}"/>
<input id="'Encounter'${rIndex.index}'_remove" type="button" value="remove" onClick="$j(this).parent().remove();refresh('Encounter')"/>

</span>
</c:forEach>
 
</div>


<input type="submit" id="save" value='<spring:message code="general.save" />' />
<input type="hidden" id="PersonAttributeCounter" name="PersonAttributeCounter" value="${fn:length(PersonAttribute)}"/>
</form>


<%@ include file="/WEB-INF/template/footer.jsp"%>