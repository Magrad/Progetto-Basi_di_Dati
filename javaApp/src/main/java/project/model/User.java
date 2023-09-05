package project.model;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/***
 * Class representing a user with simple fields: CF, name, surname,
 * birthday, gender, phone, email and password.
 * The corresponding database table is:
 *    CREATE TABLE User (
 *       cf varchar(25) NOT NULL PRIMARY KEY,
 *       name varchar(25) NOT NULL,
 *       surname varchar(25) NOT NULL,
 *       birthday date NOT NULL,
 *       gender char(1) NOT NULL,
 *       phone varchar(50),
 *       email varchar(50) NOT NULL UNIQUE,
 *       password varchar(255) NOT NULL
 *    )
 */
public class User {
    private final String CF;
    private final String name;
    private final String surname;
    private final Date birthday;
    private final char gender;
    private Optional<String> phone;
    private String email;
    private String password;

    public User(String CF, String name, String surname, Date birthday, char gender, Optional<String> phone, String email, String password) {
        this.CF = CF;
        this.name = name;
        this.surname = surname;
        this.birthday = birthday;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    public User(String CF, String name, String surname, Date birthday, char gender, String email, String password) {
        this.CF = CF;
        this.name = name;
        this.surname = surname;
        this.birthday = birthday;
        this.gender = gender;
        this.email = email;
        this.password = password;
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

    public char getGender() {
        return gender;
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

    public void setPhone(Optional<String> phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getGender() == user.getGender() && Objects.equals(getCF(), user.getCF()) && Objects.equals(getName(), user.getName()) && Objects.equals(getSurname(), user.getSurname()) && Objects.equals(getBirthday(), user.getBirthday()) && Objects.equals(getPhone(), user.getPhone()) && Objects.equals(getEmail(), user.getEmail()) && Objects.equals(getPassword(), user.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCF(), getName(), getSurname(), getBirthday(), getGender(), getPhone(), getEmail(), getPassword());
    }

    @Override
    public String toString() {
        return "User{" +
                "CF='" + CF + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", birthday=" + birthday +
                ", gender=" + gender +
                ", phone=" + phone +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
