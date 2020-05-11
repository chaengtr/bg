package dao;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import main.Main;
import model.Banner;

import java.util.*;

public class BannerDao {

    private FirebaseConnection connection = Main.connection;
    private String collectionName = "banners";
    private String storageTarget = "banners/";

    public BannerDao() {
        connection.setCollectionName(collectionName);
    }

    public List<Banner> getAll() {
        List<Banner> banners = new ArrayList<>();
        List<QueryDocumentSnapshot> documents = connection.getAllDocument("banner_name");

        for (QueryDocumentSnapshot document : documents) {
            String id = document.getId();
            String name = document.getString("banner_name");
            String image = document.getString("image_name");
            String ref = document.getString("banner_image");
            Date start = document.getDate("banner_start");
            Date end = document.getDate("banner_end");
            banners.add(new Banner(id, name, image, ref, start, end));
        }
        return banners;
    }

    public void update(Banner banner) {
        Map<String, Object> data = setData(banner);
        String id = banner.getBannerId();
        connection.updateDocument(id, data);
    }

    public void add(Banner banner) {
        Map<String, Object> data = setData(banner);
        connection.addDocument(data);
    }

    private Map<String, Object> setData(Banner banner) {
        Map<String, Object> data = new HashMap<>();
        data.put("banner_name", banner.getBannerName());
        data.put("image_name", banner.getImageName());
        data.put("banner_image", banner.getImageRef());
        data.put("banner_start", banner.getBannerStart());
        data.put("banner_end", banner.getBannerEnd());
        return  data;
    }

    public void remove(String id) {
        connection.deleteDocument(id);
    }

    public String uploadImage(String localPath, String imageName) {
        return connection.uploadJpeg(localPath, storageTarget + imageName);
    }

    public void deleteImage(String imageName) {
        connection.deleteJpeg(storageTarget + imageName);
    }
}
