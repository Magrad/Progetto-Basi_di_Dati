package project.model;

import java.util.Date;
import java.util.Objects;

/***
 * Class representing a time slot with simple fields: id,
 * Agenda:id, slot, status.
 * The corresponding database table is:
 *    CREATE TABLE Timeslot (
 *       id int NOT NULL,
 *       day date NOT NULL,
 *       agenda_day varchar(25) NOT NULL,
 *       agenda_employee_cf varchar(25) NOT NULL,
 *       slot time NOT NULL,
 *       available bit NOT NULL,
 *       PRIMARY KEY (day, slot)
 *       FOREIGN KEY (agenda_day, agenda_employee_cf) REFERENCES Agenda(day, employee_cf)
 *       UNIQUE KEY timeslot_id (id)
 *    )
 */
public class TimeSlot {
    private final Integer id;
    private final Date day;
    private final String agenda_day;
    private final String agenda_employee_cf;
    private final String slot;
    private final Boolean available;

    public TimeSlot(Integer id, Date day, String agenda_day, String agenda_employee_cf, String slot, Boolean available) {
        this.id = id;
        this.day = day;
        this.agenda_day = agenda_day;
        this.agenda_employee_cf = agenda_employee_cf;
        this.slot = slot;
        this.available = available;
    }

    public Integer getId() {
        return id;
    }

    public Date getDay() {
        return day;
    }

    public String getAgenda_day() {
        return agenda_day;
    }

    public String getAgenda_employee_cf() {
        return agenda_employee_cf;
    }

    public String getSlot() {
        return slot;
    }

    public Boolean getStatus() {
        return available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot)) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(getId(), timeSlot.getId()) && Objects.equals(getDay(), timeSlot.getDay()) && Objects.equals(getAgenda_day(), timeSlot.getAgenda_day()) && Objects.equals(getAgenda_employee_cf(), timeSlot.getAgenda_employee_cf()) && Objects.equals(getSlot(), timeSlot.getSlot()) && Objects.equals(getStatus(), timeSlot.getStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDay(), getAgenda_day(), getAgenda_employee_cf(), getSlot(), getStatus());
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "id=" + id +
                ", day=" + day +
                ", agenda_day='" + agenda_day + '\'' +
                ", agenda_employee_cf='" + agenda_employee_cf + '\'' +
                ", slot='" + slot + '\'' +
                ", available=" + available +
                '}';
    }
}
