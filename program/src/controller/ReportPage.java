package controller;

import dao.BookingDao;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import management.AlertManagement;
import management.PageManagement;
import model.Employee;
import model.Receipt;
import model.ReceiptTable;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ReportPage {

    private String empName = LoginPage.empName;
    private PageManagement page = new PageManagement();
    private AlertManagement alert = new AlertManagement();
    private BookingDao dao = new BookingDao();

    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

    private List<Receipt> receiptList = new ArrayList<>();

    @FXML private Label nameLB, numLB, priceLB, discountLB, amountLB;
    @FXML private TableView<ReceiptTable> tableView;
    @FXML private TableColumn idTC, timeTC, priceTC, discountTC, amountTC;
    @FXML private DatePicker picker = new DatePicker();
    @FXML private BarChart<?, ?> chart;

    @FXML
    public void initialize() {
        nameLB.setText(empName);
        picker.setValue(LocalDate.now());
        handleConfirmButton();
    }

    @FXML
    public void handleSignOutButton() {
        if (alert.alertSignOutConfirmation()) {
            Stage stage = (Stage) nameLB.getScene().getWindow();
            page.changePage(stage, "/view/login.fxml");
        }
    }

    @FXML
    public void handleHomeButton() {
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/home.fxml");
    }

    @FXML
    public void handleConfirmButton() {
        Date date = Date.from(picker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        setPage(date);
    }

    private void setPage(Date date) {
        receiptList = dao.getReceiptInDay(date);
        numLB.setText(String.valueOf(receiptList.size()));
        double price = 0;
        double discount = 0;
        double amount = 0;

        List<ReceiptTable> data = new ArrayList<>();
        Map<String, Integer> chartData = new HashMap<>();
        List<String> dupCode = new ArrayList<>();
        for (Receipt r : receiptList) {
            price += r.getPrice();
            discount += r.getDiscount();
            amount += r.getAmount();
            String p = String.format("%.2f", r.getPrice());
            String d = String.format("%.2f", r.getDiscount());
            String a = String.format("%.2f", r.getAmount());
            data.add(new ReceiptTable(r.getId(), df.format(r.getDate()), p, d, a));

            String code = r.getBookingCode();
            String game = r.getGame();
            if (!dupCode.contains(code)) {
                dupCode.add(code);
                if (chartData.containsKey(game)) {
                    chartData.put(game, chartData.get(game) + 1);
                } else {
                    chartData.put(game, 1);
                }
            }
        }

        Set<Map.Entry<String, Integer>> set = chartData.entrySet();
        List<Map.Entry<String, Integer>> list = new ArrayList<>(
                set);
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        priceLB.setText(String.format("%.2f", price));
        discountLB.setText(String.format("%.2f", discount));
        amountLB.setText(String.format("%.2f", amount));

        tableView.getItems().clear();
        idTC.setCellValueFactory(new PropertyValueFactory<>("id"));
        timeTC.setCellValueFactory(new PropertyValueFactory<>("date"));
        priceTC.setCellValueFactory(new PropertyValueFactory<>("price"));
        discountTC.setCellValueFactory(new PropertyValueFactory<>("discount"));
        amountTC.setCellValueFactory(new PropertyValueFactory<>("amount"));
        tableView.getItems().addAll(data);

        chart.getData().clear();
        XYChart.Series series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : list) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            series.getData().add(new XYChart.Data(key, value));
        }
        chart.getData().addAll(series);
    }
}
