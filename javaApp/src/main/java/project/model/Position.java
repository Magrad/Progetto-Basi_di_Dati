package project.model;

import java.util.Objects;
import java.util.Optional;

/***
 * Class representing a position with simple fields: id, role
 * and specialization
 * The corresponding database table is:
 *    CREATE TABLE Position (
 *       id int AUTO_INCREMENT PRIMARY KEY,
 *       role varchar(25) NOT NULL,
 *       specialization varchar(25) NOT NULL,
 *       UNIQUE KEY (role, specialization)
 *    )
 */
public class Position {
    private final Integer id;
    private final String role;
    private final String specialization;

    public Position(Integer id, String role, String specialization) {
        this.id = id;
        this.role = role;
        this.specialization = specialization;
    }

    public Integer getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getSpecialization() {
        return specialization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return Objects.equals(getId(), position.getId()) && Objects.equals(getRole(), position.getRole()) && Objects.equals(getSpecialization(), position.getSpecialization());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getRole(), getSpecialization());
    }

    @Override
    public String toString() {
        return role + (specialization.equals("") ? "" : " - " + specialization);
    }
}
