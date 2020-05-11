package dao;

import main.Main;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import model.Employee;

import java.util.*;

public class EmployeeDao {

    private FirebaseConnection connection = Main.connection;
    private String collectionName = "employees";
    private HashSet<String> allUsername = new HashSet<>();
    private Employee employee;

    public EmployeeDao() {
        connection.setCollectionName(collectionName);
        List<QueryDocumentSnapshot> documents = connection.getAllDocument();
        for (QueryDocumentSnapshot document : documents) {
            allUsername.add(document.getString("emp_username"));
        }
    }

    public boolean check(String username, String password) {
        List<QueryDocumentSnapshot> documents =
                connection.getMultipleDocument2Str("emp_username", username, "emp_password", password);
        QueryDocumentSnapshot document;
        if (!documents.isEmpty()) {
            document = documents.get(0);
            String pwd = document.getString("emp_password");
            if (pwd.equals(password)) {
                String id = document.getId();
                String first = document.getString("first_name");
                String last = document.getString("last_name");
                employee = new Employee(id, username, password, first, last);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void update(Employee emp) {
        employee = emp;
        String id = employee.getEmpId();
        Map<String, Object> data = new HashMap<>();
        data.put("emp_username", employee.getEmpUsername());
        data.put("emp_password", employee.getPassword());
        data.put("first_name", employee.getFirstName());
        data.put("last_name", employee.getLastName());
        connection.updateDocument(id, data);
    }

    public Employee getInformation() {
        return employee;
    }

    public HashSet<String> getAllUsername () {
        return allUsername;
    }
}
