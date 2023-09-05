package project.model;

import java.util.Objects;

/***
 * Class representing an agenda with simple fields: day,
 * Employee:cf, Ambulatory:id, daystart, breakstart, breakend,
 * dayend, deltatime.
 * The corresponding database table is:
 *    CREATE TABLE Agenda (
 *      day varchar(25) NOT NULL,
 *      employee_cf varchar(25) NOT NULL,
 *      ambulatory_id int NOT NULL,
 *      daystart time NOT NULL,
 *      breakstart time NOT NULL,
 *      breakend time NOT NULL,
 *      dayend time NOT NULL,
 *      deltatime time NOT NULL,
 *      PRIMARY KEY (day, employee_cf),
 *      FOREIGN KEY (employee_cf) REFERENCES Employee(cf),
 *      FOREIGN KEY (ambulatory_id) REFERENCES Ambulatory(id),
 *      CHECK (daystart < breakstart AND breakstart < breakend AND breakend < dayend)
 *    )
 */
public class Agenda {
    private final String day;
    private final String employee_cf;
    private final Integer ambulatory_id;
    private final String daystart;
    private final String breakstart;
    private final String breakend;
    private final String dayend;
    private final String deltatime;

    public Agenda(String day, String employee_cf, Integer ambulatory_id, String daystart, String breakstart, String breakend, String dayend, String deltatime) {
        this.day = day;
        this.employee_cf = employee_cf;
        this.ambulatory_id = ambulatory_id;
        this.daystart = daystart;
        this.breakstart = breakstart;
        this.breakend = breakend;
        this.dayend = dayend;
        this.deltatime = deltatime;
    }

    public String getDay() {
        return day;
    }

    public String getEmployee_cf() {
        return employee_cf;
    }

    public Integer getAmbulatory_id() {
        return ambulatory_id;
    }

    public String getDaystart() {
        return daystart;
    }

    public String getBreakstart() {
        return breakstart;
    }

    public String getBreakend() {
        return breakend;
    }

    public String getDayend() {
        return dayend;
    }

    public String getDeltatime() {
        return deltatime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agenda)) return false;
        Agenda agenda = (Agenda) o;
        return Objects.equals(getDay(), agenda.getDay()) && Objects.equals(getEmployee_cf(), agenda.getEmployee_cf()) && Objects.equals(getAmbulatory_id(), agenda.getAmbulatory_id()) && Objects.equals(getDaystart(), agenda.getDaystart()) && Objects.equals(getBreakstart(), agenda.getBreakstart()) && Objects.equals(getBreakend(), agenda.getBreakend()) && Objects.equals(getDayend(), agenda.getDayend()) && Objects.equals(getDeltatime(), agenda.getDeltatime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDay(), getEmployee_cf(), getAmbulatory_id(), getDaystart(), getBreakstart(), getBreakend(), getDayend(), getDeltatime());
    }

    @Override
    public String toString() {
        return "Agenda{" +
                "day=" + day +
                ", employee_cf='" + employee_cf + '\'' +
                ", ambulatory_id=" + ambulatory_id +
                ", daystart='" + daystart + '\'' +
                ", breakstart='" + breakstart + '\'' +
                ", breakend='" + breakend + '\'' +
                ", dayend='" + dayend + '\'' +
                ", deltatime='" + deltatime + '\'' +
                '}';
    }
}
