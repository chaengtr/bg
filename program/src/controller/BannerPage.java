package controller;

import dao.BannerDao;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import management.AlertManagement;
import management.PageManagement;
import model.Banner;
import model.Employee;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BannerPage {

    private String empName = LoginPage.empName;
    private BannerDao bannerDao = new BannerDao();
    private PageManagement page = new PageManagement();
    private AlertManagement alert = new AlertManagement();

    private List<Banner> banners = new ArrayList<>();
    private List<String> bannerNameList = new ArrayList<>();
    private List<String> imageNameLis = new ArrayList<>();
    private Date start = new Date();
    private Date end = new Date();
    private String path, fileName;
    private String imageSelectedPath;
    private String bannerName;

    @FXML private Label nameLB;
    @FXML private TextField searchTF, bannerNameTF;
    @FXML private Label idBannerLB;
    @FXML private Label modeLB;
    @FXML private ImageView imageView = new ImageView();
    @FXML private Button openBT, saveBT;
    @FXML private TableView tableView;
    @FXML private TableColumn nameTC, imageTC;
    @FXML private DatePicker startDate = new DatePicker();
    @FXML private DatePicker endDate = new DatePicker();

    @FXML public void initialize() {
        nameLB.setText(empName);
        setDefault();
        banners = bannerDao.getAll();
        setTableInfo(banners);

        for (Banner banner : banners) {
            bannerNameList.add(banner.getBannerName());
            imageNameLis.add(banner.getImageName());
        }

        openBT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser.ExtensionFilter imageFilter =
                        new FileChooser.ExtensionFilter("Image Files", "*.jpg");
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(imageFilter);

                File selectedFile = fileChooser.showOpenDialog(null);
                if (selectedFile != null) {
                    fileName = selectedFile.getName();
                    imageSelectedPath = selectedFile.getAbsolutePath();
                    imageView.setImage(new Image("file:" + imageSelectedPath));
                }
            }
        });

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
        banners = bannerDao.getAll();
        setTableInfo(banners);
    }

    @FXML public void handleSelectedNameButton() {
        String search = searchTF.getText().trim().toLowerCase();
        if (search.equals("")) {
            alert.alertFaultWarning();
        } else {
            banners = bannerDao.getAll();
            List<Banner> temp = new ArrayList<>();
            for (Banner banner : banners) {
                if (banner.getBannerName().toLowerCase().contains(search)) {
                    temp.add(banner);
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

    @FXML public void handleAddButton() {
        setDefault();
        modeLB.setText("เพิ่มแบนเนอร์");

        saveBT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                boolean hasImage = !(imageSelectedPath == "");
                if (checkSaveBTCondition() && hasImage) {
                    if (alert.alertConfirmation()) {
                        String imageRef = bannerDao.uploadImage(imageSelectedPath, fileName);
                        bannerDao.add(new Banner(null, bannerName, fileName, imageRef, start, end));
                        banners = bannerDao.getAll();
                        bannerNameList.add(bannerName);
                        setTableInfo(banners);
                        setDefault();
                    }
                } else {
                    alert.alertFaultWarning();
                }
            }
        });
    }

    @FXML public void handleEditButton() {
        if (!tableView.getSelectionModel().isEmpty()) {
            Banner banner = (Banner) nameTC.getTableView().getSelectionModel().getSelectedItem();
            String image = banner.getImageRef();

            modeLB.setText("แก้ไขแบนเนอร์");
            idBannerLB.setText(banner.getBannerId());
            bannerNameTF.setText(banner.getBannerName());
            imageView.setImage(new Image(image));
            startDate.setValue(banner.getBannerStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            endDate.setValue(banner.getBannerEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

            saveBT.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    bannerNameList.remove(banner.getBannerName());
                    imageNameLis.remove(banner.getImageName());
                    if (checkSaveBTCondition()) {
                        if (alert.alertConfirmation()) {
                            if (!(imageSelectedPath == "")) {
                                bannerDao.deleteImage(banner.getImageName());
                                String imageRef = bannerDao.uploadImage(imageSelectedPath, fileName);
                                bannerDao.update(new Banner(banner.getBannerId(), bannerName, fileName, imageRef, start, end));
                            } else {
                                bannerDao.update(new Banner(banner.getBannerId(), bannerName,
                                        banner.getImageName(), banner.getImageRef(), start, end));
                            }
                            bannerNameList.add(bannerName);
                            banners = bannerDao.getAll();
                            setTableInfo(banners);
                            setDefault();
                        }
                    } else {
                        bannerNameList.add(banner.getBannerName());
                        imageNameLis.add(banner.getImageName());
                        alert.alertFaultWarning();
                    }
                }
            });
        } else {
            alert.alertFaultWarning();
        }
    }

    @FXML public void handleRemoveButton() {
        if (!tableView.getSelectionModel().isEmpty()) {
            Banner banner = (Banner) nameTC.getTableView().getSelectionModel().getSelectedItem();
            if (alert.alertConfirmation()) {
                bannerDao.deleteImage(banner.getBannerName());
                bannerDao.remove(banner.getBannerId());
                banners = bannerDao.getAll();
                bannerNameList.remove(banner.getBannerName());
                setTableInfo(banners);
            }
        } else {
            alert.alertFaultWarning();
        }
    }

    private boolean checkSaveBTCondition() {
        bannerName = bannerNameTF.getText().trim();
        start = Date.from(startDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        end = Date.from(endDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        boolean hasName = !bannerName.equals("");
        boolean dupName = !bannerNameList.contains(bannerName);
        boolean dupImage = !imageNameLis.contains(fileName);
        boolean correctDate = start.before(end);

        return hasName && dupName && dupImage && correctDate;
    }

    private void setTableInfo(List<Banner> banner) {
        tableView.getItems().clear();
        nameTC.setCellValueFactory(new PropertyValueFactory<>("bannerName"));
        imageTC.setCellValueFactory(new PropertyValueFactory<>("imageName"));
        tableView.getItems().addAll(banner);
    }

    private void setDefault() {
        imageSelectedPath = "";
        bannerName = "";

        modeLB.setText("...กรุณาเลือกโหมดการทำงาน...");
        idBannerLB.setText("");
        bannerNameTF.setText("");
        startDate.setValue(LocalDate.now());
        endDate.setValue(LocalDate.now());
        imageView.setImage(new Image("file:" + path + "/icons/search.png"));
    }
}
