package project.model;

import java.util.Date;
import java.util.Objects;

public class Person {
    private final String CF;
    private final String name;
    private final String surname;
    private final Date birthday;
    private final char gender;

    public Person(String CF, String name, String surname, Date birthday, char gender) {
        this.CF = CF;
        this.name = name;
        this.surname = surname;
        this.birthday = birthday;
        this.gender = gender;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return gender == person.gender && Objects.equals(CF, person.CF) && Objects.equals(name, person.name) && Objects.equals(surname, person.surname) && Objects.equals(birthday, person.birthday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CF, name, surname, birthday, gender);
    }

    @Override
    public String toString() {
        return "Person{" +
                "CF='" + CF + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", birthday=" + birthday +
                ", gender=" + gender +
                '}';
    }
}
