package controller;

import dao.BookingDao;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import management.AlertManagement;
import management.PageManagement;
import management.ReceiptManagement;
import model.*;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingPage {
    private String empName = LoginPage.empName;
    private BookingDao bookDao = new BookingDao();
    private PageManagement page = new PageManagement();
    private AlertManagement alert = new AlertManagement();

    private List<Booking> bookingList = new ArrayList<>();
    private List<BookingTable> data;
    private DecimalFormat df = new DecimalFormat("000000");

    @FXML private Label nameLB;
    @FXML private TextField searchTF, codeTF;
    @FXML private TableView tableView;
    @FXML private TableColumn codeCol, cusCol, gameCol, startCol, endCol, usageCol, receiptCol, pointCol;
    @FXML private DatePicker datePicker;

    @FXML public void initialize() {
        nameLB.setText(empName);
        datePicker.setValue(LocalDate.now());
        handleDateButton();
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

    @FXML public void handleDateButton() {
        Date date = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        bookingList = bookDao.getBooking(date);
        setPage(bookingList);
    }

    @FXML public void handleSearchButton() {
        String search = searchTF.getText().trim().toLowerCase();
        if (search.equals("")) {
            alert.alertFaultWarning();
        } else {
            List<Booking> temp = new ArrayList<>();
            for (Booking booking : bookingList) {
                if (booking.getBookingCode().toLowerCase().contains(search)) {
                    temp.add(booking);
                }
            }
            if (!temp.isEmpty()) {
                setPage(temp);
            } else {
                alert.alertFaultWarning();
            }
        }
    }

    @FXML public void handleSaveCodeButton() {
        String code = codeTF.getText().trim().toUpperCase();
        List<Booking> confirmList = new ArrayList<>();
        List<Booking> finishList = new ArrayList<>();

        for (Booking booking : bookingList) {
            String bStatus = booking.getUsageStatus();
            if (booking.getBookingCode().equals(code) && bStatus.equals("จอง")) {
                confirmList.add(booking);
            } else if (booking.getBookingCode().equals(code) && bStatus.equals("ยืนยันการจอง")) {
                finishList.add(booking);
            }
        }

        if (!confirmList.isEmpty() || !finishList.isEmpty()) {
            List<Booking> temp = new ArrayList<>();
            if (alert.alertConfirmation()) {
                for (Booking booking : confirmList) {
                    booking.setBookingStart(new Date());
                    booking.setUsageStatus("ยืนยันการจอง");
                    temp.add(booking);
                }
                for (Booking booking : finishList) {
                    booking.setBookingEnd(new Date());
                    booking.setUsageStatus("เสร็จสิ้นการจอง");
                    temp.add(booking);
                }
                bookDao.updateBooking(temp);
                codeTF.setText("");
                setPage(bookingList);
            }
        } else {
            alert.alertFaultWarning();
        }
    }

    @FXML public void handleCancelButton() {
        if (!tableView.getSelectionModel().isEmpty()) {
            if (alert.alertConfirmation()) {
                int index = data.indexOf(tableView.getSelectionModel().getSelectedItem());
                Booking booking = bookingList.get(index);
                booking.setUsageStatus("ยกเลิกการจอง");
                bookDao.cancelBooking(booking);
                setPage(bookingList);
            }
        } else {
            alert.alertFaultWarning();
        }
    }

    @FXML public void handleReceiptButton() {
        if (!tableView.getSelectionModel().isEmpty()) {
            BookingTable table = (BookingTable) tableView.getSelectionModel().getSelectedItem();
            int index = data.indexOf(tableView.getSelectionModel().getSelectedItem());
            Booking booking = bookingList.get(index);
            if (booking.getUsageStatus().equals("เสร็จสิ้นการจอง") && !booking.getReceiptStatus().equals("ออกใบเสร็จ")) {
                if (alert.alertConfirmation()) {
                    ReceiptManagement rm = new ReceiptManagement();
                    String id = df.format(bookDao.getReceiptSize() + 1);
                    String code = table.getBookingCode();
                    String user = table.getUserName();
                    String game = table.getGameName();
                    String pro = table.getProName();
                    int hour = getHour(booking);
                    double prize = hour * 40;
                    double discount = getDiscount(booking.getProId());
                    double amount = prize - discount;
                    int point = (int) (amount / 10);
                    Date start = booking.getBookingStart();
                    Date end = booking.getBookingEnd();
                    Receipt receipt = new Receipt(id, new Date(), user, game, pro, prize, discount, amount, hour, point, code, start, end);
                    rm.receipt(receipt);
                    bookDao.addReceipt(receipt);
                    bookDao.updateCustomerPoint(booking.getUserId(), point);

                    booking.setReceiptStatus("ออกใบเสร็จ");
                    bookDao.cancelBooking(booking);
                    setPage(bookingList);
                }
            } else {
                alert.alertFaultWarning();
            }
        } else {
            alert.alertFaultWarning();
        }
    }

    private int getHour(Booking booking) {
        Date start = booking.getBookingStart();
        Date end = booking.getBookingEnd();
        int minDiff = ((int) (end.getTime() - start.getTime())) / 60000;
        int hour = minDiff / 60;
        int fraction = minDiff % 60;
        if (fraction > 15) {
            hour += 1;
        }
        return hour;
    }

    private double getDiscount(String promotionId) {
        double discount = 0;
        if (!promotionId.isEmpty()) {
            Promotion pro = bookDao.getPromotion(promotionId);
            if (pro.getProType().equals("ส่วนลด")) {
                discount = pro.getProPoint();
            } else if (pro.getProType().equals("เล่นฟรี")) {
                discount = (pro.getProPoint() / 60) * 40;
            }
        }
        return discount;
    }

    private void setPage(List<Booking> list) {
        data = new ArrayList<>();
        for (Booking b : list) {
            String userName = bookDao.getName("customers", b.getUserId(), "cus_username");
            String gameName = bookDao.getName("games", b.getGameId(), "game_name");
            String proName = "";
            if (!b.getProId().isEmpty()) {
                proName = bookDao.getName("promotions", b.getProId(), "pro_name");
            }
            data.add(new BookingTable(b, userName, gameName, proName));
        }

        tableView.getItems().clear();
        codeCol.setCellValueFactory(new PropertyValueFactory<>("bookingCode"));
        cusCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        gameCol.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("bookingStart"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("bookingEnd"));
        usageCol.setCellValueFactory(new PropertyValueFactory<>("usageStatus"));
        receiptCol.setCellValueFactory(new PropertyValueFactory<>("receiptStatus"));
        pointCol.setCellValueFactory(new PropertyValueFactory<>("proName"));
        tableView.getItems().addAll(data);
    }
}
