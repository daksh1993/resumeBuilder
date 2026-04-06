package com.resumebuilder.cli;

import com.resumebuilder.backend.customsection.AbstractCustomSection;
import com.resumebuilder.backend.customsection.FlexibleCustomSection;
import com.resumebuilder.backend.dao.ResumeDAO;
import com.resumebuilder.backend.exception.CustomSectionException;
import com.resumebuilder.backend.exception.DatabaseException;
import com.resumebuilder.backend.exception.InvalidInputException;
import com.resumebuilder.backend.exception.ResumeNotFoundException;
import com.resumebuilder.backend.model.Education;
import com.resumebuilder.backend.model.Experience;
import com.resumebuilder.backend.model.Project;
import com.resumebuilder.backend.model.Resume;
import com.resumebuilder.backend.model.Skill;

import java.util.List;
import java.util.Scanner;

public class ResumeEditorScreen {
    private final Resume resume;
    private final ResumeDAO resumeDAO;
    private final Scanner scanner;
    private boolean isNewResume;

    public ResumeEditorScreen(Resume resume, ResumeDAO resumeDAO) {
        this.resume = resume;
        this.resumeDAO = resumeDAO;
        this.scanner = new Scanner(System.in);
        this.isNewResume = (resume.getId() == 0);
    }

    public void show() {
        while (true) {
            System.out.println("\nRESUM EDITR\n");
            System.out.println("Curent Resum " + (resume.getTitle() != null ? resume.getTitle() : "Untitld"));
            System.out.println("Template " + resume.getTemplateName() + " Theme " + resume.getColorTheme());

            System.out.println("\nSECTIONS");
            System.out.println("  [1] Personl Informaton");
            System.out.println("  [2] Summry");
            System.out.println("  [3] Educaton " + resume.getEducationList().size() + " entris");
            System.out.println("  [4] Experince " + resume.getExperienceList().size() + " entris");
            System.out.println("  [5] Projcts " + resume.getProjectList().size() + " entris");
            System.out.println("  [6] Skills " + resume.getSkillList().size() + " entries");
            System.out.println("  [7] Templat and Them");
            System.out.println("  [8] Custom Section " + resume.getCustomSections().size() + " entris");
            System.out.println("  [S] Sav and Return");
            System.out.println("  [Q] Discard and Return\n");

            System.out.print("Choose section to edit ");
            String choice = scanner.nextLine().toLowerCase().trim();

            switch (choice) {
                case "1":
                    editPersonalInfo();
                    break;
                case "2":
                    editSummary();
                    break;
                case "3":
                    editEducation();
                    break;
                case "4":
                    editExperience();
                    break;
                case "5":
                    editProjects();
                    break;
                case "6":
                    editSkills();
                    break;
                case "7":
                    editTemplateAndTheme();
                    break;
                case "8":
                    editCustomSections();
                    break;
                case "s":
                    if (saveResume()) {
                        return;
                    }
                    break;
                case "q":
                    System.out.print("Discard all changs y/n ");
                    String confirm = scanner.nextLine().toLowerCase().trim();
                    if (confirm.equals("y") || confirm.equals("yes")) {
                        return;
                    }
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void editPersonalInfo() {
        System.out.println("\nPERSONL INFORMATON\n");
        System.out.println("Leave blank to keep current value\n");

        System.out.print("Full Name [" + orEmpty(resume.getFullName()) + "] ");
        String fullName = scanner.nextLine().trim();
        if (!fullName.isEmpty()) resume.setFullName(fullName);

        System.out.print("Email [" + orEmpty(resume.getEmail()) + "] ");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty()) resume.setEmail(email);

        System.out.print("Phone [" + orEmpty(resume.getPhone()) + "] ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty()) resume.setPhone(phone);

        System.out.print("Address [" + orEmpty(resume.getAddress()) + "] ");
        String address = scanner.nextLine().trim();
        if (!address.isEmpty()) resume.setAddress(address);

        System.out.println("Personl informaton updatd");
    }

    private void editSummary() {
        System.out.println("\nPROFESSIONAL SUMMRY\n");
        System.out.println("Curent summry");
        System.out.println(orEmpty(resume.getSummary()));

        System.out.println("\nEntr new summry typ END on a new line when don");
        StringBuilder summary = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            if (summary.length() > 0) {
                summary.append("\n");
            }
            summary.append(line);
        }

        if (summary.length() > 0) {
            resume.setSummary(summary.toString());
            System.out.println("Summry updatd");
        } else {
            System.out.println("Summry unchangd");
        }
    }

    private void editEducation() {
        while (true) {
            System.out.println("\nEDUCATION\n");

            List<Education> educationList = resume.getEducationList();

            if (educationList.isEmpty()) {
                System.out.println("  No education entries");
            } else {
                for (int i = 0; i < educationList.size(); i++) {
                    Education education = educationList.get(i);
                    System.out.println("  " + (i + 1) + " " + education.getDegree() + " " + education.getUniversity());
                }
            }

            System.out.println("\nOtions");
            System.out.println("  [A] Add Entry");
            System.out.println("  [E] Edit Entry");
            System.out.println("  [D] Delete Entry");
            System.out.println("  [B] Back\n");

            System.out.print("Choose option ");
            String choice = scanner.nextLine().toLowerCase().trim();

            switch (choice) {
                case "a":
                    addEducation();
                    break;
                case "e":
                    editEducationEntry(educationList);
                    break;
                case "d":
                    deleteEducationEntry(educationList);
                    break;
                case "b":
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void addEducation() {
        System.out.println("\nADD EDUCATION\n");

        System.out.print("Degree or Qualification ");
        String degree = readNonEmpty();

        System.out.print("University ");
        String university = readNonEmpty();

        System.out.print("Year for example 2020 to 2024 ");
        String year = scanner.nextLine().trim();

        System.out.print("CGPA or Grade ");
        String cgpa = scanner.nextLine().trim();

        Education education = new Education();
        education.setDegree(degree);
        education.setUniversity(university);
        education.setYear(year);
        education.setCgpa(cgpa);

        resume.getEducationList().add(education);
        System.out.println("Education entry added");
    }

    private void editEducationEntry(List<Education> list) {
        if (list.isEmpty()) {
            System.out.println("No entries to edit");
            return;
        }

        System.out.print("Enter entry number 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        Education education = list.get(index - 1);

        System.out.println("\nEDIT EDUCATION\n");
        System.out.println("Leave blank to keep current value\n");

        System.out.print("Degree [" + education.getDegree() + "] ");
        String degree = scanner.nextLine().trim();
        if (!degree.isEmpty()) education.setDegree(degree);

        System.out.print("University [" + education.getUniversity() + "] ");
        String university = scanner.nextLine().trim();
        if (!university.isEmpty()) education.setUniversity(university);

        System.out.print("Year [" + orEmpty(education.getYear()) + "] ");
        String year = scanner.nextLine().trim();
        if (!year.isEmpty()) education.setYear(year);

        System.out.print("CGPA [" + orEmpty(education.getCgpa()) + "] ");
        String cgpa = scanner.nextLine().trim();
        if (!cgpa.isEmpty()) education.setCgpa(cgpa);

        System.out.println("Entry updatd");
    }

    private void deleteEducationEntry(List<Education> list) {
        if (list.isEmpty()) {
            System.out.println("No entries to delete");
            return;
        }

        System.out.print("Enter entry number to delete 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        list.remove(index - 1);
        System.out.println("Entry deletd");
    }

    private void editExperience() {
        while (true) {
            System.out.println("\nEXPERIENCE\n");

            List<Experience> experienceList = resume.getExperienceList();

            if (experienceList.isEmpty()) {
                System.out.println("  No experience entries");
            } else {
                for (int i = 0; i < experienceList.size(); i++) {
                    Experience experience = experienceList.get(i);
                    System.out.println("  " + (i + 1) + " " + experience.getRole() + " " + experience.getCompany());
                }
            }

            System.out.println("\nOptions");
            System.out.println("  [A] Add Entry");
            System.out.println("  [E] Edit Entry");
            System.out.println("  [D] Delete Entry");
            System.out.println("  [B] Back\n");

            System.out.print("Choose option ");
            String choice = scanner.nextLine().toLowerCase().trim();

            switch (choice) {
                case "a":
                    addExperience();
                    break;
                case "e":
                    editExperienceEntry(experienceList);
                    break;
                case "d":
                    deleteExperienceEntry(experienceList);
                    break;
                case "b":
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void addExperience() {
        System.out.println("\nADD EXPERIENCE\n");

        System.out.print("Job Title or Role ");
        String role = readNonEmpty();

        System.out.print("Company ");
        String company = readNonEmpty();

        System.out.print("Duration for example Jan 2022 to Present ");
        String duration = scanner.nextLine().trim();

        System.out.print("Description ");
        String description = scanner.nextLine().trim();

        Experience experience = new Experience();
        experience.setRole(role);
        experience.setCompany(company);
        experience.setDuration(duration);
        experience.setDescription(description);

        resume.getExperienceList().add(experience);
        System.out.println("Experience entry added");
    }

    private void editExperienceEntry(List<Experience> list) {
        if (list.isEmpty()) {
            System.out.println("No entries to edit");
            return;
        }

        System.out.print("Enter entry number 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        Experience experience = list.get(index - 1);

        System.out.println("\nEDIT EXPERIENCE\n");
        System.out.println("Leave blank to keep current value\n");

        System.out.print("Job Title or Role [" + experience.getRole() + "] ");
        String role = scanner.nextLine().trim();
        if (!role.isEmpty()) experience.setRole(role);

        System.out.print("Company [" + experience.getCompany() + "] ");
        String company = scanner.nextLine().trim();
        if (!company.isEmpty()) experience.setCompany(company);

        System.out.print("Duration [" + orEmpty(experience.getDuration()) + "] ");
        String duration = scanner.nextLine().trim();
        if (!duration.isEmpty()) experience.setDuration(duration);

        System.out.print("Description [" + orEmpty(experience.getDescription()) + "] ");
        String description = scanner.nextLine().trim();
        if (!description.isEmpty()) experience.setDescription(description);

        System.out.println("Entry updated");
    }

    private void deleteExperienceEntry(List<Experience> list) {
        if (list.isEmpty()) {
            System.out.println("No entries to delete");
            return;
        }

        System.out.print("Enter entry number to delete 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        list.remove(index - 1);
        System.out.println("Entry deleted");
    }

    private void editProjects() {
        while (true) {
            System.out.println("\nPROJECTS\n");

            List<Project> projectList = resume.getProjectList();

            if (projectList.isEmpty()) {
                System.out.println("  No project entries");
            } else {
                for (int i = 0; i < projectList.size(); i++) {
                    Project project = projectList.get(i);
                    System.out.println("  " + (i + 1) + " " + project.getTitle());
                }
            }

            System.out.println("\nOptions");
            System.out.println("  [A] Add Entry");
            System.out.println("  [E] Edit Entry");
            System.out.println("  [D] Delete Entry");
            System.out.println("  [B] Back\n");

            System.out.print("Choose option ");
            String choice = scanner.nextLine().toLowerCase().trim();

            switch (choice) {
                case "a":
                    addProject();
                    break;
                case "e":
                    editProjectEntry(projectList);
                    break;
                case "d":
                    deleteProjectEntry(projectList);
                    break;
                case "b":
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void addProject() {
        System.out.println("\nADD PROJECT\n");

        System.out.print("Project Title ");
        String title = readNonEmpty();

        System.out.print("Description ");
        String description = scanner.nextLine().trim();

        Project project = new Project();
        project.setTitle(title);
        project.setDescription(description);

        resume.getProjectList().add(project);
        System.out.println("Project entry added");
    }

    private void editProjectEntry(List<Project> list) {
        if (list.isEmpty()) {
            System.out.println("No entries to edit");
            return;
        }

        System.out.print("Enter entry number 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        Project project = list.get(index - 1);

        System.out.println("\nEDIT PROJECT\n");
        System.out.println("Leave blank to keep current value\n");

        System.out.print("Project Title [" + project.getTitle() + "] ");
        String title = scanner.nextLine().trim();
        if (!title.isEmpty()) project.setTitle(title);

        System.out.print("Description [" + orEmpty(project.getDescription()) + "] ");
        String description = scanner.nextLine().trim();
        if (!description.isEmpty()) project.setDescription(description);

        System.out.println("Entry updated");
    }

    private void deleteProjectEntry(List<Project> list) {
        if (list.isEmpty()) {
            System.out.println("No entries to delete");
            return;
        }

        System.out.print("Enter entry number to delete 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        list.remove(index - 1);
        System.out.println("Entry deleted");
    }

    private void editSkills() {
        while (true) {
            System.out.println("\nSKILLS\n");

            List<Skill> skillList = resume.getSkillList();

            if (skillList.isEmpty()) {
                System.out.println("  No skills");
            } else {
                for (int i = 0; i < skillList.size(); i++) {
                    Skill skill = skillList.get(i);
                    System.out.println("  " + (i + 1) + " " + skill.getSkillName());
                }
            }

            System.out.println("\nOptions");
            System.out.println("  [A] Add Skill");
            System.out.println("  [E] Edit Skill");
            System.out.println("  [D] Delete Skill");
            System.out.println("  [B] Back\n");

            System.out.print("Choose option ");
            String choice = scanner.nextLine().toLowerCase().trim();

            switch (choice) {
                case "a":
                    addSkill();
                    break;
                case "e":
                    editSkillEntry(skillList);
                    break;
                case "d":
                    deleteSkillEntry(skillList);
                    break;
                case "b":
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void addSkill() {
        System.out.println("\nADD SKILL\n");

        System.out.print("Skill Name ");
        String name = readNonEmpty();

        Skill skill = new Skill();
        skill.setSkillName(name);

        resume.getSkillList().add(skill);
        System.out.println("Skill added");
    }

    private void editSkillEntry(List<Skill> list) {
        if (list.isEmpty()) {
            System.out.println("No skills to edit");
            return;
        }

        System.out.print("Enter skill number 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        Skill skill = list.get(index - 1);

        System.out.println("\nEDIT SKILL\n");

        System.out.print("Skill Name [" + skill.getSkillName() + "] ");
        String name = readNonEmpty();
        skill.setSkillName(name);

        System.out.println("Skill updatd");
    }

    private void deleteSkillEntry(List<Skill> list) {
        if (list.isEmpty()) {
            System.out.println("No skills to delete");
            return;
        }

        System.out.print("Enter skill number to delete 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        list.remove(index - 1);
        System.out.println("Skill deleted");
    }

    private void editTemplateAndTheme() {
        System.out.println("\nTEMPLATE AND THEME\n");

        String[] templates = {"Classic", "Modern", "Creative", "Minimal"};
        System.out.println("Select Template");
        for (int i = 0; i < templates.length; i++) {
            System.out.println((i + 1) + " " + templates[i]);
        }
        System.out.print("\nChoose an option ");
        int templateChoice = readInt(1, templates.length);
        resume.setTemplateName(templates[templateChoice - 1]);

        String[] themes = {"Blue", "Green", "Grey"};
        System.out.println("\nSelect Color Theme");
        for (int i = 0; i < themes.length; i++) {
            System.out.println((i + 1) + " " + themes[i]);
        }
        System.out.print("\nChoose an option ");
        int themeChoice = readInt(1, themes.length);
        resume.setColorTheme(themes[themeChoice - 1]);

        System.out.println("Template and theme updated");
    }

    private void editCustomSections() {
        while (true) {
            List<AbstractCustomSection> customSections = resume.getCustomSections();

            System.out.println("\nCUSTOM SECTIONS\n");
            if (customSections.isEmpty()) {
                System.out.println("  No custom section entries");
            } else {
                for (int i = 0; i < customSections.size(); i++) {
                    AbstractCustomSection section = customSections.get(i);
                    System.out.println("  " + (i + 1) + " " + section.getHeader() + " [" + section.getOccupationName() + "]");
                }
            }

            System.out.println("\nOptions");
            System.out.println("  [A] Add Section");
            System.out.println("  [E] Edit Section");
            System.out.println("  [V] View Section");
            System.out.println("  [D] Delete Section");
            System.out.println("  [B] Back\n");

            System.out.print("Choose option ");
            String choice = scanner.nextLine().toLowerCase().trim();

            switch (choice) {
                case "a":
                    addCustomSection();
                    break;
                case "e":
                    editCustomSectionEntry(customSections);
                    break;
                case "v":
                    viewCustomSectionEntry(customSections);
                    break;
                case "d":
                    deleteCustomSectionEntry(customSections);
                    break;
                case "b":
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void addCustomSection() {
        try {
            String occupationName = selectOccupation();
            System.out.print("Section Header ");
            String header = readNonEmptyOrThrow();

            AbstractCustomSection section = createCustomSection(header, occupationName);
            section.setResumeId(resume.getId());
            section.inputParameters(scanner);
            resume.getCustomSections().add(section);
            System.out.println("Custom section added");
        } catch (InvalidInputException e) {
            System.out.println("INPUT ERROR " + e.getMessage());
        } catch (CustomSectionException e) {
            System.out.println("SECTION ERROR " + e.getMessage());
        }
    }

    private void editCustomSectionEntry(List<AbstractCustomSection> list) {
        if (list.isEmpty()) {
            System.out.println("No custom sections to edit");
            return;
        }

        System.out.print("Enter section number 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        AbstractCustomSection section = list.get(index - 1);

        try {
            System.out.print("Section Header [" + section.getHeader() + "] ");
            String header = scanner.nextLine().trim();
            if (!header.isEmpty()) {
                section.setHeader(header);
            }

            section.inputParameters(scanner);
            System.out.println("Custom section updated");
        } catch (InvalidInputException e) {
            System.out.println("INPUT ERROR " + e.getMessage());
        } catch (CustomSectionException e) {
            System.out.println("SECTION ERROR " + e.getMessage());
        }
    }

    private void viewCustomSectionEntry(List<AbstractCustomSection> list) {
        if (list.isEmpty()) {
            System.out.println("No custom sections to view");
            return;
        }

        System.out.print("Enter section number 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        list.get(index - 1).displaySection();
    }

    private void deleteCustomSectionEntry(List<AbstractCustomSection> list) {
        if (list.isEmpty()) {
            System.out.println("No custom sections to delete");
            return;
        }

        System.out.print("Enter section number to delete 0 to cancel ");
        int index = readInt(0, list.size());
        if (index == 0) return;

        list.remove(index - 1);
        System.out.println("Custom section deleted");
    }

    private String selectOccupation() throws InvalidInputException {
        System.out.print("Enter Occupation Name ");
        return readNonEmptyOrThrow();
    }

    private AbstractCustomSection createCustomSection(String header, String occupationName) {
        return new FlexibleCustomSection(header, occupationName);
    }

    private boolean saveResume() {
        if (resume.getTitle() == null || resume.getTitle().isEmpty() || resume.getTitle().equals("My Resume")) {
            System.out.println("\nSAVE RESUME\n");
            System.out.print("Enter resume title ");
            String title = readNonEmpty();
            resume.setTitle(title);
        }

        try {
            resumeDAO.saveResume(resume);
            System.out.println("Resum savd succesfully");
            return true;
        } catch (ResumeNotFoundException e) {
            System.out.println("ERROR Resume not found " + e.getMessage());
            return false;
        } catch (DatabaseException e) {
            System.out.println("ERROR Eror saving resum " + e.getMessage());
            return false;
        }
    }

    private String orEmpty(String value) {
        return value != null ? value : "";
    }

    private String readNonEmpty() {
        while (true) {
            try {
                return readNonEmptyOrThrow();
            } catch (InvalidInputException e) {
                System.out.print(e.getMessage() + " Please try again ");
            }
        }
    }

    private String readNonEmptyOrThrow() throws InvalidInputException {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            throw new InvalidInputException("This field cannot be empty.");
        }
        return input;
    }

    private int readInt(int min, int max) {
        while (true) {
            try {
                return readIntOrThrow(min, max);
            } catch (InvalidInputException e) {
                System.out.print(e.getMessage() + " ");
            }
        }
    }

    private int readIntOrThrow(int min, int max) throws InvalidInputException {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            throw new InvalidInputException("Input cannot be empty.");
        }

        try {
            int value = Integer.parseInt(input);
            if (value < min || value > max) {
                throw new InvalidInputException("Please enter a number between " + min + " and " + max + ".");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid input Please enter a valid number.");
        }
    }
}
