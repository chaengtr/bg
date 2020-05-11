package model;

public class Game {

    private String gameId, gameName, typeName, gameDetail, imageName, imageRef;
    private int playerMin, playerMax, quantity, available, active;
    private double rating;

    public Game(String gameId, String gameName, String typeName,
                String gameDetail, String imageName, String imageRef,
                int playerMin, int playerMax, int quantity, int available, int active, double rating) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.typeName = typeName;
        this.gameDetail = gameDetail;
        this.imageName = imageName;
        this.imageRef = imageRef;
        this.playerMin = playerMin;
        this.playerMax = playerMax;
        this.quantity = quantity;
        this.available = available;
        this.active = active;
        this.rating = rating;
    }

    public String getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getGameDetail() {
        return gameDetail;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageRef() {
        return imageRef;
    }

    public int getPlayerMin() {
        return playerMin;
    }

    public int getPlayerMax() {
        return playerMax;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getAvailable() {
        return available;
    }

    public int getActive() {
        return active;
    }

    public double getRating() {
        return rating;
    }
}
