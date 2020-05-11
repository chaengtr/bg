package dao;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import main.Main;
import model.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDao {

    private FirebaseConnection connection = Main.connection;
    private String collectionName = "games";
    private String storageTarget = "games/";

    public List<Game> getAll() {
        connection.setCollectionName(collectionName);
        List<Game> games = new ArrayList<>();
        List<QueryDocumentSnapshot> documents = connection.getAllDocument("game_name");

        for (QueryDocumentSnapshot document : documents) {
            String id = document.getId();
            String name = document.getString("game_name");
            String type = document.getString("type_name");
            String detail = document.getString("game_detail");
            String image = document.getString("image_name");
            String ref = document.getString("image_ref");
            double min = document.getDouble("player_min");
            double max = document.getDouble("player_max");
            double qnt = document.getDouble("quantity");
            double avail = document.getDouble("available");
            double active = document.getDouble("active");
            double rating = document.getDouble("rating");
            games.add(new Game(id, name,type, detail, image, ref, (int) min,
                    (int) max, (int) qnt, (int) avail, (int) active, rating));
        }
        return games;
    }

    public void remove(String id) {
        connection.setCollectionName(collectionName);
        connection.deleteDocument(id);
    }

    public void add(Game game) {
        connection.setCollectionName(collectionName);
        Map<String, Object> data = setData(game);
        connection.addDocument(data);
    }

    public void update(Game game) {
        connection.setCollectionName(collectionName);
        Map<String, Object> data = setData(game);
        String id = game.getGameId();
        connection.updateDocument(id, data);
    }

    private Map<String, Object> setData(Game game) {
        Map<String, Object> data = new HashMap<>();
        data.put("game_name", game.getGameName());
        data.put("game_detail", game.getGameDetail());
        data.put("type_name", game.getTypeName());
        data.put("image_name", game.getImageName());
        data.put("image_ref", game.getImageRef());
        data.put("player_min", game.getPlayerMin());
        data.put("player_max", game.getPlayerMax());
        data.put("quantity", game.getQuantity());
        data.put("available", game.getAvailable());
        data.put("active", game.getActive());
        data.put("rating", game.getRating());
        return  data;
    }

    public String uploadImage(String path, String imageName) {
        return connection.uploadJpeg(path, storageTarget + imageName);
    }

    public void deleteImage(String imageName) {
        connection.deleteJpeg(storageTarget + imageName);
    }
}
