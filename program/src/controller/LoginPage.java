package controller;

import dao.EmployeeDao;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import management.AlertManagement;
import management.PageManagement;
import model.Employee;

public class LoginPage {
    protected static Employee employee;
    protected static String empName = "";
    private EmployeeDao empDao = new EmployeeDao();
    private PageManagement page = new PageManagement();
    private AlertManagement alert = new AlertManagement();

    private String username;
    private String password;

    @FXML private TextField usernameTF;
    @FXML private PasswordField passwordPF;

    @FXML public void handleSignInButton() {
        username = usernameTF.getText();
        password = passwordPF.getText();
        usernameTF.setText("");
        passwordPF.setText("");

        if (empDao.check(username, password)) {
            employee = empDao.getInformation();
            empName = employee.getFirstName() + "   " + employee.getLastName();
            Stage stage = (Stage) usernameTF.getScene().getWindow();
            page.changePage(stage, "/view/home.fxml");
        } else {
            alert.alertFaultWarning();
        }
    }
}
