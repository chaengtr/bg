package model;

public class Employee {

    private String empId;
    private String empUsername;
    private String password;
    private String firstName;
    private String lastName;

    public Employee(String empId, String empUsername, String password, String firstName, String lastName) {
        this.empId = empId;
        this.empUsername = empUsername;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmpId() {
        return empId;
    }

    public String getEmpUsername() {
        return empUsername;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setEmpUsername(String empUsername) {
        this.empUsername = empUsername;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
