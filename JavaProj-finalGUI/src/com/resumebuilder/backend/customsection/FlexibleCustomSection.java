package com.resumebuilder.backend.customsection;

import com.resumebuilder.backend.exception.CustomSectionException;
import com.resumebuilder.backend.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.Scanner;

public class FlexibleCustomSection extends AbstractCustomSection {

    public FlexibleCustomSection(String header, String occupationName) {
        super(header, occupationName);
    }

    @Override
    public void inputParameters(Scanner scanner) throws InvalidInputException, CustomSectionException {
        parameterList = new ArrayList<>();
        int parameterCount = readPositiveInt(scanner, "Number of parameters ");
        for (int i = 0; i < parameterCount; i++) {
            String name = readRequiredValue(scanner, "Parameter name " + (i + 1) + " ");
            String value = readRequiredValue(scanner, "Parameter value " + (i + 1) + " ");
            addParameter(name, value);
        }
        validateSection();
    }
}
