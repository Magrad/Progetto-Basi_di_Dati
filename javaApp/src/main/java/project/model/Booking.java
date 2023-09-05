package project.model;

import java.util.Date;
import java.util.Objects;

/***
 * Class representing a booking with simple fields: id,
 * day, TimeSlot:id, User:cf, Employee:cf, feedback.
 * The corresponding database table is:
 *    CREATE TABLE Booking (
 *       id INT NOT NULL,
 *       day DATE NOT NULL,
 *       timeslot_id INT NOT NULL,
 *       user_cf varchar(25) NOT NULL,
 *       employee_cf varchar(25) NOT NULL,
 *       feedback varchar(25),
 *       PRIMARY KEY (day, timeslot_id, user_cf, employee_cf)
 *       FOREIGN KEY (timeslot_id) REFERENCES TimeSlot(id),
 *       FOREIGN KEY (user_cf) REFERENCES User(cf),
 *       FOREIGN KEY (employee_cf) REFERENCES Employee(cf)
 *       UNIQUE KEY booking_id (id)
 *    )
 */
public class Booking {
    private final Integer id;
    private final Date day;
    private final Integer timeslot_id;
    private final String user_cf;
    private final String employee_cf;
    private final String feedback;

    public Booking(Integer id, Date day, Integer timeslot_id, String user_cf, String employee_cf, String feedback) {
        this.id = id;
        this.day = day;
        this.timeslot_id = timeslot_id;
        this.user_cf = user_cf;
        this.employee_cf = employee_cf;
        this.feedback = feedback;
    }

    public Integer getId() {
        return id;
    }

    public Date getDay() {
        return day;
    }

    public Integer getTimeslot_id() {
        return timeslot_id;
    }

    public String getUser_cf() {
        return user_cf;
    }

    public String getEmployee_cf() {
        return employee_cf;
    }

    public String getFeedback() {
        return feedback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return Objects.equals(getId(), booking.getId()) && Objects.equals(getDay(), booking.getDay()) && Objects.equals(getTimeslot_id(), booking.getTimeslot_id()) && Objects.equals(getUser_cf(), booking.getUser_cf()) && Objects.equals(getEmployee_cf(), booking.getEmployee_cf()) && Objects.equals(getFeedback(), booking.getFeedback());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDay(), getTimeslot_id(), getUser_cf(), getEmployee_cf(), getFeedback());
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", day=" + day +
                ", timeslotid=" + timeslot_id +
                ", usercf='" + user_cf + '\'' +
                ", employeecf='" + employee_cf + '\'' +
                ", feedback='" + feedback + '\'' +
                '}';
    }
}
