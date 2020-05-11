package dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.firestore.EventListener;
import com.google.firebase.cloud.FirestoreClient;
import model.Booking;
import model.Promotion;
import model.Receipt;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MINUTES;

public class BookingDao implements Runnable {
    private Firestore db = FirestoreClient.getFirestore();
    private String collectionName = "BOOKING";

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private String code;
    private long milliSecLeft;
    private String status, gameId;

    public BookingDao() {}

    private BookingDao(String code, String gameId, Date expDate) {
        this.code = code;
        this.gameId = gameId;
        this.milliSecLeft = expDate.getTime() + TimeUnit.MILLISECONDS.convert(15, MINUTES) - new Date().getTime();;
    }

    @Override
    public void run() {
        if (milliSecLeft >= 0) {
            System.out.println("milliSecLeft: " + milliSecLeft);
            try {
                Thread.sleep(milliSecLeft);
                CollectionReference ref = db.collection(collectionName);
                ref.document(code).collection("customers").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirestoreException e) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            status = doc.getString("usage_status");
                            if (status.equals("จอง")) {
                                ref.document(code).collection("customers").document(doc.getId()).update("usage_status", "ยกเลิกการจอง ");
                                db.collection("customers").document(doc.getId()).update("booking_code", FieldValue.delete());
                                System.out.println("delete " + code + " " + Calendar.getInstance().getTime());
                            }
                        }

                        if (status.equals("จอง")) {
                            try {
                                DocumentSnapshot documentSnapshot = db.collection("games").document(gameId).get().get();
                                double available = documentSnapshot.getDouble("available");
                                db.collection("games").document(gameId).update("available", (int) (available + 1));
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            } catch (ExecutionException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();
    }

    public void deleteOldItemListener() {
        CollectionReference ref = db.collection(collectionName);
        Query query = ref.orderBy("booking_start", Query.Direction.DESCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirestoreException e) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.get("booking_end") == null) {
                        String gameId = doc.getString("game_id");
                        Date expDate = doc.getDate("booking_start");
                        Thread thread = new Thread(new BookingDao(doc.getId(), gameId, expDate));
                        executor.execute(thread);
                    }
                }
            }
        });
    }

    public List<Booking> getBooking(Date date) {
        List<Booking> bookingList = new ArrayList<>();

        CollectionReference reference = db.collection(collectionName);
        Date endDate = date;
        Calendar c = Calendar.getInstance();
        c.setTime(endDate);
        c.add(Calendar.DATE, 1);
        endDate = c.getTime();

        Query query = reference.orderBy("booking_start", Query.Direction.DESCENDING).startAt(endDate).endBefore(date);
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

        for (QueryDocumentSnapshot q : documents) {
            String bookingCode = q.getId();
            Date start = q.getDate("booking_start");
            Object tempEnd = q.get("booking_end");
            String gameId = q.getString("game_id");
            String group = q.getString("group");

            Date end = null;
            if (tempEnd != null) {
                end = q.getDate("booking_end");
            }

            ApiFuture<QuerySnapshot> snapshot2 = db.collection(collectionName + "/" + bookingCode + "/customers").get();
            List<QueryDocumentSnapshot> documents2 = null;
            try {
                QuerySnapshot querySnapshot = snapshot2.get();
                documents2 = querySnapshot.getDocuments();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            for (QueryDocumentSnapshot q2 : documents2) {
                String userId = q2.getId();
                String proId = q2.getString("promotion_id");
                String receipt = q2.getString("receipt_status");
                String usage = q2.getString("usage_status");

                bookingList.add(new Booking(bookingCode, end, start, gameId, group, userId, proId, receipt, usage));
            }
        }
        return bookingList;
    }

    public String getName(String collection, String id, String field) {
        String name = "";
        ApiFuture<DocumentSnapshot> snapshot = db.collection(collection).document(id).get();
        try {
            DocumentSnapshot querySnapshot = snapshot.get();
            name = querySnapshot.getString(field);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return name;
    }

    public void updateBooking(List<Booking> bookingList) {
        String bookingCode = bookingList.get(0).getBookingCode();
        Date end = bookingList.get(0).getBookingEnd();
        Date start = bookingList.get(0).getBookingStart();

        Map<String, Object> time = new HashMap<>();
        time.put("booking_end", end);
        time.put("booking_start", start);

        DocumentReference ref = db.collection(collectionName).document(bookingCode);
        ref.update(time);

        for (Booking booking : bookingList) {
            Map<String, Object> data = new HashMap<>();
            data.put("usage_status", booking.getUsageStatus());
            ref.collection("customers").document(booking.getUserId()).update(data);
        }

        if (bookingList.get(0).getUsageStatus().equals("เสร็จสิ้นการจอง")) {
            ApiFuture<DocumentSnapshot> snapshot = db.collection("games").document(bookingList.get(0).getGameId()).get();
            try {
                DocumentSnapshot querySnapshot = snapshot.get();
                double available = querySnapshot.getDouble("available");
                db.collection("games").document(bookingList.get(0).getGameId()).update("available", (int) (available + 1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateCustomerPoint(String userId, int point) {
        ApiFuture<DocumentSnapshot> snapshot = db.collection("customers").document(userId).get();
        try {
            DocumentSnapshot querySnapshot = snapshot.get();
            double latestPoint = querySnapshot.getDouble("point");
            Map<String, Object> data = new HashMap<>();
            data.put("point", (int) (latestPoint + point));
            data.put("last_active_point", new Date());
            db.collection("customers").document(userId).update(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void cancelBooking(Booking booking) {
        Map<String, Object> data = new HashMap<>();
        data.put("usage_status", booking.getUsageStatus());
        data.put("receipt_status", booking.getReceiptStatus());

        DocumentReference ref = db.collection(collectionName).document(booking.getBookingCode());
        ref.collection("customers").document(booking.getUserId()).update(data);

        // Update booking_code in customers document
        db.collection("customers").document(booking.getUserId()).update("booking_code", FieldValue.delete());
    }

    public Promotion getPromotion(String proId) {
        Promotion pro = null;
        ApiFuture<DocumentSnapshot> snapshot = db.collection("promotions").document(proId).get();
        try {
            DocumentSnapshot querySnapshot = snapshot.get();
            String name = querySnapshot.getString("pro_name");
            String detail = querySnapshot.getString("pro_detail");
            Date start = querySnapshot.getDate("pro_start");
            Date end = querySnapshot.getDate("pro_end");
            double point = querySnapshot.getDouble("pro_point");
            String type = querySnapshot.getString("pro_type");
            pro = new Promotion(proId, name, detail, start, end, (int) point, type);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return pro;
    }

    public int getReceiptSize() {
        List<Receipt> receipts = new ArrayList<>();

        ApiFuture<QuerySnapshot> query = db.collection("receipts").get();
        List<QueryDocumentSnapshot> documents = null;
        try {
            QuerySnapshot querySnapshot = query.get();
            documents = querySnapshot.getDocuments();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return documents.size();
    }

    public List<Receipt> getReceiptInDay(Date date) {
        List<Receipt> receipts = new ArrayList<>();

        CollectionReference reference = db.collection("receipts");
        Date endDate = date;
        Calendar c = Calendar.getInstance();
        c.setTime(endDate);
        c.add(Calendar.DATE, 1);
        endDate = c.getTime();

        Query query = reference.orderBy("date").startAt(date).endBefore(endDate);
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

        if (!documents.isEmpty()) {
            for (QueryDocumentSnapshot q : documents) {
                String id = q.getId();
                Date d = q.getDate("date");
                String user = q.getString("username");
                String game = q.getString("game");
                String pro = q.getString("promotion");
                double price = q.getDouble("price");
                double discount = q.getDouble("discount");
                double amount = q.getDouble("amount");
                double hour = q.getDouble("hour");
                double point = q.getDouble("point");
                String code = q.getString("booking_code");
                Date start = q.getDate("booking_start");
                Date end = q.getDate("booking_end");
                receipts.add(new Receipt(id, d, user, game, pro, price, discount, amount, (int) hour, (int) point, code, start, end));
            }
        }
        return receipts;
    }

    public void addReceipt(Receipt receipt) {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", receipt.getAmount());
        data.put("date", receipt.getDate());
        data.put("discount", receipt.getDiscount());
        data.put("game", receipt.getGame());
        data.put("point", receipt.getPoint());
        data.put("price", receipt.getPrice());
        data.put("promotion", receipt.getPromotion());
        data.put("username", receipt.getUser());
        data.put("hour", receipt.getHour());
        data.put("booking_code", receipt.getBookingCode());
        data.put("booking_start", receipt.getStart());
        data.put("booking_end", receipt.getEnd());

        DocumentReference addDoc = db.collection("receipts").document(receipt.getId());
        ApiFuture<WriteResult> writeResult = addDoc.set(data);
        if (writeResult.isDone()) {
            System.out.println("Add receipt");
        }
    }
}
