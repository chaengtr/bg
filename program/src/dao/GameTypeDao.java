package dao;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import main.Main;
import model.GameType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameTypeDao {

    private FirebaseConnection connection = Main.connection;
    private String collectionName = "game_types";

    public List<GameType> getAll() {
        connection.setCollectionName(collectionName);
        List<GameType> gameTypes = new ArrayList<>();
        List<QueryDocumentSnapshot> documents = connection.getAllDocument("type_name");

        for (QueryDocumentSnapshot document : documents) {
            String id = document.getId();
            String name = document.getString("type_name");
            gameTypes.add(new GameType(id, name));
        }
        return gameTypes;
    }

    public void add(GameType gameType) {
        connection.setCollectionName(collectionName);
        Map<String, Object> data = setData(gameType);
        connection.addDocument(data);
    }

    private Map<String, Object> setData(GameType gameType) {
        Map<String, Object> data = new HashMap<>();
        data.put("type_name", gameType.getTypeName());
        return data;
    }

    public void remove(String id) {
        connection.setCollectionName(collectionName);
        connection.deleteDocument(id);
    }
}
