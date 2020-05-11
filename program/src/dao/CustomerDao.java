package dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import model.Customer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CustomerDao {
    private CollectionReference ref = FirestoreClient.getFirestore().collection("customers");

    public List<Customer> getAll() {
        List<Customer> customers = new ArrayList<>();

        Query query = ref.orderBy("last_active_point", Query.Direction.DESCENDING);
        ApiFuture<QuerySnapshot> snapshot = query.get();
        List<QueryDocumentSnapshot> documents = null;
        try {
            QuerySnapshot querySnapshot = snapshot.get();
            documents = querySnapshot.getDocuments();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        for (QueryDocumentSnapshot document : documents) {
            String id = document.getId();
            String user = document.getString("cus_username");
            String email = document.getString("email");
            double p = document.getDouble("point");
            int point = (int) p;
            Date lastActive = document.getDate("last_active_point");
            customers.add(new Customer(id, user, email, point, lastActive));
        }
        return customers;
    }
}
