package project.model;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/***
 * Class representing an employee with simple fields: CF, name, surname,
 * birthday, gender, role, phone, email, password and permissions.
 * The corresponding database table is:
 *    CREATE TABLE Employee (
 *       cf varchar(25) NOT NULL PRIMARY KEY,
 *       name varchar(25) NOT NULL,
 *       surname varchar(25) NOT NULL,
 *       birthday date NOT NULL,
 *       gender char(1) NOT NULL,
 *       role_id int NOT NULL FOREIGN KEY,
 *       phone varchar(50),
 *       email varchar(50) NOT NULL UNIQUE,
 *       password varchar(255) NOT NULL,
 *       permissions int NOT NULL,
 *       FOREIGN KEY (role_id) REFERENCES Position(id)
 *    )
 */
public class Employee {
    private final String CF;
    private final String name;
    private final String surname;
    private final Date birthday;
    private final Character gender;
    private final Integer role_id;
    private Optional<String> phone;
    private final String email;
    private final String password;
    private final int permissions;

    public Employee(String CF, String name, String surname, Date birthday, Character gender, Integer role_id, Optional<String> phone, String email, String password, int permissions) {
        this.CF = CF;
        this.name = name;
        this.surname = surname;
        this.birthday = birthday;
        this.gender = gender;
        this.role_id = role_id;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.permissions = permissions;
    }

    public Employee(String CF, String name, String surname, Date birthday, Character gender, Integer role_id, String email, String password, int permissions) {
        this.CF = CF;
        this.name = name;
        this.surname = surname;
        this.birthday = birthday;
        this.gender = gender;
        this.role_id = role_id;
        this.email = email;
        this.password = password;
        this.permissions = permissions;
    }

    public String getCF() {
        return CF;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public Date getBirthday() {
        return birthday;
    }

    public Character getGender() {
        return gender;
    }

    public Integer getRole_id() {
        return role_id;
    }

    public Optional<String> getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public int getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employees = (Employee) o;
        return getGender() == employees.getGender() && getPermissions() == employees.getPermissions() && Objects.equals(getCF(), employees.getCF()) && Objects.equals(getName(), employees.getName()) && Objects.equals(getSurname(), employees.getSurname()) && Objects.equals(getBirthday(), employees.getBirthday()) && Objects.equals(getRole_id(), employees.getRole_id()) && Objects.equals(getPhone(), employees.getPhone()) && Objects.equals(getEmail(), employees.getEmail()) && Objects.equals(getPassword(), employees.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCF(), getName(), getSurname(), getBirthday(), getGender(), getRole_id(), getPhone(), getEmail(), getPassword(), getPermissions());
    }

    @Override
    public String toString() {
        return "Employees{" +
                "CF='" + CF + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", birthday=" + birthday +
                ", gender=" + gender +
                ", role=" + role_id +
                ", phone=" + phone +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
