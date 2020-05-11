package controller;

import dao.GameDao;
import dao.GameTypeDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import management.AlertManagement;
import management.PageManagement;
import model.Employee;
import model.Game;
import model.GameType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Game2Page {

    public static String mode;
    public static Game game;

    private String empName = LoginPage.empName;
    private GameDao gameDao = new GameDao();
    private PageManagement page = new PageManagement();
    private AlertManagement alert = new AlertManagement();

    private ObservableList<String> numberForCB, typeForCB;
    private String fileName, sourcePath;
    private List<GameType> gameTypeList;
    private List<String> gameNameList = new ArrayList<>();
    private List<String> gameImageNameList = new ArrayList<>();
    private String gameName, typeName, gameDetail;
    private int playerMin, playerMax, quantity, avl;

    @FXML private Label nameLB, titleLB, id;
    @FXML private TextField name;
    @FXML private TextArea detail;
    @FXML private ChoiceBox type, min, max, qnt, available;
    @FXML private ImageView imageView = new ImageView();
    @FXML private Button openBT;

    @FXML public void initialize() {
        nameLB.setText(empName);
        setValue();

        if (mode == "EDIT") {
            titleLB.setText("แก้ไขเกม");
            id.setText("รหัสเกม :\t" + game.getGameId());
            type.setValue(game.getTypeName());
            name.setText(game.getGameName());
            detail.setText(game.getGameDetail());
            min.setValue(String.valueOf(game.getPlayerMin()));
            max.setValue(String.valueOf(game.getPlayerMax()));
            qnt.setValue(String.valueOf(game.getQuantity()));
            available.setValue(String.valueOf(game.getAvailable()));
            fileName = game.getImageName();

            imageView.setImage(new Image(game.getImageRef()));
        } else {
            titleLB.setText("เพิ่มเกม");
            id.setText("");
            type.setValue(typeForCB.get(0));
            min.setValue(numberForCB.get(0));
            max.setValue(numberForCB.get(0));
            qnt.setValue(numberForCB.get(0));
            available.setValue(numberForCB.get(0));
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
                    sourcePath = selectedFile.getAbsolutePath();
                    imageView.setImage(new Image("file:" + sourcePath));
                }
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

    @FXML public void handleBackButton() {
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/game.fxml");
    }

    @FXML public void handleSaveButton() {
        if (mode == "EDIT") {
            gameNameList.remove(game.getGameName());
            gameImageNameList.remove(game.getImageName());
            if (checkSaveBTCondition()) {
                if (alert.alertConfirmation()) {
                    if (!(sourcePath == "")) {
                        gameDao.deleteImage(game.getImageName());
                        String ref = gameDao.uploadImage(sourcePath, fileName);
                        gameDao.update(new Game(game.getGameId(), gameName, typeName, gameDetail,
                                fileName, ref, playerMin, playerMax, quantity, avl,
                                game.getActive(), game.getRating()));
                    } else {
                        gameDao.update(new Game(game.getGameId(), gameName, typeName, gameDetail,
                                fileName, game.getImageRef(), playerMin, playerMax, quantity, avl,
                                game.getActive(), game.getRating()));
                    }
                    handleBackButton();
                }
            } else {
                gameNameList.add(game.getGameName());
                gameImageNameList.add(game.getImageName());
                alert.alertFaultWarning();
            }
        } else if (mode == "ADD"){
            boolean hasImage = !(sourcePath == "");
            if (checkSaveBTCondition() && hasImage) {
                if (alert.alertConfirmation()) {
                    String ref = gameDao.uploadImage(sourcePath, fileName);
                    gameDao.add(new Game(null, gameName, typeName, gameDetail,
                            fileName, ref, playerMin, playerMax, quantity, avl, 0, 0));
                    handleBackButton();
                }
            } else {
                alert.alertFaultWarning();
            }
        }
    }

    private boolean checkSaveBTCondition() {
        gameName = name.getText().trim();
        gameDetail = detail.getText().trim();
        typeName = String.valueOf(type.getValue());
        playerMax = Integer.parseInt(String.valueOf(max.getValue()));
        playerMin = Integer.parseInt(String.valueOf(min.getValue()));
        quantity = Integer.parseInt(String.valueOf(qnt.getValue()));
        avl = Integer.parseInt(String.valueOf(available.getValue()));

        boolean hasData = (gameName != "" && gameDetail != "");
        boolean isMaxMore = (playerMax > playerMin);
        boolean isInQnt = (quantity >= avl);
        boolean gameDup = gameNameList.contains(gameName);
        boolean imageDup = gameImageNameList.contains(fileName);

        return hasData && isMaxMore && isInQnt  && !gameDup && !imageDup;
    }

    private void setValue() {
        List<String> n = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            n.add(String.valueOf(i));
        }
        numberForCB = FXCollections.observableArrayList(n);
        min.setItems(numberForCB);
        max.setItems(numberForCB);
        qnt.setItems(numberForCB);
        available.setItems(numberForCB);

        GameTypeDao typeDao = new GameTypeDao();
        gameTypeList = typeDao.getAll();
        List<String> t = new ArrayList<>();
        for (GameType g : gameTypeList) {
            t.add(g.getTypeName());
        }
        typeForCB = FXCollections.observableArrayList(t);
        type.setItems(typeForCB);

        List<Game> games = gameDao.getAll();
        for (Game g : games) {
            gameNameList.add(g.getGameName());
            gameImageNameList.add(g.getImageName());
        }

        sourcePath = "";
    }
}
