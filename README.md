# Insurance Demo App
Demonstration of the BPMS rest API

## Setup
* Clone the included git repository into business central
* Build and deploy the project
* Create a bpms user with the **reviewer** role
* Deploy the WAR file(insuranceui-1.0.war) to business central
* The insurancerest project can be ignored
* Navigate to the UI and select the apply link
* Use the credentials of a BPMS user (does not need the reviewer role) and submit an application
* Navigate to the review applications link
* Use the credentials of a BPMS users with the reviewer role
* The list of applications will appear, select one to claim and start it
* Use the Ad Hoc buttons to signal the ad hoc tasks (they will simply log to the console of BPMS)
* Update the Status and click Complete to finish the human task and complete the process
* The application status will be printed to the BPMS console and the task will be removed from the task list
