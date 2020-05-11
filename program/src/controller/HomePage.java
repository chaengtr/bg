package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import management.AlertManagement;
import management.PageManagement;
import model.Employee;

public class HomePage {

    private String empName = LoginPage.empName;
    private PageManagement page = new PageManagement();
    private AlertManagement alert = new AlertManagement();

    @FXML private Label nameLB;

    @FXML public void initialize() {
        nameLB.setText(empName);
    }

    @FXML public void handleSignOutButton() {
        if (alert.alertSignOutConfirmation()) {
            Stage stage = (Stage) nameLB.getScene().getWindow();
            page.changePage(stage, "/view/login.fxml");
        }
    }

    @FXML public void handleProfileButton() {
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/profile.fxml");
    }

    @FXML public void handleGameAddButton() {
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/game.fxml");
    }

    @FXML public void handlePromotionAddButton() {
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/promotion.fxml");
    }

    @FXML public void handleBannerAddButton() {
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/banner.fxml");
    }

    @FXML public void handleCustomerInfoButton() {
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/customer.fxml");
    }

    @FXML public void handleBookingInfoButton() {
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/booking.fxml");
    }

    @FXML public void handleDailyReportButton() {
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/report.fxml");
    }
}
