package com.paj;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Task {
    private String pathToFile = "";
    private Boolean completed = false;
    private List<Job> jobList = new LinkedList<>();

    public Task(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public Boolean isCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public List<Job> getJobList() {
        return jobList;
    }

}
