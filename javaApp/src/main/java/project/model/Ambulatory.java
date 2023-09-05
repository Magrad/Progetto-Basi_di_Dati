package project.model;

import java.util.Objects;

/***
 * Class representing an ambulatory with simple fields:
 * id, room_number, Ward:id.
 * The corresponding database table is:
 *    CREATE TABLE Ambulatory (
 *       id int NOT NULL,
 *       room_number int NOT NULL,
 *       ward_id int NOT NULL,
 *       PRIMARY KEY (room_number, ward_id),
 *       FOREIGN KEY (ward_id) REFERENCES Ward(id)
 *       UNIQUE KEY ambulatory_id (id)
 *    )
 */
public class Ambulatory {
    private final Integer id;
    private final Integer room_number;
    private final Integer ward_id;

    public Ambulatory(Integer id, Integer room_number, Integer ward_id) {
        this.id = id;
        this.room_number = room_number;
        this.ward_id = ward_id;
    }

    public Integer getId() {
        return id;
    }

    public Integer getRoom_number() {
        return room_number;
    }

    public Integer getWard_id() {
        return ward_id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ambulatory)) return false;
        Ambulatory that = (Ambulatory) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getRoom_number(), that.getRoom_number()) && Objects.equals(getWard_id(), that.getWard_id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getRoom_number(), getWard_id());
    }

    @Override
    public String toString() {
        return "Ambulatory{" +
                "id=" + id +
                ", room_number=" + room_number +
                ", ward_id=" + ward_id +
                '}';
    }
}
