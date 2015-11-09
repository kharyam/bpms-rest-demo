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

public class BpmsJavaRemoteExample {

  private static final String DEPLOYMENT_ID = "com.example:insurance:1.0";
  private static final String APP_URL = "http://localhost:8080/business-central/";
  private static final String USER = "bob";
  private static final String PASSWORD = "p@ssw0rd";

  private KieSession ksession;
  private AuditService auditService;
  private TaskService taskService;

  public long startProcess() {

    URL url = null;

    try {
      url = new URL(APP_URL);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    RuntimeEngine factory = RemoteRuntimeEngineFactory.newRestBuilder().addUrl(url).addUserName(USER)
        .addPassword(PASSWORD).addDeploymentId(DEPLOYMENT_ID).build();

    ksession = factory.getKieSession();
    taskService = factory.getTaskService();
    auditService = factory.getAuditService();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("pv_firstName", "Joe");
    variables.put("pv_lastName", "Smith");
    variables.put("pv_age", new Integer(47));
    variables.put("pv_income", new Integer(100000));
    ProcessInstance pi = ksession.startProcess("insurance.mainProcess", variables);
    System.out.println("Created process id " + pi.getId() + "\n");
    return pi.getId();
  }

  public static void main(String args[]) {
    BpmsJavaRemoteExample example = new BpmsJavaRemoteExample();
    long pid = example.startProcess();
    List<TaskSummary> tasks = example.taskService.getTasksAssignedAsPotentialOwner(USER, "EN_UK");

    System.out.println("Claimable processes:");
    for (TaskSummary summary : tasks) {
      System.out.println(summary.getName());
      Map<String, Object> content = example.taskService.getTaskContent(summary.getId());
      Set<String> keys = content.keySet();
      for (String key : keys) {
        System.out.println("\t"+key + " => " + content.get(key));
      }
    }
    
    example.ksession.signalEvent("Task A",null, pid);

    List<? extends NodeInstanceLog> completedNodes = example.auditService.findNodeInstances(pid);
    
    System.out.println("\nStarted Nodes:");
    for (NodeInstanceLog node : completedNodes) {
      System.out.println(node.getNodeId() + " - " + node.getNodeName() + " - " + node.getNodeType() + " - " + node.getType());
    }
    
  }
}
