package com.resumebuilder.gui;

import com.resumebuilder.backend.customsection.AbstractCustomSection;
import com.resumebuilder.backend.customsection.FlexibleCustomSection;
import com.resumebuilder.backend.customsection.PresetCustomSection;
import com.resumebuilder.backend.dao.ResumeDAO;
import com.resumebuilder.backend.dao.UserDAO;
import com.resumebuilder.backend.exception.DatabaseException;
import com.resumebuilder.backend.exception.ResumeNotFoundException;
import com.resumebuilder.backend.model.CustomParameter;
import com.resumebuilder.backend.model.Education;
import com.resumebuilder.backend.model.Experience;
import com.resumebuilder.backend.model.Project;
import com.resumebuilder.backend.model.Resume;
import com.resumebuilder.backend.model.Skill;
import com.resumebuilder.backend.model.User;
import com.resumebuilder.pdf.PdfGenerator;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ResumeBuilderApp extends Application {
    private final UserDAO userDAO = new UserDAO();
    private final ResumeDAO resumeDAO = new ResumeDAO();

    private Stage primaryStage;
    private User currentUser;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Resume Builder");
        this.primaryStage.setMinWidth(540);
        this.primaryStage.setMinHeight(420);
        showAuthScene();
        this.primaryStage.show();
    }

    private void showAuthScene() {
        TabPane authTabs = new TabPane();
        authTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab loginTab = new Tab("Login");
        Tab registerTab = new Tab("Register");

        TextField loginUsernameField = new TextField();
        PasswordField loginPasswordField = new PasswordField();
        loginTab.setContent(buildLoginPane(authTabs, registerTab, loginUsernameField, loginPasswordField));
        registerTab.setContent(buildRegisterPane(authTabs, loginTab, loginUsernameField));

        authTabs.getTabs().setAll(loginTab, registerTab);

        Label titleLabel = new Label("Resume Builder");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        VBox header = new VBox(4, titleLabel);
        BorderPane root = new BorderPane(authTabs);
        root.setPadding(new Insets(20));
        root.setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 16, 0));

        primaryStage.setScene(new Scene(root, 560, 430));
    }

    private Node buildLoginPane(TabPane authTabs, Tab registerTab, TextField loginUsernameField, PasswordField loginPasswordField) {
        GridPane form = createFormGrid();

        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> loginUsernameField.getText().trim().isEmpty() || loginPasswordField.getText().isEmpty(),
                        loginUsernameField.textProperty(),
                        loginPasswordField.textProperty()
                )
        );

        Button switchToRegisterButton = new Button("Create Account");
        switchToRegisterButton.setOnAction(event -> authTabs.getSelectionModel().select(registerTab));

        loginButton.setOnAction(event -> {
            String username = loginUsernameField.getText().trim();
            String password = loginPasswordField.getText();

            User user = userDAO.login(username, password);
            if (user == null) {
                showError(primaryStage, "Login Failed", "Invalid credentials or database connection issue.");
                return;
            }

            currentUser = user;
            loginPasswordField.clear();
            showDashboardScene();
        });

        form.add(new Label("Username"), 0, 0);
        form.add(loginUsernameField, 1, 0);
        form.add(new Label("Password"), 0, 1);
        form.add(loginPasswordField, 1, 1);

        HBox actions = new HBox(10, loginButton, switchToRegisterButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(16, form, actions);
        content.setPadding(new Insets(8, 0, 0, 0));
        return content;
    }

    private Node buildRegisterPane(TabPane authTabs, Tab loginTab, TextField loginUsernameField) {
        GridPane form = createFormGrid();

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();

        Button registerButton = new Button("Register");
        registerButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> usernameField.getText().trim().isEmpty()
                                || passwordField.getText().isEmpty()
                                || confirmPasswordField.getText().isEmpty(),
                        usernameField.textProperty(),
                        passwordField.textProperty(),
                        confirmPasswordField.textProperty()
                )
        );

        registerButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!password.equals(confirmPassword)) {
                showError(primaryStage, "Register Failed", "Passwords do not match.");
                return;
            }

            boolean success = userDAO.register(new User(username, password));
            if (!success) {
                showError(primaryStage, "Register Failed", "Registration failed. Username may already exist.");
                return;
            }

            showInfo(primaryStage, "Registration Complete", "Account created successfully. You can log in now.");
            loginUsernameField.setText(username);
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            authTabs.getSelectionModel().select(loginTab);
        });

        form.add(new Label("Username"), 0, 0);
        form.add(usernameField, 1, 0);
        form.add(new Label("Password"), 0, 1);
        form.add(passwordField, 1, 1);
        form.add(new Label("Confirm Password"), 0, 2);
        form.add(confirmPasswordField, 1, 2);

        VBox content = new VBox(16, form, registerButton);
        content.setPadding(new Insets(8, 0, 0, 0));
        return content;
    }

    private void showDashboardScene() {
        ObservableList<Resume> resumeItems = FXCollections.observableArrayList();
        TableView<Resume> resumeTable = createResumeTable(resumeItems);

        Runnable refreshDashboard = () -> {
            try {
                resumeItems.setAll(resumeDAO.getResumesByUserIdOrThrow(currentUser.getId()));
            } catch (DatabaseException e) {
                resumeItems.clear();
                showError(primaryStage, "Load Failed", "Could not load resumes from the database.");
            }
        };

        Button createButton = new Button("Create");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
        Button exportButton = new Button("Export PDF");
        Button refreshButton = new Button("Refresh");
        Button logoutButton = new Button("Logout");

        createButton.setOnAction(event -> {
            Resume newResume = new Resume();
            newResume.setUserId(currentUser.getId());
            openResumeEditor(newResume, refreshDashboard);
        });

        editButton.disableProperty().bind(resumeTable.getSelectionModel().selectedItemProperty().isNull());
        editButton.setOnAction(event -> {
            Resume selectedResume = resumeTable.getSelectionModel().getSelectedItem();
            if (selectedResume == null) {
                return;
            }

            try {
                Resume fullResume = resumeDAO.getResumeByIdOrThrow(selectedResume.getId());
                openResumeEditor(fullResume, refreshDashboard);
            } catch (ResumeNotFoundException e) {
                showError(primaryStage, "Resume Missing", "The selected resume no longer exists.");
                refreshDashboard.run();
            } catch (DatabaseException e) {
                showError(primaryStage, "Load Failed", "Could not open the selected resume.");
            }
        });

        deleteButton.disableProperty().bind(resumeTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.setOnAction(event -> {
            Resume selectedResume = resumeTable.getSelectionModel().getSelectedItem();
            if (selectedResume == null) {
                return;
            }

            boolean confirmed = confirm(
                    primaryStage,
                    "Delete Resume",
                    "Delete \"" + selectedResume.getTitle() + "\"?"
            );

            if (!confirmed) {
                return;
            }

            try {
                resumeDAO.deleteResumeOrThrow(selectedResume.getId());
                refreshDashboard.run();
            } catch (ResumeNotFoundException e) {
                showError(primaryStage, "Resume Missing", "The selected resume no longer exists.");
                refreshDashboard.run();
            } catch (DatabaseException e) {
                showError(primaryStage, "Delete Failed", "Could not delete the selected resume.");
            }
        });

        exportButton.disableProperty().bind(resumeTable.getSelectionModel().selectedItemProperty().isNull());
        exportButton.setOnAction(event -> exportSelectedResume(resumeTable.getSelectionModel().getSelectedItem()));

        refreshButton.setOnAction(event -> refreshDashboard.run());
        logoutButton.setOnAction(event -> {
            currentUser = null;
            showAuthScene();
        });

        Label titleLabel = new Label("Welcome, " + currentUser.getUsername());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label subtitleLabel = new Label("Create, edit, delete, and export resumes.");

        HBox buttonBar = new HBox(10, createButton, editButton, deleteButton, exportButton, refreshButton, logoutButton);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        VBox center = new VBox(12, resumeTable, buttonBar);
        VBox.setVgrow(resumeTable, Priority.ALWAYS);

        BorderPane root = new BorderPane(center);
        root.setPadding(new Insets(20));
        root.setTop(new VBox(4, titleLabel, subtitleLabel));
        BorderPane.setMargin(center, new Insets(16, 0, 0, 0));

        refreshDashboard.run();
        primaryStage.setScene(new Scene(root, 920, 620));
    }

    private TableView<Resume> createResumeTable(ObservableList<Resume> resumeItems) {
        TableView<Resume> table = new TableView<>(resumeItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No resumes found."));

        TableColumn<Resume, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(safe(cellData.getValue().getTitle())));

        TableColumn<Resume, String> templateColumn = new TableColumn<>("Template");
        templateColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(safe(cellData.getValue().getTemplateName())));

        TableColumn<Resume, String> themeColumn = new TableColumn<>("Theme");
        themeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(safe(cellData.getValue().getColorTheme())));

        table.getColumns().add(titleColumn);
        table.getColumns().add(templateColumn);
        table.getColumns().add(themeColumn);
        return table;
    }

    private void openResumeEditor(Resume resume, Runnable onSaved) {
        Stage editorStage = new Stage();
        editorStage.initOwner(primaryStage);
        editorStage.initModality(Modality.APPLICATION_MODAL);
        editorStage.setTitle(resume.getId() == 0 ? "Create Resume" : "Edit Resume");
        editorStage.setMinWidth(960);
        editorStage.setMinHeight(720);

        ResumeEditorPane editorPane = new ResumeEditorPane(resume, resumeDAO, editorStage, onSaved);
        editorStage.setScene(new Scene(editorPane, 980, 740));
        editorStage.showAndWait();
    }

    private void exportSelectedResume(Resume selectedResume) {
        if (selectedResume == null) {
            return;
        }

        try {
            Resume fullResume = resumeDAO.getResumeByIdOrThrow(selectedResume.getId());
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Resume as PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName(sanitizeFileName(fullResume.getTitle()) + ".pdf");

            File file = fileChooser.showSaveDialog(primaryStage);
            if (file == null) {
                return;
            }

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                PdfGenerator.generate(fullResume, outputStream);
            }

            showInfo(primaryStage, "Export Complete", "PDF saved to:\n" + file.getAbsolutePath());
        } catch (ResumeNotFoundException e) {
            showError(primaryStage, "Resume Missing", "The selected resume no longer exists.");
        } catch (DatabaseException e) {
            showError(primaryStage, "Load Failed", "Could not load the selected resume for export.");
        } catch (Exception e) {
            showError(primaryStage, "Export Failed", "Could not generate the PDF file.");
        }
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        return grid;
    }

    private static void showInfo(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showError(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static boolean confirm(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
        return ButtonType.OK.equals(alert.getResult());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String blankToNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String sanitizeFileName(String value) {
        String fallback = value == null || value.trim().isEmpty() ? "resume" : value.trim();
        return fallback.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static final class ResumeEditorPane extends BorderPane {
        private final Resume resume;
        private final ResumeDAO resumeDAO;
        private final Stage stage;
        private final Runnable onSaved;

        private final TextField titleField = new TextField();
        private final TextField fullNameField = new TextField();
        private final TextField emailField = new TextField();
        private final TextField phoneField = new TextField();
        private final TextField addressField = new TextField();
        private final ComboBox<String> templateBox = new ComboBox<>();
        private final ComboBox<String> themeBox = new ComboBox<>();
        private final TextArea summaryArea = new TextArea();

        private final ObservableList<Education> educationItems;
        private final ObservableList<Experience> experienceItems;
        private final ObservableList<Project> projectItems;
        private final ObservableList<Skill> skillItems;
        private final ObservableList<AbstractCustomSection> customSectionItems;

        private ResumeEditorPane(Resume resume, ResumeDAO resumeDAO, Stage stage, Runnable onSaved) {
            this.resume = resume;
            this.resumeDAO = resumeDAO;
            this.stage = stage;
            this.onSaved = onSaved;

            this.educationItems = FXCollections.observableArrayList(copyEducationList(resume.getEducationList()));
            this.experienceItems = FXCollections.observableArrayList(copyExperienceList(resume.getExperienceList()));
            this.projectItems = FXCollections.observableArrayList(copyProjectList(resume.getProjectList()));
            this.skillItems = FXCollections.observableArrayList(copySkillList(resume.getSkillList()));
            this.customSectionItems = FXCollections.observableArrayList(copyCustomSectionList(resume.getCustomSections()));

            setPadding(new Insets(16));
            setTop(buildHeader());
            setCenter(buildTabs());
            setBottom(buildActions());
        }

        private Node buildHeader() {
            Label titleLabel = new Label(resume.getId() == 0 ? "Create Resume" : "Edit Resume");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
            Label subtitleLabel = new Label("This screen keeps the existing resume logic and saves through the same DAO.");

            VBox header = new VBox(4, titleLabel, subtitleLabel);
            BorderPane.setMargin(header, new Insets(0, 0, 16, 0));
            return header;
        }

        private Node buildTabs() {
            titleField.setText(safe(resume.getTitle()));
            fullNameField.setText(safe(resume.getFullName()));
            emailField.setText(safe(resume.getEmail()));
            phoneField.setText(safe(resume.getPhone()));
            addressField.setText(safe(resume.getAddress()));

            templateBox.setItems(FXCollections.observableArrayList("Classic", "Modern", "Creative", "Minimal"));
            templateBox.setValue(safe(resume.getTemplateName()).isEmpty() ? "Classic" : resume.getTemplateName());

            themeBox.setItems(FXCollections.observableArrayList("Blue", "Green", "Grey"));
            themeBox.setValue(safe(resume.getColorTheme()).isEmpty() ? "Blue" : resume.getColorTheme());

            summaryArea.setText(safe(resume.getSummary()));
            summaryArea.setPromptText("Professional summary");
            summaryArea.setPrefRowCount(8);
            summaryArea.setWrapText(true);

            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            tabPane.getTabs().add(createTab("General", buildGeneralTab()));
            tabPane.getTabs().add(createTab("Education", buildEducationTab()));
            tabPane.getTabs().add(createTab("Experience", buildExperienceTab()));
            tabPane.getTabs().add(createTab("Projects", buildProjectsTab()));
            tabPane.getTabs().add(createTab("Skills", buildSkillsTab()));
            tabPane.getTabs().add(createTab("Custom Sections", buildCustomSectionsTab()));
            VBox.setVgrow(tabPane, Priority.ALWAYS);
            return tabPane;
        }

        private Tab createTab(String title, Node content) {
            Tab tab = new Tab(title);
            tab.setContent(content);
            return tab;
        }

        private Node buildGeneralTab() {
            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(12);

            grid.add(new Label("Title"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Full Name"), 0, 1);
            grid.add(fullNameField, 1, 1);
            grid.add(new Label("Email"), 0, 2);
            grid.add(emailField, 1, 2);
            grid.add(new Label("Phone"), 0, 3);
            grid.add(phoneField, 1, 3);
            grid.add(new Label("Address"), 0, 4);
            grid.add(addressField, 1, 4);
            grid.add(new Label("Template"), 0, 5);
            grid.add(templateBox, 1, 5);
            grid.add(new Label("Color Theme"), 0, 6);
            grid.add(themeBox, 1, 6);
            grid.add(new Label("Summary"), 0, 7);
            grid.add(summaryArea, 1, 7);

            summaryArea.setPrefHeight(180);
            GridPane.setHgrow(titleField, Priority.ALWAYS);
            GridPane.setHgrow(fullNameField, Priority.ALWAYS);
            GridPane.setHgrow(emailField, Priority.ALWAYS);
            GridPane.setHgrow(phoneField, Priority.ALWAYS);
            GridPane.setHgrow(addressField, Priority.ALWAYS);
            GridPane.setHgrow(templateBox, Priority.ALWAYS);
            GridPane.setHgrow(themeBox, Priority.ALWAYS);
            GridPane.setHgrow(summaryArea, Priority.ALWAYS);
            GridPane.setVgrow(summaryArea, Priority.ALWAYS);

            VBox content = new VBox(grid);
            content.setPadding(new Insets(12));
            VBox.setVgrow(grid, Priority.ALWAYS);
            return content;
        }

        private Node buildEducationTab() {
            TableView<Education> table = new TableView<>(educationItems);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setPlaceholder(new Label("No education entries."));
            table.getColumns().add(createTextColumn("Degree", Education::getDegree));
            table.getColumns().add(createTextColumn("University", Education::getUniversity));
            table.getColumns().add(createTextColumn("Year", Education::getYear));
            table.getColumns().add(createTextColumn("CGPA", Education::getCgpa));

            Button addButton = new Button("Add");
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");

            addButton.setOnAction(event -> {
                Education education = showEducationDialog(null);
                if (education != null) {
                    educationItems.add(education);
                }
            });
            editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            editButton.setOnAction(event -> {
                Education selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    return;
                }

                Education updated = showEducationDialog(selected);
                if (updated != null) {
                    int index = table.getSelectionModel().getSelectedIndex();
                    educationItems.set(index, updated);
                }
            });

            deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.setOnAction(event -> {
                Education selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    educationItems.remove(selected);
                }
            });

            return buildTableSection(table, addButton, editButton, deleteButton);
        }

        private Node buildExperienceTab() {
            TableView<Experience> table = new TableView<>(experienceItems);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setPlaceholder(new Label("No experience entries."));
            table.getColumns().add(createTextColumn("Role", Experience::getRole));
            table.getColumns().add(createTextColumn("Company", Experience::getCompany));
            table.getColumns().add(createTextColumn("Duration", Experience::getDuration));
            table.getColumns().add(createTextColumn("Description", Experience::getDescription));

            Button addButton = new Button("Add");
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");

            addButton.setOnAction(event -> {
                Experience experience = showExperienceDialog(null);
                if (experience != null) {
                    experienceItems.add(experience);
                }
            });
            editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            editButton.setOnAction(event -> {
                Experience selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    return;
                }

                Experience updated = showExperienceDialog(selected);
                if (updated != null) {
                    int index = table.getSelectionModel().getSelectedIndex();
                    experienceItems.set(index, updated);
                }
            });

            deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.setOnAction(event -> {
                Experience selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    experienceItems.remove(selected);
                }
            });

            return buildTableSection(table, addButton, editButton, deleteButton);
        }

        private Node buildProjectsTab() {
            TableView<Project> table = new TableView<>(projectItems);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setPlaceholder(new Label("No project entries."));
            table.getColumns().add(createTextColumn("Title", Project::getTitle));
            table.getColumns().add(createTextColumn("Description", Project::getDescription));

            Button addButton = new Button("Add");
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");

            addButton.setOnAction(event -> {
                Project project = showProjectDialog(null);
                if (project != null) {
                    projectItems.add(project);
                }
            });
            editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            editButton.setOnAction(event -> {
                Project selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    return;
                }

                Project updated = showProjectDialog(selected);
                if (updated != null) {
                    int index = table.getSelectionModel().getSelectedIndex();
                    projectItems.set(index, updated);
                }
            });

            deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.setOnAction(event -> {
                Project selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    projectItems.remove(selected);
                }
            });

            return buildTableSection(table, addButton, editButton, deleteButton);
        }

        private Node buildSkillsTab() {
            TableView<Skill> table = new TableView<>(skillItems);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setPlaceholder(new Label("No skills yet."));
            table.getColumns().add(createTextColumn("Skill", Skill::getSkillName));

            Button addButton = new Button("Add");
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");

            addButton.setOnAction(event -> {
                Skill skill = showSkillDialog(null);
                if (skill != null) {
                    skillItems.add(skill);
                }
            });
            editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            editButton.setOnAction(event -> {
                Skill selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    return;
                }

                Skill updated = showSkillDialog(selected);
                if (updated != null) {
                    int index = table.getSelectionModel().getSelectedIndex();
                    skillItems.set(index, updated);
                }
            });

            deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.setOnAction(event -> {
                Skill selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    skillItems.remove(selected);
                }
            });

            return buildTableSection(table, addButton, editButton, deleteButton);
        }

        private Node buildCustomSectionsTab() {
            TableView<AbstractCustomSection> table = new TableView<>(customSectionItems);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setPlaceholder(new Label("No custom sections yet."));
            table.getColumns().add(createTextColumn("Header", AbstractCustomSection::getHeader));
            table.getColumns().add(createTextColumn("Occupation", AbstractCustomSection::getOccupationName));
            table.getColumns().add(createTextColumn("Parameters", section -> String.valueOf(section.getParameters().size())));

            Button addButton = new Button("Add");
            Button editButton = new Button("Edit");
            Button viewButton = new Button("View");
            Button deleteButton = new Button("Delete");

            addButton.setOnAction(event -> {
                AbstractCustomSection section = showCustomSectionDialog(null);
                if (section != null) {
                    customSectionItems.add(section);
                }
            });
            editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            editButton.setOnAction(event -> {
                AbstractCustomSection selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    return;
                }

                AbstractCustomSection updated = showCustomSectionDialog(selected);
                if (updated != null) {
                    int index = table.getSelectionModel().getSelectedIndex();
                    customSectionItems.set(index, updated);
                }
            });

            viewButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            viewButton.setOnAction(event -> {
                AbstractCustomSection selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    return;
                }
                showInfo(stage, "Custom Section", formatCustomSection(selected));
            });

            deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.setOnAction(event -> {
                AbstractCustomSection selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    customSectionItems.remove(selected);
                }
            });

            return buildTableSection(table, addButton, editButton, viewButton, deleteButton);
        }

        private Node buildTableSection(TableView<?> table, Button... buttons) {
            HBox buttonBar = new HBox(10, buttons);
            VBox content = new VBox(12, table, buttonBar);
            content.setPadding(new Insets(12));
            VBox.setVgrow(table, Priority.ALWAYS);
            return content;
        }

        private Node buildActions() {
            Button saveButton = new Button("Save");
            saveButton.setDefaultButton(true);
            Button cancelButton = new Button("Cancel");

            saveButton.setOnAction(event -> saveResume());
            cancelButton.setOnAction(event -> {
                if (confirm(stage, "Discard Changes", "Close this editor without saving?")) {
                    stage.close();
                }
            });

            HBox actionBar = new HBox(10, saveButton, cancelButton);
            actionBar.setAlignment(Pos.CENTER_RIGHT);
            actionBar.setPadding(new Insets(16, 0, 0, 0));
            return actionBar;
        }

        private void saveResume() {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                showError(stage, "Missing Title", "Resume title is required.");
                return;
            }

            resume.setTitle(title);
            resume.setFullName(blankToNull(fullNameField.getText()));
            resume.setEmail(blankToNull(emailField.getText()));
            resume.setPhone(blankToNull(phoneField.getText()));
            resume.setAddress(blankToNull(addressField.getText()));
            resume.setSummary(blankToNull(summaryArea.getText()));
            resume.setTemplateName(templateBox.getValue());
            resume.setColorTheme(themeBox.getValue());
            resume.setEducationList(new ArrayList<>(educationItems));
            resume.setExperienceList(new ArrayList<>(experienceItems));
            resume.setProjectList(new ArrayList<>(projectItems));
            resume.setSkillList(new ArrayList<>(skillItems));
            resume.setCustomSections(new ArrayList<>(customSectionItems));

            try {
                resumeDAO.saveResume(resume);
                if (onSaved != null) {
                    onSaved.run();
                }
                stage.close();
            } catch (ResumeNotFoundException e) {
                showError(stage, "Save Failed", "The resume could not be found for update.");
            } catch (DatabaseException e) {
                showError(stage, "Save Failed", "Could not save the resume to the database.");
            }
        }

        private Education showEducationDialog(Education existing) {
            Dialog<Education> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.setTitle(existing == null ? "Add Education" : "Edit Education");

            ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            TextField degreeField = new TextField(existing == null ? "" : safe(existing.getDegree()));
            TextField universityField = new TextField(existing == null ? "" : safe(existing.getUniversity()));
            TextField yearField = new TextField(existing == null ? "" : safe(existing.getYear()));
            TextField cgpaField = new TextField(existing == null ? "" : safe(existing.getCgpa()));

            GridPane grid = createDialogGrid();
            grid.add(new Label("Degree"), 0, 0);
            grid.add(degreeField, 1, 0);
            grid.add(new Label("University"), 0, 1);
            grid.add(universityField, 1, 1);
            grid.add(new Label("Year"), 0, 2);
            grid.add(yearField, 1, 2);
            grid.add(new Label("CGPA"), 0, 3);
            grid.add(cgpaField, 1, 3);

            Node saveButton = dialog.getDialogPane().lookupButton(saveType);
            saveButton.disableProperty().bind(
                    Bindings.createBooleanBinding(
                            () -> degreeField.getText().trim().isEmpty() || universityField.getText().trim().isEmpty(),
                            degreeField.textProperty(),
                            universityField.textProperty()
                    )
            );

            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(button -> {
                if (button != saveType) {
                    return null;
                }

                Education updated = copyEducation(existing);
                updated.setDegree(degreeField.getText().trim());
                updated.setUniversity(universityField.getText().trim());
                updated.setYear(blankToNull(yearField.getText()));
                updated.setCgpa(blankToNull(cgpaField.getText()));
                return updated;
            });

            dialog.showAndWait();
            return dialog.getResult();
        }

        private Experience showExperienceDialog(Experience existing) {
            Dialog<Experience> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.setTitle(existing == null ? "Add Experience" : "Edit Experience");


            ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            TextField roleField = new TextField(existing == null ? "" : safe(existing.getRole()));
            TextField companyField = new TextField(existing == null ? "" : safe(existing.getCompany()));
            TextField durationField = new TextField(existing == null ? "" : safe(existing.getDuration()));
            TextArea descriptionArea = new TextArea(existing == null ? "" : safe(existing.getDescription()));
            descriptionArea.setPrefRowCount(4);
            descriptionArea.setWrapText(true);

            GridPane grid = createDialogGrid();
            grid.add(new Label("Role"), 0, 0);
            grid.add(roleField, 1, 0);
            grid.add(new Label("Company"), 0, 1);
            grid.add(companyField, 1, 1);
            grid.add(new Label("Duration"), 0, 2);
            grid.add(durationField, 1, 2);
            grid.add(new Label("Description"), 0, 3);
            grid.add(descriptionArea, 1, 3);

            Node saveButton = dialog.getDialogPane().lookupButton(saveType);
            saveButton.disableProperty().bind(
                    Bindings.createBooleanBinding(
                            () -> roleField.getText().trim().isEmpty() || companyField.getText().trim().isEmpty(),
                            roleField.textProperty(),
                            companyField.textProperty()
                    )
            );
dialog.getDialogPane().setPrefWidth(700);
            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(button -> {
                if (button != saveType) {
                    return null;
                }

                Experience updated = copyExperience(existing);
                updated.setRole(roleField.getText().trim());
                updated.setCompany(companyField.getText().trim());
                updated.setDuration(blankToNull(durationField.getText()));
                updated.setDescription(blankToNull(descriptionArea.getText()));
                return updated;
            });

            dialog.showAndWait();
            return dialog.getResult();
        }

        private Project showProjectDialog(Project existing) {
            Dialog<Project> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.setTitle(existing == null ? "Add Project" : "Edit Project");

            ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            TextField titleField = new TextField(existing == null ? "" : safe(existing.getTitle()));
            TextArea descriptionArea = new TextArea(existing == null ? "" : safe(existing.getDescription()));
            descriptionArea.setPrefRowCount(4);
            descriptionArea.setWrapText(true);

            GridPane grid = createDialogGrid();
            grid.add(new Label("Title"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Description"), 0, 1);
            grid.add(descriptionArea, 1, 1);

            Node saveButton = dialog.getDialogPane().lookupButton(saveType);
            saveButton.disableProperty().bind(
                    Bindings.createBooleanBinding(
                            () -> titleField.getText().trim().isEmpty(),
                            titleField.textProperty()
                    )
            );

            dialog.getDialogPane().setContent(grid);
                        grid.setPrefWidth(700);

            dialog.setResultConverter(button -> {
                if (button != saveType) {
                    return null;
                }

                Project updated = copyProject(existing);
                updated.setTitle(titleField.getText().trim());
                updated.setDescription(blankToNull(descriptionArea.getText()));
                return updated;
            });

            dialog.showAndWait();
            return dialog.getResult();
        }

        private Skill showSkillDialog(Skill existing) {
            Dialog<Skill> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.setTitle(existing == null ? "Add Skill" : "Edit Skill");

            ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            TextField skillNameField = new TextField(existing == null ? "" : safe(existing.getSkillName()));

            GridPane grid = createDialogGrid();
            grid.add(new Label("Skill Name"), 0, 0);
            grid.add(skillNameField, 1, 0);

            Node saveButton = dialog.getDialogPane().lookupButton(saveType);
            saveButton.disableProperty().bind(
                    Bindings.createBooleanBinding(
                            () -> skillNameField.getText().trim().isEmpty(),
                            skillNameField.textProperty()
                    )
            );

            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(button -> {
                if (button != saveType) {
                    return null;
                }

                Skill updated = copySkill(existing);
                updated.setSkillName(skillNameField.getText().trim());
                return updated;
            });

            dialog.showAndWait();
            return dialog.getResult();
        }

        private AbstractCustomSection showCustomSectionDialog(AbstractCustomSection existing) {
            Dialog<AbstractCustomSection> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.setTitle(existing == null ? "Add Custom Section" : "Edit Custom Section");

            ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            TextField headerField = new TextField(existing == null ? "" : safe(existing.getHeader()));
            ComboBox<String> occupationTypeBox = new ComboBox<>();
            occupationTypeBox.setItems(FXCollections.observableArrayList(PresetCustomSection.getOccupationOptions()));
            occupationTypeBox.setMaxWidth(Double.MAX_VALUE);

            String existingOccupation = existing == null ? "" : safe(existing.getOccupationName()).trim();
            boolean hasPresetOccupation = PresetCustomSection.getFieldNamesForOccupation(existingOccupation) != null;
            occupationTypeBox.setValue(hasPresetOccupation ? existingOccupation : "Custom");

            Label customOccupationLabel = new Label("Custom Occupation");
            TextField customOccupationField = new TextField(hasPresetOccupation ? "" : existingOccupation);
            customOccupationField.setPromptText("Enter occupation name");

            Label parametersLabel = new Label("Parameters");
            Label parametersHintLabel = new Label();
            VBox parameterRowsBox = new VBox(8);
            Button addParameterButton = new Button("Add Parameter");

            class SectionFormState {
                private final List<ParameterRow> rows = new ArrayList<>();

                private Map<String, String> snapshotValues() {
                    Map<String, String> values = new LinkedHashMap<>();
                    for (ParameterRow row : rows) {
                        String name = row.getName().trim();
                        String value = row.getValue().trim();
                        if (name.isEmpty() && value.isEmpty()) {
                            continue;
                        }
                        values.put(name, value);
                    }
                    return values;
                }

                private void refreshRows() {
                    parameterRowsBox.getChildren().clear();
                    for (ParameterRow row : rows) {
                        parameterRowsBox.getChildren().add(row.getNode());
                    }
                }

                private void addCustomRow(String name, String value) {
                    ParameterRow row = new ParameterRow(name, value, true, true);
                    row.setOnRemove(event -> {
                        rows.remove(row);
                        refreshRows();
                    });
                    rows.add(row);
                    refreshRows();
                }

                private void configureRows(Map<String, String> existingValues) {
                    Map<String, String> values = existingValues == null ? new LinkedHashMap<>() : existingValues;
                    String selection = occupationTypeBox.getValue();
                    boolean isCustom = selection == null || PresetCustomSection.isCustomOption(selection);

                    customOccupationLabel.setVisible(isCustom);
                    customOccupationLabel.setManaged(isCustom);
                    customOccupationField.setVisible(isCustom);
                    customOccupationField.setManaged(isCustom);
                    addParameterButton.setVisible(isCustom);
                    addParameterButton.setManaged(isCustom);

                    rows.clear();
                    if (isCustom) {
                        parametersHintLabel.setText("Add one or more name/value pairs.");
                        if (values.isEmpty()) {
                            addCustomRow("", "");
                            return;
                        }

                        for (Map.Entry<String, String> entry : values.entrySet()) {
                            addCustomRow(entry.getKey(), entry.getValue());
                        }
                        return;
                    }

                    String[] presetFieldNames = PresetCustomSection.getFieldNamesForOccupation(selection);
                    parametersHintLabel.setText("Fill in the required fields for " + selection + ".");
                    if (presetFieldNames != null) {
                        for (String fieldName : presetFieldNames) {
                            rows.add(new ParameterRow(fieldName, values.getOrDefault(fieldName, ""), false, false));
                        }
                    }
                    refreshRows();
                }

                private AbstractCustomSection buildSection() {
                    String header = headerField.getText().trim();
                    if (header.isEmpty()) {
                        throw new IllegalArgumentException("Section header is required.");
                    }

                    String selection = occupationTypeBox.getValue();
                    if (selection == null || selection.trim().isEmpty()) {
                        throw new IllegalArgumentException("Select an occupation type.");
                    }

                    boolean isCustom = PresetCustomSection.isCustomOption(selection);
                    String occupation = isCustom ? customOccupationField.getText().trim() : selection;
                    if (occupation.isEmpty()) {
                        throw new IllegalArgumentException("Occupation name is required.");
                    }

                    List<CustomParameter> parameters = new ArrayList<>();
                    for (ParameterRow row : rows) {
                        String name = row.getName().trim();
                        String value = row.getValue().trim();

                        if (name.isEmpty() && value.isEmpty()) {
                            continue;
                        }
                        if (name.isEmpty() || value.isEmpty()) {
                            throw new IllegalArgumentException("Every parameter must have both a name and a value.");
                        }

                        parameters.add(new CustomParameter(name, value));
                    }

                    if (parameters.isEmpty()) {
                        throw new IllegalArgumentException("Add at least one parameter.");
                    }

                    String[] presetFieldNames = PresetCustomSection.getFieldNamesForOccupation(occupation);
                    AbstractCustomSection section;
                    if (presetFieldNames != null) {
                        section = new PresetCustomSection(header, occupation, presetFieldNames);
                    } else {
                        section = new FlexibleCustomSection(header, occupation);
                    }

                    if (existing != null) {
                        section.setId(existing.getId());
                        section.setResumeId(existing.getResumeId());
                    }
                    section.setParameters(parameters);
                    return section;
                }
            }

            SectionFormState formState = new SectionFormState();
            Map<String, String> initialValues = new LinkedHashMap<>();
            if (existing != null) {
                for (CustomParameter parameter : existing.getParameters()) {
                    initialValues.put(safe(parameter.getName()), safe(parameter.getValue()));
                }
            }
            formState.configureRows(initialValues);

            occupationTypeBox.valueProperty().addListener((observable, oldValue, newValue) ->
                    formState.configureRows(formState.snapshotValues())
            );
            addParameterButton.setOnAction(event -> formState.addCustomRow("", ""));

            ScrollPane parameterScrollPane = new ScrollPane(parameterRowsBox);
            parameterScrollPane.setFitToWidth(true);
            parameterScrollPane.setPrefHeight(220);

            GridPane grid = createDialogGrid();
            grid.add(new Label("Header"), 0, 0);
            grid.add(headerField, 1, 0);
            grid.add(new Label("Occupation Type"), 0, 1);
            grid.add(occupationTypeBox, 1, 1);
            grid.add(customOccupationLabel, 0, 2);
            grid.add(customOccupationField, 1, 2);
            grid.add(parametersLabel, 0, 3);
            VBox parameterSection = new VBox(8, parametersHintLabel, parameterScrollPane, addParameterButton);
            grid.add(parameterSection, 1, 3);

            GridPane.setHgrow(occupationTypeBox, Priority.ALWAYS);
            GridPane.setHgrow(customOccupationField, Priority.ALWAYS);
            GridPane.setHgrow(parameterSection, Priority.ALWAYS);
            GridPane.setVgrow(parameterSection, Priority.ALWAYS);

            Node saveButton = dialog.getDialogPane().lookupButton(saveType);
            saveButton.disableProperty().bind(
                    Bindings.createBooleanBinding(
                            () -> headerField.getText().trim().isEmpty()
                                    || occupationTypeBox.getValue() == null
                                    || (PresetCustomSection.isCustomOption(occupationTypeBox.getValue())
                                            && customOccupationField.getText().trim().isEmpty()),
                            headerField.textProperty(),
                            occupationTypeBox.valueProperty(),
                            customOccupationField.textProperty()
                    )
            );

            saveButton.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    formState.buildSection();
                } catch (IllegalArgumentException ex) {
                    showError(stage, "Invalid Custom Section", ex.getMessage());
                    event.consume();
                }
            });

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().setPrefWidth(700);
            dialog.setResultConverter(button -> {
                if (button != saveType) {
                    return null;
                }
                return formState.buildSection();
            });

            dialog.showAndWait();
            return dialog.getResult();
        }

        private GridPane createDialogGrid() {
            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(12);
            grid.setPadding(new Insets(12));
            grid.setPrefWidth(460);
            return grid;
        }

        private String formatCustomSection(AbstractCustomSection section) {
            StringBuilder builder = new StringBuilder();
            builder.append("Header: ").append(safe(section.getHeader())).append("\n");
            builder.append("Occupation: ").append(safe(section.getOccupationName())).append("\n\n");
            for (CustomParameter parameter : section.getParameters()) {
                builder.append(safe(parameter.getName()))
                        .append(" = ")
                        .append(safe(parameter.getValue()))
                        .append("\n");
            }
            return builder.toString().trim();
        }

        private <T> TableColumn<T, String> createTextColumn(String title, Function<T, String> valueExtractor) {
            TableColumn<T, String> column = new TableColumn<>(title);
            column.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(safe(valueExtractor.apply(cellData.getValue()))));
            return column;
        }

        private List<Education> copyEducationList(List<Education> source) {
            List<Education> copies = new ArrayList<>();
            for (Education education : source) {
                copies.add(copyEducation(education));
            }
            return copies;
        }

        private List<Experience> copyExperienceList(List<Experience> source) {
            List<Experience> copies = new ArrayList<>();
            for (Experience experience : source) {
                copies.add(copyExperience(experience));
            }
            return copies;
        }

        private List<Project> copyProjectList(List<Project> source) {
            List<Project> copies = new ArrayList<>();
            for (Project project : source) {
                copies.add(copyProject(project));
            }
            return copies;
        }

        private List<Skill> copySkillList(List<Skill> source) {
            List<Skill> copies = new ArrayList<>();
            for (Skill skill : source) {
                copies.add(copySkill(skill));
            }
            return copies;
        }

        private List<AbstractCustomSection> copyCustomSectionList(List<AbstractCustomSection> source) {
            List<AbstractCustomSection> copies = new ArrayList<>();
            for (AbstractCustomSection section : source) {
                FlexibleCustomSection copy = new FlexibleCustomSection(safe(section.getHeader()), safe(section.getOccupationName()));
                copy.setId(section.getId());
                copy.setResumeId(section.getResumeId());
                copy.setParameters(new ArrayList<>(copyCustomParameters(section.getParameters())));
                copies.add(copy);
            }
            return copies;
        }

        private List<CustomParameter> copyCustomParameters(List<CustomParameter> source) {
            List<CustomParameter> copies = new ArrayList<>();
            for (CustomParameter parameter : source) {
                CustomParameter copy = new CustomParameter();
                copy.setId(parameter.getId());
                copy.setSectionId(parameter.getSectionId());
                copy.setName(parameter.getName());
                copy.setValue(parameter.getValue());
                copies.add(copy);
            }
            return copies;
        }

        private Education copyEducation(Education education) {
            Education copy = new Education();
            if (education != null) {
                copy.setId(education.getId());
                copy.setResumeId(education.getResumeId());
                copy.setDegree(education.getDegree());
                copy.setUniversity(education.getUniversity());
                copy.setYear(education.getYear());
                copy.setCgpa(education.getCgpa());
            }
            return copy;
        }

        private Experience copyExperience(Experience experience) {
            Experience copy = new Experience();
            if (experience != null) {
                copy.setId(experience.getId());
                copy.setResumeId(experience.getResumeId());
                copy.setRole(experience.getRole());
                copy.setCompany(experience.getCompany());
                copy.setDuration(experience.getDuration());
                copy.setDescription(experience.getDescription());
            }
            return copy;
        }

        private Project copyProject(Project project) {
            Project copy = new Project();
            if (project != null) {
                copy.setId(project.getId());
                copy.setResumeId(project.getResumeId());
                copy.setTitle(project.getTitle());
                copy.setDescription(project.getDescription());
            }
            return copy;
        }

        private Skill copySkill(Skill skill) {
            Skill copy = new Skill();
            if (skill != null) {
                copy.setId(skill.getId());
                copy.setResumeId(skill.getResumeId());
                copy.setSkillName(skill.getSkillName());
            }
            return copy;
        }

        private static final class ParameterRow {
            private final HBox root;
            private final TextField nameField;
            private final TextField valueField;
            private final Button removeButton;

            private ParameterRow(String name, String value, boolean nameEditable, boolean removable) {
                this.nameField = new TextField(name);
                this.valueField = new TextField(value);
                this.removeButton = new Button("Remove");

                nameField.setPromptText("Name");
                valueField.setPromptText("Value");
                nameField.setEditable(nameEditable);
                nameField.setFocusTraversable(nameEditable);
                nameField.setPrefWidth(180);
                valueField.setPrefWidth(260);

                removeButton.setVisible(removable);
                removeButton.setManaged(removable);

                HBox.setHgrow(valueField, Priority.ALWAYS);
                this.root = new HBox(8, nameField, valueField, removeButton);
                this.root.setAlignment(Pos.CENTER_LEFT);
            }

            private Node getNode() {
                return root;
            }

            private String getName() {
                return nameField.getText();
            }

            private String getValue() {
                return valueField.getText();
            }

            private void setOnRemove(javafx.event.EventHandler<ActionEvent> handler) {
                removeButton.setOnAction(handler);
            }
        }
    }
}
