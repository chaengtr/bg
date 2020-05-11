package controller;

import dao.CustomerDao;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import management.AlertManagement;
import management.PageManagement;
import model.Customer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerPage {
    private String empName = LoginPage.empName;
    private CustomerDao cusDao = new CustomerDao();
    private PageManagement page = new PageManagement();
    private AlertManagement alert = new AlertManagement();

    private List<Customer> customers = new ArrayList<>();

    @FXML private Label nameLB;
    @FXML private TextField searchTF;
    @FXML private TableView tableView;
    @FXML private TableColumn usernameTC;
    @FXML private TableColumn emailTC;
    @FXML private TableColumn pointTC;
    @FXML private TableColumn lastActivePointTC;

    @FXML public void initialize() {
        nameLB.setText(empName);
        customers = cusDao.getAll();
        setTableInfo(customers);
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

    @FXML public void handleRefreshButton() {
        customers = cusDao.getAll();
        setTableInfo(customers);
    }

    @FXML public void handleSearchButton() {
        String user = searchTF.getText().trim().toLowerCase();
        if (user.equals("")) {
            alert.alertFaultWarning();
        } else {
            customers = cusDao.getAll();
            List<Customer> temp = new ArrayList<>();
            for (Customer cus : customers) {
                if (cus.getCusUsername().toLowerCase().contains(user)) {
                    temp.add(cus);
                }
            }
            if (!temp.isEmpty()) {
                setTableInfo(temp);
            } else {
                alert.alertFaultWarning();
            }
        }
        searchTF.setText("");
    }

    private void setTableInfo(List<Customer> cus) {
        tableView.getItems().clear();
        usernameTC.setCellValueFactory(new PropertyValueFactory<>("cusUsername"));
        emailTC.setCellValueFactory(new PropertyValueFactory<>("email"));
        pointTC.setCellValueFactory(new PropertyValueFactory<>("point"));
        lastActivePointTC.setCellValueFactory(new PropertyValueFactory<>("lastActivePoint"));
        lastActivePointTC.setCellFactory(tableColumn -> {
            TableCell<Customer, Date> cell = new TableCell<>() {
                @Override
                protected void updateItem(Date date, boolean b) {
                    super.updateItem(date, b);
                    if (b) {
                        setText(null);
                    } else {
                        setText(new SimpleDateFormat("yyyy-MM-dd", new Locale("th")).format(date));
                    }
                }
            };
            return cell;
        });
        tableView.getItems().addAll(cus);
    }
}
