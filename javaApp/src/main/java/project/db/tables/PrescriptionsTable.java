package project.db.tables;

import project.db.Table;
import project.model.Prescription;
import project.utils.GUIUtils;
import project.utils.Pair;

import javax.swing.text.html.Option;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
 *       PRIMARY KEY (id, medicalreport_id),
 *       FOREIGN KEY (medicalreport_id,user_cf) REFERENCES MedicalReport(id,user_cf),
 *       FOREIGN KEY (treatment_id) REFERENCES Treatment(id)
 *    )
 */
public class PrescriptionsTable implements Table<Prescription, Pair<Integer,Integer>> {

    public static final String TABLE_NAME = "Prescription";
    private final Connection connection;

    public PrescriptionsTable(final Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public boolean createTable() {
        try (final Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE " + TABLE_NAME + " (" +
                            "id INT NOT NULL," +
                            "medicalreport_id INT NOT NULL," +
                            "user_cf varchar(16) NOT NULL," +
                            "treatment_id INT," +
                            "quantity INT," +
                            "followup varchar(255)," +
                            "PRIMARY KEY (id, medicalreport_id)," +
                            "FOREIGN KEY (medicalreport_id) REFERENCES " + MedicalReportsTable.TABLE_NAME + "(id)," +
                            "FOREIGN KEY (treatment_id) REFERENCES " + TreatmentsTable.TABLE_NAME + "(id)" +
                            ")");
            return true;
        } catch (final SQLException e) {
            return false;
        }
    }

    @Override
    public boolean dropTable() {
        try (final Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("DROP TABLE " + TABLE_NAME);
            return true;
        } catch (final SQLException e) {
            return false;
        }
    }

    @Override
    public boolean insertToTable(Prescription value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (id, medicalreport_id, user_cf, treatment_id, quantity, followup)" +
                " VALUES (?, ?, ?, ?, ?, ?)";
        try (final var preparedStatement = this.connection.prepareStatement(query)) {
            preparedStatement.setInt(1, findMaxID(value.getMedicalreport_id()));
            preparedStatement.setInt(2, value.getMedicalreport_id());
            preparedStatement.setString(3, value.getUser_cf());
            preparedStatement.setObject(4, value.getTreatment_id().orElse(null));
            preparedStatement.setObject(5, value.getQuantity().orElse(null));
            preparedStatement.setString(6, value.getFollowup().orElse(null));
            preparedStatement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new prescription parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new prescription insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(Prescription value, Pair<Integer, Integer> primaryKey) {
        final Integer id = primaryKey.getX();
        final Integer medicalreport_id = primaryKey.getY();

        final String query = "UPDATE " + TABLE_NAME +
                " SET id=?, medicalreport_id=?, user_cf=?, treatment_id=?, quantity=?, followup=?" +
                " WHERE id=? AND medicalreport_id=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, findMaxID(value.getMedicalreport_id()));
            statement.setInt(2, value.getMedicalreport_id());
            statement.setString(3, value.getUser_cf());
            statement.setObject(3, value.getTreatment_id().orElse(null));
            statement.setObject(4, value.getQuantity().orElse(null));
            statement.setString(5, value.getFollowup().orElse(null));
            statement.setInt(6, id);
            statement.setInt(7, medicalreport_id);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
            return false;
        }
    }

    @Override
    public boolean deleteFromTable(Pair<Integer, Integer> primaryKey) {
        final int id = primaryKey.getX();
        final int medicalreport_id = primaryKey.getY();

        final String query = "DELETE FROM " + TABLE_NAME + " WHERE id=? AND medicalreport_id=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setInt(2, medicalreport_id);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
            return false;
        }
    }

    @Override
    public List<Prescription> findResultSet(ResultSet result) {
        final List<Prescription> prescriptions = new ArrayList<>();
        try {
            while (result.next()) {
                final int id = result.getInt("id");
                final int medicalreport_id = result.getInt("medicalreport_id");
                final String user_cf = result.getString("user_cf");
                final Optional<Integer> treatment_id = Optional.ofNullable(result.getInt("treatment_id"));
                final Optional<Integer> quantity = Optional.ofNullable(result.getInt("quantity"));
                final Optional<String> followup = Optional.ofNullable(result.getString("followup"));
                final Prescription prescription = new Prescription(id, medicalreport_id, user_cf, treatment_id, quantity, followup);
                prescriptions.add(prescription);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL result set error", e);
        }
        return prescriptions;
    }

    @Override
    public List<Prescription> findAll() {
        List<Prescription> prescriptions = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            prescriptions = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return prescriptions;
    }

    @Override
    public Optional<Prescription> findByPrimaryKey(Pair<Integer, Integer> primaryKey) {
        final Integer id = primaryKey.getX();
        final Integer medicalreport_id = primaryKey.getY();

        Optional<Prescription> prescription = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id=? AND medicalreport_id=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setInt(2, medicalreport_id);
            final ResultSet resultSet = statement.executeQuery();
            final List<Prescription> prescriptions = findResultSet(resultSet);
            if (!prescriptions.isEmpty()) {
                prescription = Optional.of(prescriptions.get(0));
            }
        } catch (final SQLException e) { }
        return prescription;
    }

    /**
     * @param medicalreport_id the medical report id.
     * @return the list of prescriptions with the given medical report id.
     */
    public List<Prescription> findPrescriptionByMedicalReportID(Integer medicalreport_id) {
        List<Prescription> prescriptions = new ArrayList<>();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE medicalreport_id=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, medicalreport_id);
            final ResultSet resultSet = statement.executeQuery();
            prescriptions = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return prescriptions;
    }

    /**
     * @param medicalreport_id the medical report id.
     * @return the max id of prescriptions and increment it by 1.
     */
    public Integer findMaxID(Integer medicalreport_id) {
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE medicalreport_id=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, medicalreport_id);
            final ResultSet resultSet = statement.executeQuery();
            return findResultSet(resultSet).size() + 1;
        } catch (final SQLException e) { }
        return 1;
    }
}
