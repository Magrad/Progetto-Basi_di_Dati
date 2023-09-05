package project.db.tables;

import project.db.Table;
import project.model.MedicalReport;
import project.utils.GUIUtils;
import project.utils.Pair;
import project.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MedicalReportsTable implements Table<MedicalReport, Pair<Integer,String>> {

    public static final String TABLE_NAME = "Medical_Report";
    private final Connection connection;

    public MedicalReportsTable(final Connection connection) {
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
                            "id int NOT NULL," +
                            "user_cf varchar(16) NOT NULL," +
                            "employee_cf varchar(16) NOT NULL," +
                            "day date NOT NULL," +
                            "description varchar(255) NOT NULL," +
                            "diagnosis varchar(255) NOT NULL," +
                            "PRIMARY KEY (id, user_cf)," +
                            "FOREIGN KEY (user_cf) REFERENCES " + UsersTable.TABLE_NAME + "(cf)," +
                            "FOREIGN KEY (employee_cf) REFERENCES " + EmployeesTable.TABLE_NAME + "(cf)" +
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
    public boolean insertToTable(MedicalReport value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (id, user_cf, employee_cf, day, description, diagnosis)" +
                " VALUES (?,?,?,?,?,?)";
        try (final var preparedStatement = this.connection.prepareStatement(query)) {
            preparedStatement.setInt(1, findUserMaxID(value.getUser_cf()));
            preparedStatement.setString(2, value.getUser_cf());
            preparedStatement.setString(3, value.getEmployee_cf());
            preparedStatement.setDate(4, Utils.dateToSqlDate(value.getDay()));
            preparedStatement.setString(5, value.getDescription());
            preparedStatement.setString(6, value.getDiagnosis());
            preparedStatement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new medical report parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new medical report insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(MedicalReport value, Pair<Integer, String> primaryKey) {
        final Integer id = primaryKey.getX();
        final String user_cf = primaryKey.getY();

        final String query = "UPDATE " + TABLE_NAME +
                " SET id=?, user_cf=?, employee_cf=?, day=?, description=?, diagnosis=?" +
                " WHERE id=? AND user_cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, value.getId());
            statement.setString(2, value.getUser_cf());
            statement.setString(3, value.getEmployee_cf());
            statement.setDate(4, Utils.dateToSqlDate(value.getDay()));
            statement.setString(5, value.getDescription());
            statement.setString(6, value.getDiagnosis());
            statement.setInt(7, id);
            statement.setString(8, user_cf);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
            return false;
        }
    }

    @Override
    public boolean deleteFromTable(Pair<Integer, String> primaryKey) {
        final int id = primaryKey.getX();
        final String user_cf = primaryKey.getY();

        final String query = "DELETE FROM " + TABLE_NAME + " WHERE id=? AND user_cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, user_cf);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
            return false;
        }
    }

    @Override
    public List<MedicalReport> findResultSet(ResultSet result) {
        final List<MedicalReport> medicalReports = new ArrayList<>();
        try {
            while (result.next()) {
                final int id = result.getInt("id");
                final String user_cf = result.getString("user_cf");
                final String employee_cf = result.getString("employee_cf");
                final Date day = result.getDate("day");
                final String description = result.getString("description");
                final String diagnosis = result.getString("diagnosis");
                final MedicalReport medicalReport = new MedicalReport(id, user_cf, employee_cf, day, description, diagnosis);
                medicalReports.add(medicalReport);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL result set error", e);
        }
        return medicalReports;
    }

    @Override
    public List<MedicalReport> findAll() {
        List<MedicalReport> medicalReports = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            medicalReports = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return medicalReports;
    }

    @Override
    public Optional<MedicalReport> findByPrimaryKey(Pair<Integer, String> primaryKey) {
        final Integer id = primaryKey.getX();
        final String user_cf = primaryKey.getY();

        Optional<MedicalReport> medicalReport = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id=? AND user_cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, user_cf);
            final ResultSet resultSet = statement.executeQuery();
            final List<MedicalReport> medicalReports = findResultSet(resultSet);
            if (!medicalReports.isEmpty()) {
                medicalReport = Optional.of(medicalReports.get(0));
            }
        } catch (final SQLException e) { }
        return medicalReport;
    }

    /**
     * @param user_cf the cf of the user.
     * @return the max id of the medical reports of the user with the given cf.
     */
    public Integer findUserMaxID(String user_cf) {
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE user_cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, user_cf);
            final ResultSet resultSet = statement.executeQuery();
            return findResultSet(resultSet).size() + 1;
        } catch (final SQLException e) { }
        return 1;
    }

    /**
     * Retrieves the medical record at the specified row of the result set of the given query.
     * @param query the query to execute.
     * @param row the row of the result set.
     * @return the medical record at the specified row of the result set of the given query.
     */
    public Optional<MedicalReport> getMedicalReportAtRowWithStatement(final String query, final int row) {
        Optional<MedicalReport> medicalReport = Optional.empty();
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            final ResultSet resultSet = statement.executeQuery();
            medicalReport = Optional.of(findResultSet(resultSet).get(row));
        } catch (Exception e) {
            GUIUtils.exceptionToast("View columns don't match GUI fields", null);
        }
        return medicalReport;
    }

    /**
     * Retrieves the medical record at the specified row of the result set of the given query
     * with the given conditions and orderings.
     * @param user_cf the cf of the user.
     * @param conditions the conditions to add to the query.
     * @param order the orderings to add to the query.
     * @param row the row of the result set.
     * @return the medical record at the specified row of the result set of the given query.
     */
    public Optional<MedicalReport> getMedicalReportsByUserCFAtRow(final String user_cf, final String conditions,
                                                                  final Pair<String,String> order, final int row) {
        Optional<MedicalReport> medicalReport = Optional.empty();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE user_cf=? " + conditions;

        if (order != null) {
            query += " ORDER BY " + order.getX() + " " + order.getY();
        }

        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, user_cf);
            final ResultSet resultSet = statement.executeQuery();
            final List<MedicalReport> medicalReports = findResultSet(resultSet);
            if (!medicalReports.isEmpty()) {
                medicalReport = Optional.of(medicalReports.get(row));
            }
        } catch (final SQLException e) { }
        return medicalReport;
    }

    /**
     * Utility query used to be able to retrieve data from the medical record table in the database
     * and display it in a table.
     */
    public static String getUserMedicalRecordsQuery = "SELECT e.name,e.surname,mr.day,mr.description,mr.diagnosis" +
            " FROM " + TABLE_NAME + " as mr, " + EmployeesTable.TABLE_NAME + " as e" +
            " WHERE mr.user_cf=?" +
            " AND mr.employee_cf=e.cf";

}
