package dao;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import main.Main;
import model.Promotion;

import java.util.*;

public class PromotionDao {

    private FirebaseConnection connection = Main.connection;
    private String collectionName = "promotions";

    public PromotionDao() {
        connection.setCollectionName(collectionName);
    }

    public List<Promotion> getAll() {
        List<Promotion> promotions = new ArrayList<>();
        List<QueryDocumentSnapshot> documents = connection.getAllDocument("pro_name");

        for (QueryDocumentSnapshot document : documents) {
            String id = document.getId();
            String name = document.getString("pro_name");
            String detail = document.getString("pro_detail");
            Date start = document.getDate("pro_start");
            Date end = document.getDate("pro_end");
            double p = document.getDouble("pro_point");
            int point = (int) p;
            String type = document.getString("pro_type");
            promotions.add(new Promotion(id, name, detail, start, end, point, type));
        }
        return promotions;
    }

    public void update(Promotion pro) {
        Map<String, Object> data = setData(pro);
        String id = pro.getProId();
        connection.updateDocument(id, data);
    }

    public void add(Promotion pro) {
        Map<String, Object> data = setData(pro);
        connection.addDocument(data);
    }

    private Map<String, Object> setData(Promotion pro) {
        Map<String, Object> data = new HashMap<>();
        data.put("pro_name", pro.getProName());
        data.put("pro_detail", pro.getProDetail());
        data.put("pro_start", pro.getProStart());
        data.put("pro_end", pro.getProEnd());
        data.put("pro_point", pro.getProPoint());
        data.put("pro_type", pro.getProType());
        return data;
    }

    public void remove(String id) {
        connection.deleteDocument(id);
    }
}