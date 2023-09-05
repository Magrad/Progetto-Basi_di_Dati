package project.db.tables;

import project.db.Table;
import project.model.Booking;
import project.utils.GUIUtils;
import project.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BookingsTable implements Table<Booking,Object[]> {

    public static final String TABLE_NAME = "Booking";
    private final Connection connection;

    public BookingsTable(final Connection connection) {
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
                            "day DATE NOT NULL," +
                            "timeslot_id INT NOT NULL," +
                            "user_cf varchar(16) NOT NULL," +
                            "employee_cf varchar(16) NOT NULL," +
                            "feedback varchar(255)," +
                            "PRIMARY KEY (day, timeslot_id, user_cf)," +
                            "FOREIGN KEY (timeslot_id) REFERENCES " + TimeSlotsTable.TABLE_NAME + "(id)," +
                            "FOREIGN KEY (user_cf) REFERENCES " + UsersTable.TABLE_NAME + "(cf)," +
                            "FOREIGN KEY (employee_cf) REFERENCES " + EmployeesTable.TABLE_NAME + "(cf)," +
                            "UNIQUE KEY booking_id (id)" +
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
    public boolean insertToTable(Booking value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (id,day,timeslot_id,user_cf,employee_cf,feedback)" +
                " VALUES (?,?,?,?,?,?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, getMaxId());
            statement.setDate(2, Utils.dateToSqlDate(value.getDay()));
            statement.setInt(3, value.getTimeslot_id());
            statement.setString(4, value.getUser_cf());
            statement.setString(5, value.getEmployee_cf());
            statement.setString(6, value.getFeedback());
            statement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new booking parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new booking insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(Booking value, Object[] primaryKey) {
        final Date day = (Date) primaryKey[0];
        final Integer timeslot_id = (Integer) primaryKey[1];
        final String user_cf = (String) primaryKey[2];

        final String query = "UPDATE " + TABLE_NAME +
                " SET id=?, day=?, timeslot_id=?, user_cf=?, employee_cf=?, feedback=?" +
                " WHERE day=? AND timeslot_id=? AND user_cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, getMaxId());
            statement.setDate(2, Utils.dateToSqlDate(value.getDay()));
            statement.setInt(3, value.getTimeslot_id());
            statement.setString(4, value.getUser_cf());
            statement.setString(5, value.getEmployee_cf());
            statement.setString(6, value.getFeedback());
            statement.setDate(7, day);
            statement.setInt(8, timeslot_id);
            statement.setString(9, user_cf);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
            return false;
        }
    }

    @Override
    public boolean deleteFromTable(Object[] primaryKey) {
        final Date day = (Date) primaryKey[0];
        final Integer timeslot_id = (Integer) primaryKey[1];
        final String user_cf = (String) primaryKey[2];

        final String query = "DELETE FROM " + TABLE_NAME + " WHERE day=? AND timeslot_id=? AND user_cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setDate(1, day);
            statement.setInt(2, timeslot_id);
            statement.setString(3, user_cf);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
            return false;
        }
    }

    @Override
    public List<Booking> findResultSet(ResultSet result) {
        final List<Booking> bookings = new ArrayList<>();
        try {
            while (result.next()) {
                final int id = result.getInt("id");
                final java.util.Date day = result.getDate("day");
                final int timeslot_id = result.getInt("timeslot_id");
                final String user_cf = result.getString("user_cf");
                final String employee_cf = result.getString("employee_cf");
                final String feedback = result.getString("feedback");
                final Booking booking = new Booking(id, day, timeslot_id, user_cf, employee_cf, feedback);
                bookings.add(booking);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL result set error", e);
        }
        return bookings;
    }

    @Override
    public List<Booking> findAll() {
        List<Booking> bookings = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            bookings = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return bookings;
    }

    @Override
    public Optional<Booking> findByPrimaryKey(Object[] primaryKey) {
        final Date day = (Date) primaryKey[0];
        final Integer timeslot_id = (Integer) primaryKey[1];
        final String user_cf = (String) primaryKey[2];

        Optional<Booking> booking = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE day=? AND timeslot_id=? AND user_cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setDate(1, day);
            statement.setInt(2, timeslot_id);
            statement.setString(3, user_cf);
            final ResultSet resultSet = statement.executeQuery();
            final List<Booking> bookings = findResultSet(resultSet);
            if (!bookings.isEmpty()) {
                booking = Optional.of(bookings.get(0));
            }
        } catch (final SQLException e) { }
        return booking;
    }

    /**
     * Retrieves the booking at the specified row of the result set of the given query.
     * @param query the query to execute.
     * @param row the row of the result set.
     * @return the booking at the specified row of the result set of the given query.
     */
    public Optional<Booking> getBookingAtRowWithStatement(final String query, final int row) {
        Optional<Booking> booking = Optional.empty();
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            final ResultSet resultSet = statement.executeQuery();
            booking = Optional.of(findResultSet(resultSet).get(row));
        } catch (Exception e) {
            GUIUtils.exceptionToast("View columns don't match GUI fields", null);
        }
        return booking;
    }

    /**
     * Retrieves the booking at the specified row of the result set of the given query
     * by using the given user/employee cf.
     * @param query the query to execute.
     * @param row the row of the result set.
     * @param cf the user/employee cf
     * @return
     */
    public Optional<Booking> getBookingAtRowWithStatementWithCF(final String query, final int row, final String cf) {
        Optional<Booking> booking = Optional.empty();
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, cf);
            final ResultSet resultSet = statement.executeQuery();
            booking = Optional.of(findResultSet(resultSet).get(row));
        } catch (Exception e) {
            GUIUtils.exceptionToast("View columns don't match GUI fields", e);
        }
        return booking;
    }

    /**
     * Method used to delete all the past bookings to free up space in the database.
     */
    public void deleteAllPastDays() {
        final String query = "DELETE FROM " + TABLE_NAME + " WHERE day < ?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setDate(1, Utils.dateToSqlDate(new java.util.Date()));
            statement.executeUpdate();
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
        }
    }

    /**
     * Utility query used to be able to retrieve data of a specific employee from the
     * booking table and display it in a table.
     */
    public static String getBookingByEmployeeCFQuery = "SELECT b.day, t.slot, b.feedback, user_cf as \"patient\", employee_cf as \"doctor\" " +
            "FROM " + TABLE_NAME + " AS b JOIN " + TimeSlotsTable.TABLE_NAME + " AS t ON b.timeslot_id=t.id " +
            "JOIN " + EmployeesTable.TABLE_NAME + " AS e ON b.employee_cf=e.cf " +
            "WHERE e.cf=? " +
            "AND TRIM(b.feedback)=\"\"";

    /**
     * Utility query used to be able to retrieve data of a specific user from the
     * booking table and display it in a table.
     */
    public static String getBookingByUserCFQuery = "SELECT b.day,t.slot,b.user_cf,b.employee_cf,b.feedback " +
            "FROM " + TABLE_NAME + " AS b, " + TimeSlotsTable.TABLE_NAME + " AS t " +
            "WHERE user_cf=? " +
            "AND b.timeslot_id=t.id ";

    /**
     * Utility query used to be able to retrieve a specific booking from the
     * booking table by using the employee cf.
     */
    public static String getBookingsByEmployeeCFQuery = "SELECT * FROM " + TABLE_NAME + " WHERE employee_cf=? AND TRIM(feedback)=\"\"";

    /**
     * Utility query used to be able to retrieve a specific booking from the
     * booking table by using the user cf.
     */
    public static String getBookingsByUserCFQuery = "SELECT b.* " +
            "FROM " + TABLE_NAME + " AS b, " + TimeSlotsTable.TABLE_NAME + " AS t " +
            "WHERE user_cf=? " +
            "AND b.timeslot_id=t.id ";

    /**
     * @return the maximum id of the table and returns it incremented by 1.
     */
    private Integer getMaxId() {
        final String query = "SELECT MAX(id) FROM " + TABLE_NAME;
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getInt(1)+1;
            }
        } catch (final SQLException e) { }
        return -1;
    }
}
