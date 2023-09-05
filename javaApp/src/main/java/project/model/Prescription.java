package project.model;

import java.util.Objects;
import java.util.Optional;

/***
 * Class representing a prescription with simple fields: id,
 * MedicalReport:id, MedicalReport:user_cf, Treatment:id, quantity, followup.
 * The corresponding database table is:
 *    CREATE TABLE Prescription (
 *       id INT NOT NULL AUTO_INCREMENT,
 *       medicalreport_id INT NOT NULL,
 *       user_cf varchar(25) NOT NULL,
 *       treatment_id INT NOT NULL,
 *       quantity INT NOT NULL,
 *       followup varchar(255),
 *       PRIMARY KEY (id, medicalreport_id)
 *       FOREIGN KEY (medicalreport_id,user_cf) REFERENCES MedicalReport(id,user_cf)
 *       FOREIGN KEY (treatment_id) REFERENCES Treatment(id)
 *    )
 */
public class Prescription {
    private final Integer id;
    private final Integer medicalreport_id;
    private final String user_cf;
    private Optional<Integer> treatment_id;
    private Optional<Integer> quantity;
    private Optional<String> followup;

    public Prescription(Integer id, Integer medicalreport_id, String user_cf, Optional<Integer> treatment_id, Optional<Integer> quantity, Optional<String> followup) {
        this.id = id;
        this.medicalreport_id = medicalreport_id;
        this.user_cf = user_cf;
        this.treatment_id = treatment_id;
        this.quantity = quantity;
        this.followup = followup;
    }

    public Prescription(Integer id, Integer medicalreport_id, String user_cf) {
        this.id = id;
        this.medicalreport_id = medicalreport_id;
        this.user_cf = user_cf;
    }

    public Integer getId() {
        return id;
    }

    public Integer getMedicalreport_id() {
        return medicalreport_id;
    }

    public String getUser_cf() {
        return user_cf;
    }

    public Optional<Integer> getTreatment_id() {
        return treatment_id;
    }

    public Optional<Integer> getQuantity() {
        return quantity;
    }

    public Optional<String> getFollowup() {
        return followup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prescription)) return false;
        Prescription that = (Prescription) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getMedicalreport_id(), that.getMedicalreport_id()) && Objects.equals(getUser_cf(), that.getUser_cf()) && Objects.equals(getTreatment_id(), that.getTreatment_id()) && Objects.equals(getQuantity(), that.getQuantity()) && Objects.equals(getFollowup(), that.getFollowup());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMedicalreport_id(), getUser_cf(), getTreatment_id(), getQuantity(), getFollowup());
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "id=" + id +
                ", medicalreport_id=" + medicalreport_id +
                ", user_cf='" + user_cf + '\'' +
                ", treatment_id=" + treatment_id +
                ", quantity=" + quantity +
                ", followup=" + followup +
                '}';
    }
}
