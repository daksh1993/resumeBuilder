package com.resumebuilder.backend.dao;

import com.resumebuilder.backend.customsection.AbstractCustomSection;
import com.resumebuilder.backend.customsection.FlexibleCustomSection;
import com.resumebuilder.backend.customsection.PresetCustomSection;
import com.resumebuilder.backend.exception.DatabaseException;
import com.resumebuilder.backend.exception.ResumeNotFoundException;
import com.resumebuilder.backend.model.CustomParameter;
import com.resumebuilder.backend.model.Education;
import com.resumebuilder.backend.model.Experience;
import com.resumebuilder.backend.model.Project;
import com.resumebuilder.backend.model.Resume;
import com.resumebuilder.backend.model.Skill;
import com.resumebuilder.backend.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResumeDAO {

    

    public void saveResume(Resume resume) throws DatabaseException, ResumeNotFoundException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            ensureCustomSectionTables(conn);
            conn.setAutoCommit(false);

            if (resume.getId() == 0) {
                insertResume(conn, resume);
            } else {
                if (!resumeExists(conn, resume.getId())) {
                    throw new ResumeNotFoundException("Resume not found for update.");
                }
                updateResume(conn, resume);
                deleteChildRecords(conn, resume.getId());
            }

            insertEducation(conn, resume);
            insertExperience(conn, resume);
            insertProjects(conn, resume);
            insertSkills(conn, resume);
            insertCustomSections(conn, resume);

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new DatabaseException("Failed to save resume data.", e);
        } finally {
            resetAutoCommitQuietly(conn);
            closeQuietly(conn);
        }
    }

    public List<Resume> getResumesByUserId(int userId) {
        try {
            return getResumesByUserIdOrThrow(userId);
        } catch (DatabaseException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Resume> getResumesByUserIdOrThrow(int userId) throws DatabaseException {
        List<Resume> resumes = new ArrayList<>();
        String sql = "SELECT * FROM resumes WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Resume resume = new Resume();
                    populateResumeBasicInfo(resume, rs);
                    resumes.add(resume);
                }
            }
            return resumes;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch resumes for the user.", e);
        }
    }

    public Resume getResumeById(int resumeId) {
        try {
            return getResumeByIdOrThrow(resumeId);
        } catch (DatabaseException | ResumeNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Resume getResumeByIdOrThrow(int resumeId) throws DatabaseException, ResumeNotFoundException {
        String sql = "SELECT * FROM resumes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ensureCustomSectionTables(conn);

            stmt.setInt(1, resumeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new ResumeNotFoundException("Resume not found.");
                }

                Resume resume = new Resume();
                populateResumeBasicInfo(resume, rs);
                resume.setEducationList(getEducation(conn, resumeId));
                resume.setExperienceList(getExperience(conn, resumeId));
                resume.setProjectList(getProjects(conn, resumeId));
                resume.setSkillList(getSkills(conn, resumeId));
                resume.setCustomSections(getCustomSections(conn, resumeId));
                return resume;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch resume details.", e);
        }
    }

    public boolean deleteResume(int resumeId) {
        try {
            return deleteResumeOrThrow(resumeId);
        } catch (DatabaseException | ResumeNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteResumeOrThrow(int resumeId) throws DatabaseException, ResumeNotFoundException {
        String sql = "DELETE FROM resumes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (!resumeExists(conn, resumeId)) {
                throw new ResumeNotFoundException("Resume not found for deletion.");
            }

            stmt.setInt(1, resumeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete resume.", e);
        }
    }

    public void insertCustomSection(AbstractCustomSection section) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureCustomSectionTables(conn);
            insertCustomSection(conn, section);
            insertCustomParameters(conn, section.getParameters(), section.getId());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert custom section.", e);
        }
    }

    public void insertCustomParameters(List<CustomParameter> parameterList) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureCustomSectionTables(conn);
            insertCustomParameters(conn, parameterList, 0);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert custom parameters.", e);
        }
    }

    public List<AbstractCustomSection> getCustomSections(int resumeId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureCustomSectionTables(conn);
            return getCustomSections(conn, resumeId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load custom sections.", e);
        }
    }

    public List<CustomParameter> getCustomParameters(int sectionId) throws DatabaseException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureCustomSectionTables(conn);
            return getCustomParameters(conn, sectionId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load custom parameters.", e);
        }
    }

    private void insertResume(Connection conn, Resume resume) throws SQLException {
        String sql = "INSERT INTO resumes (user_id, title, template_name, color_theme, " +
                     "full_name, email, phone, address, summary, photo_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, resume.getUserId());
            stmt.setString(2, resume.getTitle());
            stmt.setString(3, resume.getTemplateName());
            stmt.setString(4, resume.getColorTheme());
            stmt.setString(5, resume.getFullName());
            stmt.setString(6, resume.getEmail());
            stmt.setString(7, resume.getPhone());
            stmt.setString(8, resume.getAddress());
            stmt.setString(9, resume.getSummary());
            stmt.setString(10, resume.getPhotoPath());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    resume.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    private void updateResume(Connection conn, Resume resume) throws SQLException {
        String sql = "UPDATE resumes SET title=?, template_name=?, color_theme=?, " +
                     "full_name=?, email=?, phone=?, address=?, summary=?, photo_path=? " +
                     "WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, resume.getTitle());
            stmt.setString(2, resume.getTemplateName());
            stmt.setString(3, resume.getColorTheme());
            stmt.setString(4, resume.getFullName());
            stmt.setString(5, resume.getEmail());
            stmt.setString(6, resume.getPhone());
            stmt.setString(7, resume.getAddress());
            stmt.setString(8, resume.getSummary());
            stmt.setString(9, resume.getPhotoPath());
            stmt.setInt(10, resume.getId());
            stmt.executeUpdate();
        }
    }

    private boolean resumeExists(Connection conn, int resumeId) throws SQLException {
        String sql = "SELECT id FROM resumes WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resumeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void deleteChildRecords(Connection conn, int resumeId) throws SQLException {
        String[] statements = {
            "DELETE FROM education WHERE resume_id = ?",
            "DELETE FROM experience WHERE resume_id = ?",
            "DELETE FROM projects WHERE resume_id = ?",
            "DELETE FROM skills WHERE resume_id = ?",
            "DELETE FROM custom_section WHERE resume_id = ?"
        };

        for (String sql : statements) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, resumeId);
                stmt.executeUpdate();
            }
        }
    }

    private void insertEducation(Connection conn, Resume resume) throws SQLException {
        if (resume.getEducationList().isEmpty()) return;

        String sql = "INSERT INTO education (resume_id, degree, university, year, cgpa) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Education education : resume.getEducationList()) {
                stmt.setInt(1, resume.getId());
                stmt.setString(2, education.getDegree());
                stmt.setString(3, education.getUniversity());
                stmt.setString(4, education.getYear());
                stmt.setString(5, education.getCgpa());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void insertExperience(Connection conn, Resume resume) throws SQLException {
        if (resume.getExperienceList().isEmpty()) return;

        String sql = "INSERT INTO experience (resume_id, company, role, duration, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Experience experience : resume.getExperienceList()) {
                stmt.setInt(1, resume.getId());
                stmt.setString(2, experience.getCompany());
                stmt.setString(3, experience.getRole());
                stmt.setString(4, experience.getDuration());
                stmt.setString(5, experience.getDescription());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void insertProjects(Connection conn, Resume resume) throws SQLException {
        if (resume.getProjectList().isEmpty()) return;

        String sql = "INSERT INTO projects (resume_id, title, description) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Project project : resume.getProjectList()) {
                stmt.setInt(1, resume.getId());
                stmt.setString(2, project.getTitle());
                stmt.setString(3, project.getDescription());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void insertSkills(Connection conn, Resume resume) throws SQLException {
        if (resume.getSkillList().isEmpty()) return;

        String sql = "INSERT INTO skills (resume_id, skill_name) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Skill skill : resume.getSkillList()) {
                stmt.setInt(1, resume.getId());
                stmt.setString(2, skill.getSkillName());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void insertCustomSections(Connection conn, Resume resume) throws SQLException {
        List<AbstractCustomSection> sections = resume.getCustomSections();
        if (sections.isEmpty()) return;

        for (AbstractCustomSection section : sections) {
            section.setResumeId(resume.getId());
            insertCustomSection(conn, section);
            insertCustomParameters(conn, section.getParameters(), section.getId());
        }
    }

    private void insertCustomSection(Connection conn, AbstractCustomSection section) throws SQLException {
        String sql = "INSERT INTO custom_section (resume_id, header, occupation) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, section.getResumeId());
            stmt.setString(2, section.getHeader());
            stmt.setString(3, section.getOccupationName());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    section.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    private void insertCustomParameters(Connection conn, List<CustomParameter> parameterList, int fallbackSectionId) throws SQLException {
        if (parameterList == null || parameterList.isEmpty()) return;

        String sql = "INSERT INTO custom_parameter (section_id, name, value) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (CustomParameter parameter : parameterList) {
                int sectionId = fallbackSectionId != 0 ? fallbackSectionId : parameter.getSectionId();
                if (sectionId == 0) {
                    throw new SQLException("Custom parameter is missing a valid section id.");
                }
                parameter.setSectionId(sectionId);
                stmt.setInt(1, sectionId);
                stmt.setString(2, parameter.getName());
                stmt.setString(3, parameter.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void populateResumeBasicInfo(Resume resume, ResultSet rs) throws SQLException {
        resume.setId(rs.getInt("id"));
        resume.setUserId(rs.getInt("user_id"));
        resume.setTitle(rs.getString("title"));
        resume.setTemplateName(rs.getString("template_name"));
        resume.setColorTheme(rs.getString("color_theme"));
        resume.setFullName(rs.getString("full_name"));
        resume.setEmail(rs.getString("email"));
        resume.setPhone(rs.getString("phone"));
        resume.setAddress(rs.getString("address"));
        resume.setSummary(rs.getString("summary"));
        resume.setPhotoPath(rs.getString("photo_path"));
    }

    private List<Education> getEducation(Connection conn, int resumeId) throws SQLException {
        List<Education> list = new ArrayList<>();
        String sql = "SELECT * FROM education WHERE resume_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resumeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Education education = new Education(rs.getString("degree"), rs.getString("university"),
                            rs.getString("year"), rs.getString("cgpa"));
                    education.setId(rs.getInt("id"));
                    education.setResumeId(resumeId);
                    list.add(education);
                }
            }
        }
        return list;
    }

    private List<Experience> getExperience(Connection conn, int resumeId) throws SQLException {
        List<Experience> list = new ArrayList<>();
        String sql = "SELECT * FROM experience WHERE resume_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resumeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Experience experience = new Experience(rs.getString("company"), rs.getString("role"),
                            rs.getString("duration"), rs.getString("description"));
                    experience.setId(rs.getInt("id"));
                    experience.setResumeId(resumeId);
                    list.add(experience);
                }
            }
        }
        return list;
    }

    private List<Project> getProjects(Connection conn, int resumeId) throws SQLException {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE resume_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resumeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Project project = new Project(rs.getString("title"), rs.getString("description"));
                    project.setId(rs.getInt("id"));
                    project.setResumeId(resumeId);
                    list.add(project);
                }
            }
        }
        return list;
    }

    private List<Skill> getSkills(Connection conn, int resumeId) throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT * FROM skills WHERE resume_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resumeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Skill skill = new Skill(rs.getString("skill_name"));
                    skill.setId(rs.getInt("id"));
                    skill.setResumeId(resumeId);
                    list.add(skill);
                }
            }
        }
        return list;
    }

    private List<AbstractCustomSection> getCustomSections(Connection conn, int resumeId) throws SQLException {
        List<AbstractCustomSection> list = new ArrayList<>();
        String sql = "SELECT * FROM custom_section WHERE resume_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resumeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String header = rs.getString("header");
                    String occupation = rs.getString("occupation");
                    AbstractCustomSection section = createSection(occupation, header);
                    section.setId(rs.getInt("id"));
                    section.setResumeId(resumeId);
                    section.setParameters(getCustomParameters(conn, section.getId()));
                    list.add(section);
                }
            }
        }
        return list;
    }

    private List<CustomParameter> getCustomParameters(Connection conn, int sectionId) throws SQLException {
        List<CustomParameter> list = new ArrayList<>();
        String sql = "SELECT * FROM custom_parameter WHERE section_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CustomParameter parameter = new CustomParameter(
                            rs.getInt("id"),
                            rs.getInt("section_id"),
                            rs.getString("name"),
                            rs.getString("value")
                    );
                    list.add(parameter);
                }
            }
        }
        return list;
    }

    private AbstractCustomSection createSection(String occupationName, String header) {
        String[] fieldNames = PresetCustomSection.getFieldNamesForOccupation(occupationName);
        if (fieldNames != null) {
            return new PresetCustomSection(header, occupationName, fieldNames);
        }
        return new FlexibleCustomSection(header, occupationName);
    }

    private void ensureCustomSectionTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS custom_section (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "resume_id INT NOT NULL, " +
                    "header VARCHAR(150) NOT NULL, " +
                    "occupation VARCHAR(100) NOT NULL, " +
                    "FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS custom_parameter (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "section_id INT NOT NULL, " +
                    "name VARCHAR(150) NOT NULL, " +
                    "value TEXT NOT NULL, " +
                    "FOREIGN KEY (section_id) REFERENCES custom_section(id) ON DELETE CASCADE" +
                    ")"
            );
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn == null) return;

        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void resetAutoCommitQuietly(Connection conn) {
        if (conn == null) return;

        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn == null) return;

        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }
}
