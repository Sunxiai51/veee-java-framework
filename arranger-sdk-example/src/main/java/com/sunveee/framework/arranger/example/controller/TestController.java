package com.sunveee.framework.arranger.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sunveee.framework.arranger.api.ArrangerClient;
import com.sunveee.framework.arranger.api.vo.CreateTaskInput;

@RestController
@RequestMapping("/arranger")
public class TestController {

    @Autowired
    private ArrangerClient arrangerClient;

    @RequestMapping("/createAndLaunchTask")
    public String createAndLaunchTask(@RequestBody CreateTaskInput createTaskInput) {
        arrangerClient.createAndLaunchTask(createTaskInput);
        return "done";
    }

    @RequestMapping("/pauseTask")
    public String pauseTask(@RequestBody String taskId) {
        arrangerClient.pauseTask(taskId);
        return "done";
    }

    @RequestMapping("/proceedTask")
    public String proceedTask(@RequestBody String taskId) {
        arrangerClient.proceedTask(taskId);
        return "done";
    }

    @RequestMapping("/promoteSteps")
    public String promoteSteps(@RequestParam String taskId, @RequestParam Boolean recoverFailedStep) {
        arrangerClient.promoteSteps(taskId, recoverFailedStep);
        return "done";
    }

    @RequestMapping("/stopTask")
    public String stopTask(@RequestBody String taskId) {
        arrangerClient.stopTask(taskId);
        return "done";
    }

}
