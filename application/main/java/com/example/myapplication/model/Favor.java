package com.example.myapplication.model;

public class Favor {
    private String favorId;
    private String gameId;

    public Favor(String favorId, String gameId) {
        this.favorId = favorId;
        this.gameId = gameId;
    }

    public String getFavorId() {
        return favorId;
    }

    public String getGameId() {
        return gameId;
    }
}
