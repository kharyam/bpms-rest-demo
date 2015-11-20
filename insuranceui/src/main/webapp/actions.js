// Register UI callbacks
$(document).ready(function() {

  $("#setCredentials").click(showApplication);

  $("#getApplications").click(getApplications);

  $('#taskListTable').bootstrapTable({}).on('click-row.bs.table', function(e, row, $element) {
    startReview(row);
  });

  $("#apply").click(apply);

  $("#taskA").click(adHocA);

  $("#taskB").click(adHocB);

  $("#save").click(saveReview);

  $("#complete").click(completeReview);
  
  $("button[name='refresh']").click(getApplications);

});


function getUrl(){
  return "http://localhost:8080/business-central";
}

function showApplication() {
  // Just make sure something was entered, in reality we would validate the credentials as well.
  if ($('#userName').val().length > 0 && $('#password').val().length > 0) {
    $('#credentials').hide(500); 
    $('#applicationForm').show(500);
  }
}

function apply() {
  $.ajax({
    type : "POST",
    url : getUrl() + "/rest/runtime/com.example:insurance:1.0/process/insurance.mainProcess/start",

    data: "map_pv_firstName=" + $('#firstName').val() + 
          "&map_pv_lastName=" + $('#lastName').val() + 
          "&map_pv_age=" + $('#age').val() + "i" +
          "&map_pv_income=" + $('#income').val() + "i",

    headers: {"Authorization": "Basic " + btoa($('#userName').val() + ":" + $('#password').val())},
            
    success : function (data,status) {
            $('#applicationForm').hide(500);
            $('#thankYouForm').show(500);
          },

    error : function (data,status) {alert("Something went wrong, try again later. (status = " + status);}
      
  });
  
}

function getApplications() {
	getTaskList($("#userName").val());
}

function getTaskList(user){
    $.ajax({
	    type: "GET",
	    dataType: "json",
	    url: getUrl() + "/rest/task/query?potentialOwner="+user,
	    headers: {"Authorization": "Basic " + btoa($('#userName').val() + ":" + $('#password').val())},
	    success : taskListCB,
	    error : function (data,status) {alert("Something went wrong, try again later. (status = " + status);}
    	});
}

function taskListCB(taskList) {
	var tasks = taskList.taskSummaryList;
	$('#taskListTable').bootstrapTable('load', tasks);
  $('#credentials').hide(500);
	$('#tasksPanel').show(500);
}

function dateFormatter(value) {
  return  new Date(value).toString();
}

function startReview(row) {
	$('#applicationTitle').text("Application " + row.processInstanceId + " - " + row.name);
	
	// Store the process instance id and task id in hidden fields for later use
	$('#processInstanceId').val(row.processInstanceId);
	$('#taskId').val(row.id);
	
	if (row.status == "Ready") {
		claimTask();
	}
	
	if (row.status == "Reserved") {
		startTask();
	}
	
	getTaskVariables();
	
	$('#tasksPanel').hide(250);
	$('#reviewForm').show(250);	
}

function claimTask(){
    $.ajax({
	    type: "POST",
	    url: getUrl() + "/rest/task/" + $('#taskId').val() + "/claim",
	    headers: {"Authorization": "Basic " + btoa($('#userName').val() + ":" + $('#password').val())},
	    success : function(){console.log("Successfully claimed task");startTask();},
	    error : function (data,status) {console.log("Something went wrong, try again later. (status = " + status);}
		});
}

function startTask(){
    $.ajax({
	    type: "POST",
	    url: getUrl() + "/rest/task/" + $('#taskId').val() + "/start",
	    headers: {"Authorization": "Basic " + btoa($('#userName').val() + ":" + $('#password').val())},
	    success : function(){console.log("Successfully started task")},
	    error : function (data,status) {console.log("Something went wrong, try again later. (status = " + status);}
		});
}

function getTaskVariables() {
    $.ajax({
	    type: "GET",
	    dataType: "json",
	    url: getUrl() + "/rest/query/runtime/task?taskid=" + $('#taskId').val(),
	    headers: {"Authorization": "Basic " + btoa($('#userName').val() + ":" + $('#password').val())},
	    success : populateReviewFormCB,
	    error : function (data,status) {alert("Something went wrong, try again later. (status = " + status);}
    	});
}


function populateReviewFormCB(taskInfo) {
  var variables = taskInfo.taskInfoList[0].variables;
  
  for (i = 0 ; i < variables.length ; i++) {
    if(variables[i].name == "pv_age") {
      $("#age").val(variables[i].value)
    } else if (variables[i].name == "pv_firstName") {
      $("#firstName").val(variables[i].value)
    } else if(variables[i].name == "pv_lastName") {
      $("#lastName").val(variables[i].value)
    } else if(variables[i].name == "pv_income") {
      $("#income").val(variables[i].value)
    } else if(variables[i].name == "pv_status") {
      $("#status").val(variables[i].value)
    }
  }
  
}

function saveReview() {
  var taskId = $('#taskId').val();
  alert("This feature is not yet implemented");
}

function completeReview() {
  
  $.ajax({
    type: "POST",
    url: getUrl() + "/rest/task/"+ $('#taskId').val() +"/complete",

    data: "map_out_firstName=" + $('#firstName').val() + 
          "&map_out_lastName=" + $('#lastName').val() + 
          "&map_out_age=" + $('#age').val() + "i" +
          "&map_out_income=" + $('#income').val() + "i" +
          "&map_out_status=" + $('#status').val(),

    headers: {"Authorization": "Basic " + btoa($('#userName').val() + ":" + $('#password').val())},
            
    success : function (data,status) {
            getApplications(); // refresh application list
            $('#reviewForm').hide(500);
            $('#taskPanel').show(500);
          },

    error : function (data,status) {alert("Something went wrong, try again later. (status = " + status);}
  });
}

function adHocA() {
  signal("Task A");
}

function adHocB() {
  signal("Task B");
}

function signal(source) {
	$.ajax({
		type : "POST",
		url : getUrl() + "/rest/runtime/com.example:insurance:1.0/process/instance/" + $('#processInstanceId').val() + "/signal?signal=" + source, 
		headers: {"Authorization": "Basic " + btoa($('#userName').val() + ":" + $('#password').val())},
		success : function (data,status) {console.log("Successfully signalled " + source);},
		error : function (data,status) {alert("Failed to signal " + source + " (status = " + status + ")");}
	});  
}
