package project.model;

import java.util.Date;
import java.util.Objects;

/***
 * Class representing a medical report with simple fields: id, User:cf,
 * Employee:cf, day, description, diagnosis.
 * The corresponding database table is:
 *    CREATE TABLE MedicalReport (
 *       id int NOT NULL AUTO_INCREMENT,
 *       user_cf varchar(25) NOT NULL,
 *       employee_cf varchar(25) NOT NULL,
 *       day date NOT NULL,
 *       description varchar(255) NOT NULL,
 *       diagnosis varchar(255) NOT NULL,
 *       PRIMARY KEY (id, user_cf)
 *       FOREIGN KEY (user_cf) REFERENCES User(cf),
 *       FOREIGN KEY (employee_cf) REFERENCES Employee(cf)
 *    )
 */
public class MedicalReport {
    private final Integer id;
    private final String user_cf;
    private final String employee_cf;
    private final Date day;
    private final String description;
    private final String diagnosis;

    public MedicalReport(Integer id, String user_cf, String employee_cf, Date day, String description, String diagnosis) {
        this.id = id;
        this.user_cf = user_cf;
        this.employee_cf = employee_cf;
        this.day = day;
        this.description = description;
        this.diagnosis = diagnosis;
    }

    public Integer getId() {
        return id;
    }

    public String getUser_cf() {
        return user_cf;
    }

    public String getEmployee_cf() {
        return employee_cf;
    }

    public Date getDay() {
        return day;
    }

    public String getDescription() {
        return description;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalReport)) return false;
        MedicalReport that = (MedicalReport) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getUser_cf(), that.getUser_cf()) && Objects.equals(getEmployee_cf(), that.getEmployee_cf()) && Objects.equals(getDay(), that.getDay()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getDiagnosis(), that.getDiagnosis());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser_cf(), getEmployee_cf(), getDay(), getDescription(), getDiagnosis());
    }

    @Override
    public String toString() {
        return "MedicalReport{" +
                "id=" + id +
                ", usercf='" + user_cf + '\'' +
                ", employeecf='" + employee_cf + '\'' +
                ", day='" + day + '\'' +
                ", description='" + description + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                '}';
    }
}
