package com.resumebuilder.backend.customsection;

import com.resumebuilder.backend.exception.CustomSectionException;
import com.resumebuilder.backend.exception.InvalidInputException;
import com.resumebuilder.backend.model.CustomParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class AbstractCustomSection {
    private int id;
    private int resumeId;
    private String header;
    private String occupationName;
    protected List<CustomParameter> parameterList;

    public AbstractCustomSection() {
        this.parameterList = new ArrayList<>();
    }

    public AbstractCustomSection(String header, String occupationName) {
        this();
        this.header = header;
        this.occupationName = occupationName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }

    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }
    
    public String getOccupationName() { return occupationName; }
    public void setOccupationName(String occupationName) { this.occupationName = occupationName; }

    public abstract void inputParameters(Scanner scanner) throws InvalidInputException, CustomSectionException;
    
    public void displaySection() {
        printSection();
    }
    
    public List<CustomParameter> getParameters() {
        return parameterList;
    }

    public void setParameters(List<CustomParameter> parameterList) {
        this.parameterList = new ArrayList<>(parameterList);
    }

    protected String readRequiredValue(Scanner scanner, String prompt) throws InvalidInputException {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) {
            throw new InvalidInputException("Input cannot be empty.");
        }
        return value;
    }

    protected int readPositiveInt(Scanner scanner, String prompt) throws InvalidInputException {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            throw new InvalidInputException("Input cannot be empty.");
        }

        try {
            int value = Integer.parseInt(input);
            if (value <= 0) {
                throw new InvalidInputException("Please enter a number greater than zero.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Please enter a valid numeric value.");
        }
    }

    protected void addParameter(String name, String value) throws InvalidInputException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Parameter name cannot be empty.");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException("Parameter value cannot be empty.");
        }
        parameterList.add(new CustomParameter(name.trim(), value.trim()));
    }

    protected void validateSection() throws CustomSectionException {
        if (header == null || header.trim().isEmpty()) {
            throw new CustomSectionException("Section header cannot be empty.");
        }
        if (occupationName == null || occupationName.trim().isEmpty()) {
            throw new CustomSectionException("Occupation name cannot be empty.");
        }
        if (getParameters() == null || getParameters().isEmpty()) {
            throw new CustomSectionException("Custom section must contain at least one parameter.");
        }
    }

    protected void printSection() {
        System.out.println("\n" + getHeader() + " [" + getOccupationName() + "]");
        for (CustomParameter parameter : getParameters()) {
            System.out.println("  " + parameter.getName() + " : " + parameter.getValue());
        }
    }
}
