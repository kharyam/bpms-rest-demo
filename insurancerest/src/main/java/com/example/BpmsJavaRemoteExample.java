package com.example;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.manager.audit.NodeInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.api.RemoteRuntimeEngineFactory;

/**
 * A class demonstrating the use of the BPMS Remote Java (REST) API
 *
 */
public class BpmsJavaRemoteExample {

  private static final String DEPLOYMENT_ID = "com.example:insurance:1.0";
  private static final String APP_URL = "http://localhost:8080/business-central/";
  private static final String USER = "bob";
  private static final String PASSWORD = "p@ssw0rd";

  private KieSession ksession;
  private AuditService auditService;
  private TaskService taskService;

  /**
   * Default constructor, use predefined parameters
   */
  public BpmsJavaRemoteExample() {
    this(DEPLOYMENT_ID, APP_URL, USER, PASSWORD);
  }

  /**
   * Creates the runtime engine and services
   * 
   * @param deploymentId
   * @param appUrl
   * @param user
   * @param password
   */
  public BpmsJavaRemoteExample(String deploymentId, String appUrl, String user, String password) {
    URL url = null;

    try {
      url = new URL(appUrl);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    RuntimeEngine factory = RemoteRuntimeEngineFactory.newRestBuilder().addUrl(url).addUserName(user)
        .addPassword(password).addDeploymentId(deploymentId).build();

    ksession = factory.getKieSession();
    taskService = factory.getTaskService();
    auditService = factory.getAuditService();
  }

  /**
   * Start an example business process
   * 
   * @return process id
   */
  public long startProcess() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("pv_firstName", "Joe");
    variables.put("pv_lastName", "Smith");
    variables.put("pv_age", new Integer(47));
    variables.put("pv_income", new Integer(100000));

    ProcessInstance processInstance = ksession.startProcess("insurance.mainProcess", variables);
    System.out.println("Created process id " + processInstance.getId() + "\n");
    return processInstance.getId();
  }

  /**
   * Exercise some of the API functionality
   * 
   * @param args
   */
  public static void main(String args[]) {
    BpmsJavaRemoteExample example = new BpmsJavaRemoteExample();
    long processId = example.startProcess();
    List<TaskSummary> tasks = example.taskService.getTasksAssignedAsPotentialOwner(USER, "EN_UK");

    System.out.println("Claimable processes:");
    for (TaskSummary summary : tasks) {
      System.out.println(summary.getName());
      Map<String, Object> content = example.taskService.getTaskContent(summary.getId());
      Set<String> keys = content.keySet();
      for (String key : keys) {
        System.out.println("\t" + key + " => " + content.get(key));
      }
    }

    example.ksession.signalEvent("Task A", null, processId);

    List<? extends NodeInstanceLog> completedNodes = example.auditService.findNodeInstances(processId);

    System.out.println("\nStarted Nodes:");
    for (NodeInstanceLog node : completedNodes) {
      System.out.println(node.getNodeId() + " - " + node.getNodeName() + " - " + node.getNodeType() + " - "
          + node.getType());
    }

  }
}
