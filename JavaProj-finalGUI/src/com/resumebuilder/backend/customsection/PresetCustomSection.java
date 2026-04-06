package com.resumebuilder.backend.customsection;

import com.resumebuilder.backend.exception.CustomSectionException;
import com.resumebuilder.backend.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PresetCustomSection extends AbstractCustomSection {
    private static final String DEVELOPER = "Developer";
    private static final String DESIGNER = "Designer";
    private static final String RESEARCHER = "Researcher";
    private static final String CUSTOM = "Custom";

    private final String[] fieldNames;

    public PresetCustomSection(String header, String occupationName, String[] fieldNames) {
        super(header, normalizeOccupationName(occupationName));
        this.fieldNames = fieldNames != null ? fieldNames.clone() : new String[0];
    }

    public static List<String> getOccupationOptions() {
        return Arrays.asList(DEVELOPER, DESIGNER, RESEARCHER, CUSTOM);
    }

    public static String[] getFieldNamesForOccupation(String occupationName) {
        if (DEVELOPER.equalsIgnoreCase(occupationName)) {
            return new String[]{"GitHub", "Tech Stack", "Project Link"};
        }
        if (DESIGNER.equalsIgnoreCase(occupationName)) {
            return new String[]{"Portfolio", "Tools", "Design Type"};
        }
        if (RESEARCHER.equalsIgnoreCase(occupationName)) {
            return new String[]{"Paper Title", "Journal", "Year"};
        }
        return null;
    }

    public static boolean isCustomOption(String occupationName) {
        return CUSTOM.equalsIgnoreCase(occupationName);
    }

    private static String normalizeOccupationName(String occupationName) {
        if (DEVELOPER.equalsIgnoreCase(occupationName)) {
            return DEVELOPER;
        }
        if (DESIGNER.equalsIgnoreCase(occupationName)) {
            return DESIGNER;
        }
        if (RESEARCHER.equalsIgnoreCase(occupationName)) {
            return RESEARCHER;
        }
        return occupationName;
    }

    @Override
    public void inputParameters(Scanner scanner) throws InvalidInputException, CustomSectionException {
        parameterList = new ArrayList<>();
        for (String fieldName : fieldNames) {
            addParameter(fieldName, readRequiredValue(scanner, fieldName + " "));
        }
        validateSection();
    }
}
