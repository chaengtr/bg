package controller;

import dao.EmployeeDao;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import management.AlertManagement;
import management.PageManagement;
import model.Employee;

import java.util.HashSet;

public class ProfilePage {
    private Employee emp = LoginPage.employee;
    private EmployeeDao empDao = new EmployeeDao();
    private PageManagement page = new PageManagement();
    private AlertManagement alert = new AlertManagement();

    @FXML private Label nameLB;
    @FXML private Label idLB;
    @FXML private Label usernameLB;
    @FXML private TextField userTF;
    @FXML private PasswordField userPwdPF;
    @FXML private PasswordField oldPwdPF;
    @FXML private PasswordField newPwdPF;
    @FXML private PasswordField confirmPwdPF;

    @FXML public void initialize() {
        nameLB.setText(emp.getFirstName() + "   " + emp.getLastName());
        idLB.setText(emp.getEmpId());
        usernameLB.setText(emp.getEmpUsername());
    }

    @FXML public void handleSignOutButton() {
        if (alert.alertSignOutConfirmation()) {
            Stage stage = (Stage) nameLB.getScene().getWindow();
            page.changePage(stage, "/view/login.fxml");
        }
    }

    @FXML public void handleHomeButton() {
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/home.fxml");
    }

    @FXML public void handleSaveUsernameButton() {
        HashSet<String> allUsername = empDao.getAllUsername();
        String username = userTF.getText();
        String password = userPwdPF.getText();
        boolean dupUsername = allUsername.contains(username);
        boolean hasSpace = username.contains(" ");
        boolean truePassword = emp.getPassword().equals(password);
        if (!dupUsername && !hasSpace && truePassword) {
            if (alert.alertConfirmation()) {
                emp.setEmpUsername(userTF.getText());
                empDao.update(emp);
                handleHomeButton();
            }
        } else {
            alert.alertFaultWarning();
        }
        userTF.setText("");
        userPwdPF.setText("");
    }

    @FXML public void handleSavePasswordButton() {
        String oldPwd = oldPwdPF.getText();
        String newPwd = newPwdPF.getText();
        String confirmPwd = confirmPwdPF.getText();
        boolean dupOldPassword = newPwd.equals(oldPwd);
        boolean hasSpace = newPwd.contains(" ");
        boolean correctOldPassword = emp.getPassword().equals(oldPwd);
        boolean correctPassword = newPwd.equals(confirmPwd);
        if (!dupOldPassword && !hasSpace && correctOldPassword && correctPassword) {
            if (alert.alertConfirmation()) {
                emp.setPassword(newPwd);
                empDao.update(emp);
                handleHomeButton();
            }
        } else {
            alert.alertFaultWarning();
        }
        oldPwdPF.setText("");
        newPwdPF.setText("");
        confirmPwdPF.setText("");
    }
}
