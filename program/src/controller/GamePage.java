package controller;

import dao.GameDao;
import dao.GameTypeDao;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import management.AlertManagement;
import management.PageManagement;
import model.Game;
import model.GameType;

import java.util.ArrayList;
import java.util.List;

public class GamePage {
    private String empName = LoginPage.empName;
    private GameTypeDao typeDao = new GameTypeDao();
    private GameDao gameDao = new GameDao();
    private PageManagement page = new PageManagement();
    private AlertManagement alert = new AlertManagement();

    private List<GameType> gameTypes = new ArrayList<>();
    private List<Game> games = new ArrayList<>();

    @FXML private Label nameLB;
    @FXML private TextField searchTF;
    @FXML private TableView typeTable;
    @FXML private TableColumn typeNameColumn;
    @FXML private TextField typeTextField;

    @FXML private TableView gameTable;
    @FXML private TableColumn gameCol, typeCol, quantityCol, availableCol, rateCol, imageCol;

    @FXML public void initialize() {
        nameLB.setText(empName);
        gameTypes = typeDao.getAll();
        games = gameDao.getAll();
        setTypeTable(gameTypes);
        setGameTable(games);
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
        games = gameDao.getAll();
        setGameTable(games);
    }

    @FXML public void handleSelectedNameButton() {
        String search = searchTF.getText().trim().toLowerCase();
        if (search.equals("")) {
            alert.alertFaultWarning();
        } else {
            games = gameDao.getAll();
            List<Game> temp = new ArrayList<>();
            for (Game game : games) {
                if (game.getGameName().toLowerCase().contains(search)) {
                    temp.add(game);
                }
            }
            if (!temp.isEmpty()) {
                setGameTable(temp);
            } else {
                alert.alertFaultWarning();
            }
        }
        searchTF.setText("");
    }

    @FXML public void handleAddTypeButton() {
        String type = typeTextField.getText().trim();
        if (!type.equals("")) {
            if (alert.alertConfirmation()) {
                GameType gameType = new GameType(null, type);
                typeDao.add(gameType);
                gameTypes = typeDao.getAll();
                setTypeTable(gameTypes);
                typeTextField.setText("");
            }
        } else {
            alert.alertFaultWarning();
        }
    }

    @FXML public void handleRemoveTypeButton() {
        if (!typeTable.getSelectionModel().isEmpty()) {
            GameType gameType = (GameType) typeNameColumn.getTableView().getSelectionModel().getSelectedItem();
            if (alert.alertConfirmation()) {
                typeDao.remove(gameType.getTypeId());
                gameTypes = typeDao.getAll();
                setTypeTable(gameTypes);
            }
        } else {
            alert.alertFaultWarning();
        }
    }

    @FXML public void handleRemoveGameButton() {
        if (!gameTable.getSelectionModel().isEmpty()) {
            Game game = (Game) gameCol.getTableView().getSelectionModel().getSelectedItem();
            if (alert.alertConfirmation()) {
                gameDao.deleteImage(game.getImageName());
                gameDao.remove(game.getGameId());
                games = gameDao.getAll();
                setGameTable(games);
            }
        } else {
            alert.alertFaultWarning();
        }
    }

    @FXML public void handleEditGameButton() {
        if (!gameTable.getSelectionModel().isEmpty()) {
            Game game = (Game) gameCol.getTableView().getSelectionModel().getSelectedItem();
            Game2Page.mode = "EDIT";
            Game2Page.game = game;
            Stage stage = (Stage) nameLB.getScene().getWindow();
            page.changePage(stage, "/view/game2.fxml");
        } else {
            alert.alertFaultWarning();
        }
    }

    @FXML public void handleAddGameButton() {
        Game2Page.mode = "ADD";
        Game2Page.game = null;
        Stage stage = (Stage) nameLB.getScene().getWindow();
        page.changePage(stage, "/view/game2.fxml");
    }

    private void setTypeTable(List<GameType> gameTypes) {
        typeTable.getItems().clear();
        typeNameColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
        typeTable.getItems().addAll(gameTypes);
    }

    private void setGameTable(List<Game> games) {
        gameTable.getItems().clear();
        availableCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        gameCol.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        imageCol.setCellValueFactory(new PropertyValueFactory<>("imageName"));
        rateCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        rateCol.setCellFactory(tableColumn -> {
            TableCell<Game, Double> cell = new TableCell<>() {
                @Override
                protected void updateItem(Double aDouble, boolean b) {
                    super.updateItem(aDouble, b);
                    if (b) {
                        setText(null);
                    } else {
                        setText(String.format("%.1f", aDouble));
                    }
                }
            };
            return cell;
        });
        gameTable.getItems().addAll(games);
    }
}
