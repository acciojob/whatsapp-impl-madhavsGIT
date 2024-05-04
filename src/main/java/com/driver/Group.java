package com.driver;

public class Group {
    private String name;
    private int numberOfParticipants;

    public Group(int numberOfParticipants, String name) {
        this.numberOfParticipants = numberOfParticipants;
        this.name = name;
    }

    public Group() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public void setNumberOfParticipants(int numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }
}
