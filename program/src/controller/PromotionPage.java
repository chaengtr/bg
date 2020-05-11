package controller;

import dao.PromotionDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import management.AlertManagement;
import management.PageManagement;
import model.Employee;
import model.Promotion;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PromotionPage {

    private String empName = LoginPage.empName;
    private PromotionDao proDao = new PromotionDao();
    private PageManagement page = new PageManagement();
    private AlertManagement alert = new AlertManagement();

    private List<Promotion> promotions = new ArrayList<>();
    private Date start = new Date();
    private Date end = new Date();
    private String name, detail, type;
    private int point;
    private ObservableList<String> points, proType;

    @FXML private Label nameLB;
    @FXML private Label modeLB;
    @FXML private Label proIdLB;
    @FXML private Button saveBT;
    @FXML private TextField searchTF;
    @FXML private TableView<Promotion> tableView;
    @FXML private TableColumn nameTC;
    @FXML private TableColumn pointTC;
    @FXML private TextField proNameTF;
    @FXML private TextField detailTF;
    @FXML private DatePicker startDate = new DatePicker();
    @FXML private DatePicker endDate = new DatePicker();
    @FXML private ChoiceBox pointCB, typeCB;

    @FXML public void initialize() {
        nameLB.setText(empName);
        promotions = proDao.getAll();

        List<String> p = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            p.add(String.valueOf(i * 10));
        }
        points = FXCollections.observableArrayList(p);
        pointCB.setItems(points);

        proType = FXCollections.observableArrayList("เล่นฟรี", "แลกของ", "ส่วนลด");
        typeCB.setItems(proType);

        setTableInfo(promotions);
        setDefault();

        saveBT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                alert.alertFaultWarning();
            }
        });
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
        promotions = proDao.getAll();
        setTableInfo(promotions);
        setDefault();
    }

    @FXML public void handleSelectedNameButton() {
        String search = searchTF.getText().trim().toLowerCase();
        if (search.equals("")) {
            alert.alertFaultWarning();
        } else {
            promotions = proDao.getAll();
            List<Promotion> temp = new ArrayList<>();
            for (Promotion pro : promotions) {
                if (pro.getProName().toLowerCase().contains(search)) {
                    temp.add(pro);
                }
            }
            if (!temp.isEmpty()) {
                setTableInfo(temp);
            } else {
                alert.alertFaultWarning();
            }
        }
        setDefault();
        searchTF.setText("");
    }

    @FXML public void handleDetailButton() {
        if (!tableView.getSelectionModel().isEmpty()) {
            Promotion pro = (Promotion) nameTC.getTableView().getSelectionModel().getSelectedItem();
            int index = promotions.indexOf(pro);
            modeLB.setText("รายละเอียดโปรโมชัน");
            setData(index);
        } else {
            alert.alertFaultWarning();
        }

        saveBT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                alert.alertFaultWarning();
            }
        });
    }

    @FXML public void handleEditButton() {
        if (!tableView.getSelectionModel().isEmpty()) {
            Promotion pro = (Promotion) nameTC.getTableView().getSelectionModel().getSelectedItem();
            int index = promotions.indexOf(pro);
            modeLB.setText("แก้ไขโปรโมชัน");
            setData(index);

            saveBT.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if (checkSaveBTCondition()) {
                        if (alert.alertConfirmation()) {
                            proDao.update(new Promotion(pro.getProId(), name, detail, start, end, point, type));
                            promotions = proDao.getAll();
                            setTableInfo(promotions);
                        }
                    }
                    setDefault();
                }
            });
        } else {
            alert.alertFaultWarning();
        }
    }

    @FXML public void handleAddButton() {
        setDefault();
        modeLB.setText("เพิ่มโปรโมชัน");

        saveBT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (checkSaveBTCondition()) {
                    Promotion pro = new Promotion(null, name, detail, start, end, point, type);
                    if (alert.alertConfirmation()) {
                        proDao.add(pro);
                        promotions = proDao.getAll();
                        setTableInfo(promotions);
                    }
                } else {
                    alert.alertFaultWarning();
                }
                setDefault();
            }
        });
    }

    @FXML public void handleRemoveButton() {
        if (!tableView.getSelectionModel().isEmpty()) {
            Promotion pro = (Promotion) nameTC.getTableView().getSelectionModel().getSelectedItem();
            int index = promotions.indexOf(pro);
            if (alert.alertConfirmation()) {
                proDao.remove(promotions.get(index).getProId());
                promotions = proDao.getAll();
                setTableInfo(promotions);
            }
        } else {
            alert.alertFaultWarning();
        }
    }

    private boolean checkSaveBTCondition() {
        name = proNameTF.getText().trim();
        detail = detailTF.getText();
        start = Date.from(startDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        end = Date.from(endDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        point = Integer.parseInt(String.valueOf(pointCB.getValue()));
        type = String.valueOf(typeCB.getValue());

        boolean hasData = !(name.equals("") || detail.equals(""));
        boolean correctDate = start.before(end);
        return hasData && correctDate;
    }

    private void setTableInfo(List<Promotion> pro) {
        tableView.getItems().clear();
        nameTC.setCellValueFactory(new PropertyValueFactory<>("proName"));
        pointTC.setCellValueFactory(new PropertyValueFactory<>("proPoint"));
        tableView.getItems().addAll(pro);
    }

    private void setDefault() {
        modeLB.setText("...กรุณาเลือกโหมดการทำงาน...");
        proIdLB.setText("");
        proNameTF.setText("");
        detailTF.setText("");
        startDate.setValue(LocalDate.now());
        endDate.setValue(LocalDate.now());
        pointCB.setValue(points.get(0));
        typeCB.setValue(proType.get(0));
    }

    private void setData(int index) {
        Promotion pro = promotions.get(index);
        proIdLB.setText(pro.getProId());
        proNameTF.setText(pro.getProName());
        detailTF.setText(pro.getProDetail());
        startDate.setValue(pro.getProStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        endDate.setValue(pro.getProEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        pointCB.setValue(String.valueOf(pro.getProPoint()));
        typeCB.setValue(String.valueOf(pro.getProType()));
    }
}
