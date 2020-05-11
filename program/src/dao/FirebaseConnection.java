package dao;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FirebaseConnection {

    private String jsonFile;
    private String databaseUrl;
    private String bucketName;
    private String proId;
    private String collectionName;
    private Storage storage;

    public FirebaseConnection() {
        jsonFile = "my-project-c0f85-firebase-adminsdk-bk5vm-b30e28f4f6.json";
        databaseUrl = "https://my-project-c0f85.firebaseio.com";
        bucketName = "my-project-c0f85.appspot.com";
        proId = "my-project-c0f85";
        collectionName = "employees";
    }

    public void setCollectionName(String collection) {
        collectionName = collection;
    }

    public void connect() {
        try {
            FileInputStream serviceAccount = new FileInputStream(jsonFile);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(databaseUrl)
                    .setStorageBucket(bucketName)
                    .build();
            FirebaseApp.initializeApp(options);

            storage = StorageOptions.newBuilder().setProjectId(proId)
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(jsonFile)))
                    .build()
                    .getService();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<QueryDocumentSnapshot> getAllDocument() {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection(collectionName).get();
        List<QueryDocumentSnapshot> documents = null;
        try {
            QuerySnapshot querySnapshot = query.get();
            documents = querySnapshot.getDocuments();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return documents;
    }

    public List<QueryDocumentSnapshot> getAllDocument(String order) {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference reference = db.collection(collectionName);
        Query q = reference.orderBy(order);
        ApiFuture<QuerySnapshot> query = q.get();
        List<QueryDocumentSnapshot> documents = null;
        try {
            QuerySnapshot querySnapshot = query.get();
            documents = querySnapshot.getDocuments();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return documents;
    }

    public List<QueryDocumentSnapshot> getMultipleDocument1Bool(String field, boolean value) {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference reference = db.collection(collectionName);
        Query q = reference.whereEqualTo(field, value);
        List<QueryDocumentSnapshot> documents = getMultipleDocument(q);
        return documents;
    }

    public List<QueryDocumentSnapshot> getMultipleDocument2Str(String field1, String value1, String field2, String value2) {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference reference = db.collection(collectionName);
        Query q = reference.whereEqualTo(field1, value1).whereEqualTo(field2, value2);
        List<QueryDocumentSnapshot> documents = getMultipleDocument(q);
        return documents;
    }

    private List<QueryDocumentSnapshot> getMultipleDocument(Query q) {
        ApiFuture<QuerySnapshot> query = q.get();
        List<QueryDocumentSnapshot> documents = null;
        try {
            QuerySnapshot querySnapshot = query.get();
            documents = querySnapshot.getDocuments();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return documents;
    }

    public void updateDocument(String docName, Map<String, Object> docData) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(collectionName).document(docName);
        ApiFuture<WriteResult> writeResult = docRef.update(docData);
        try {
            System.out.println("Update document: " + writeResult.get().getUpdateTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void addDocument(Map<String, Object> docData) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference addDoc = db.collection(collectionName).document();
        ApiFuture<WriteResult> writeResult = addDoc.set(docData);
        try {
            System.out.println("Add document: " + writeResult.get().getUpdateTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void deleteDocument(String docName) {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = db.collection(collectionName).document(docName).delete();
        try {
            System.out.println("Delete document: " + writeResult.get().getUpdateTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public String uploadJpeg(String localPath, String blobName) {
        String ref = "";

        try {
            BlobId blobId = BlobId.of(bucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpeg").build();

            InputStream inputStream = new FileInputStream(new File(localPath));
            WriteChannel writer = storage.writer(blobInfo);
            byte [] buffer = new byte[1024];
            int limit;
            while ((limit = inputStream.read(buffer)) >= 0) {
                writer.write(ByteBuffer.wrap(buffer, 0, limit));
            }
            writer.close();

            URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature()) ;
            ref = url + "";
//            ref = "-";
            System.out.println("Upload image: " + ref);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ref;
    }

    public void deleteJpeg(String blobName) {
        BlobId blobId = BlobId.of(bucketName, blobName);
        boolean deleted = storage.delete(blobId);
        if (deleted) {
            System.out.println("Delete image: " + blobId.toString());
        }
    }

//    public String downloadJpeg(String localPath, String blobName) {
//        BlobId blobId = BlobId.of(bucketName, blobName);
//        String desPath = localPath + blobName;
//        storage.get(blobId).downloadTo(Paths.get(desPath));
//        System.out.println("Download image: " + desPath);
//
//        return desPath;
//    }

    public static void main(String[] args) {
        FirebaseConnection connection = new FirebaseConnection();
        connection.connect();

//        Upload Image
//        String blobName = "banners/banner3";
//        String localPath = "D:/banner3.jpg";
//
//        System.out.println(connection.uploadJpeg(localPath, blobName));
//        System.out.println("banner1 = https://storage.googleapis.com/my-project-c0f85.appspot.com/banners/banner1?X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Credential=firebase-adminsdk-bk5vm%40my-project-c0f85.iam.gserviceaccount.com%2F25630216%2Fauto%2Fstorage%2Fgoog4_request&X-Goog-Date=25630216T173817Z&X-Goog-Expires=60&X-Goog-SignedHeaders=host&X-Goog-Signature=2d264c37bc2ecc9777b1c96336cb2633149f62bd6a5bb43d543b9a25d6a3a1326c995ca2d161187f33cbc7e374aec1c8d980c3954238304c1f32165620bc3e82d4b26fb5922b5b16c09c3b0be41073941333f58b0999e102ccdfa0dc9419d9cde9a1964d229df746532f9bf4d9e46f8e9bc22981fab8c64d93c2b7637a8f05c84025fca6dc9e91ce475431b106c9bbc9dc951b607a8112c86c43b424942c7e702122caa7af750f575d24467e86b57d75c425096439cc914d0fda63cab87e3632afe4913da63a5d8825527b4ae3bafcb00e0e836aea1f8abd62f7297497c45cffb99f6f7d95b08e2a5bf9a44ebda234f16bbeff1f92542d6ae111cee40c4d8ecc");
//        System.out.println("banner2 = https://storage.googleapis.com/my-project-c0f85.appspot.com/banners/banner2?X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Credential=firebase-adminsdk-bk5vm%40my-project-c0f85.iam.gserviceaccount.com%2F25630216%2Fauto%2Fstorage%2Fgoog4_request&X-Goog-Date=25630216T174058Z&X-Goog-Expires=60&X-Goog-SignedHeaders=host&X-Goog-Signature=1cbdeedc04d0d23790c95c648e7d5a5923c16a5a60b9e51ce21300dd7c977ee1fd4ea3c9e926713a1b27917b4a16f29c4b8dd51bae0b1ce43ee7595b72c2ad1730d014677ef8f7c85e3d0cdc4cd76fa0a9e6313a47b70d6612c151a9c4902d6364c0b064548bc8467766abe813db65678e113d76d2c2b78645e7af5632233fdc722e3062213f54dbb6de504215602c5b660864d361404b8949fc40b3390862b109c696b52535e941060128b27c325eb25590b14fd55705d565dacdb917ad40d7cdf13b52506f00e08fa8e1091e97b7ccfef55bcdf690a6f31abd4929c5f15fc1d80999ce187ab1f747077a32a45a1f8ddf298de6e00bfc76815218446e54aa9a");

//        Download Image
//        connection.downloadJpeg("C:/Users/admin/Desktop/BoardGameProject/src/picture/", "banners/test");
    }
}
