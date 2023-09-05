package project.db.tables;

import project.db.Table;
import project.model.TimeSlot;
import project.utils.GUIUtils;
import project.utils.Pair;
import project.utils.Utils;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class TimeSlotsTable implements Table<TimeSlot,Pair<Integer, Object[]>> {

    public static final String TABLE_NAME = "Time_Slot";
    private final Connection connection;

    public TimeSlotsTable(final Connection connection) {
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
                            "day date NOT NULL," +
                            "agenda_day varchar(25) NOT NULL," +
                            "agenda_employee_cf varchar(16) NOT NULL," +
                            "slot time NOT NULL," +
                            "available bit NOT NULL," +
                            "PRIMARY KEY (day, slot, agenda_employee_cf)," +
                            "FOREIGN KEY (agenda_day, agenda_employee_cf) REFERENCES " + AgendasTable.TABLE_NAME + "(day, employee_cf)," +
                            "UNIQUE KEY timeslot_id (id)" +
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
    public boolean insertToTable(TimeSlot value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (id, day, agenda_day, agenda_employee_cf, slot, available)" +
                " VALUES (?, ?, ?, ?, ?, ?)";
        try (final var preparedStatement = this.connection.prepareStatement(query)) {
            preparedStatement.setInt(1, getMaxId());
            preparedStatement.setDate(2, Utils.dateToSqlDate(value.getDay()));
            preparedStatement.setString(3, value.getAgenda_day());
            preparedStatement.setString(4, value.getAgenda_employee_cf());
            preparedStatement.setString(5, value.getSlot());
            preparedStatement.setBoolean(6, value.getStatus());
            preparedStatement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new time slot parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new time slot insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(TimeSlot value, Pair<Integer, Object[]> primaryKey) {
        final int id = primaryKey.getX();
        final Date day = (Date) primaryKey.getY()[0];
        final String slot = (String) primaryKey.getY()[1];
        final String employee_cf = (String) primaryKey.getY()[2];

        final String query = "UPDATE " + TABLE_NAME +
                " SET id=?, day=?, agenda_day=?, agenda_employee_cf=?, slot=?, available=?" +
                " WHERE id=? OR (day=? AND slot=? AND agenda_employee_cf=?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, value.getId());
            statement.setDate(2, Utils.dateToSqlDate(value.getDay()));
            statement.setString(3, value.getAgenda_day());
            statement.setString(4, value.getAgenda_employee_cf());
            statement.setString(5, value.getSlot());
            statement.setBoolean(6, value.getStatus());
            statement.setInt(7, id);
            statement.setDate(8, Utils.dateToSqlDate(day));
            statement.setTime(9, Utils.stringToSqlTime(slot));
            statement.setString(10, employee_cf);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
            return false;
        }
    }

    @Override
    public boolean deleteFromTable(Pair<Integer, Object[]> primaryKey) {
        final int id = primaryKey.getX();
        final Date day = (Date) primaryKey.getY()[0];
        final String slot = (String) primaryKey.getY()[1];
        final String employee_cf = (String) primaryKey.getY()[2];

        final String query = "DELETE FROM " + TABLE_NAME + " WHERE id=? OR (day=? AND slot=? AND agenda_employee_cf=?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setDate(2, Utils.dateToSqlDate(day));
            statement.setTime(3, Utils.stringToSqlTime(slot));
            statement.setString(4, employee_cf);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
            return false;
        }
    }

    @Override
    public List<TimeSlot> findResultSet(ResultSet result) {
        final List<TimeSlot> timeSlots = new ArrayList<>();
        try {
            while (result.next()) {
                final int id = result.getInt("id");
                final Date day = result.getDate("day");
                final String agenda_day = result.getString("agenda_day");
                final String agenda_employee_cf = result.getString("agenda_employee_cf");
                final String slot = Utils.sqlTimeToString(result.getTime("slot"));
                final boolean status = result.getBoolean("available");
                final TimeSlot timeSlot = new TimeSlot(id, day, agenda_day, agenda_employee_cf, slot, status);
                timeSlots.add(timeSlot);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL result set error", e);
        }
        return timeSlots;
    }

    @Override
    public List<TimeSlot> findAll() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            timeSlots = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return timeSlots;
    }

    @Override
    public Optional<TimeSlot> findByPrimaryKey(Pair<Integer, Object[]> primaryKey) {
        final int id = primaryKey.getX();
        final Date day = (Date) primaryKey.getY()[0];
        final String slot = (String) primaryKey.getY()[1];
        final String employee_cf = (String) primaryKey.getY()[2];

        Optional<TimeSlot> timeSlot = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id=? OR (day=? AND slot=? AND agenda_employee_cf=?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setDate(2, Utils.dateToSqlDate(day));
            statement.setTime(3, Utils.stringToSqlTime(slot));
            statement.setString(4, employee_cf);
            final ResultSet resultSet = statement.executeQuery();
            final List<TimeSlot> timeSlots = findResultSet(resultSet);
            if (!timeSlots.isEmpty()) {
                timeSlot = Optional.of(timeSlots.get(0));
            }
        } catch (final SQLException e) { }
        return timeSlot;
    }

    /**
     * @param query the query to execute.
     * @return a list of time slots available with the given query.
     */
    public List<String> findTimeSlotsAvailableByUserQuery(final String query) {
        List<String> timeSlots = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                timeSlots.add(resultSet.getString("slot"));
            }
        } catch (final SQLException e) { }
        return timeSlots;
    }

    /**
     * Retrieves the time slot at the specified row of the result set of the given query.
     * @param query the query to execute.
     * @param row the row of the result set.
     * @return the time slot at the specified row of the result set of the given query.
     */
    public Optional<TimeSlot> getTimeSlotAtRowWithStatement(final String query, final int row) {
        Optional<TimeSlot> timeSlot = Optional.empty();
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            final ResultSet resultSet = statement.executeQuery();
            timeSlot = Optional.of(findResultSet(resultSet).get(row));
        } catch (Exception e) {
            GUIUtils.exceptionToast("View columns don't match GUI fields", e);
        }
        return timeSlot;
    }

    /**
     * Method used to delete all the past `time slots` to free up space in the database.
     */
    public void deleteAllPastDays() {
        final String query = "DELETE FROM " + TABLE_NAME + " WHERE day < ?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setDate(1, Utils.dateToSqlDate(new Date()));
            statement.executeUpdate();
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
        }
    }

    /**
     * Method used to delete all the `time slots` of the given employee when the agenda
     * of the given day is deleted or updated.
     * @param employee_cf
     * @param day
     * @return
     */
    public boolean deleteAllTimeSlotsOfEmployeeByDay(final String employee_cf, final String day) {
        final String query = "DELETE FROM " + TABLE_NAME + " WHERE agenda_employee_cf=? AND agenda_day=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, employee_cf);
            statement.setString(2, day);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            return false;
        }
    }

    /**
     * Utility query used to be able to retrieve data from the time slot table in the database
     * and display it in a table.
     */
    public static String getTimeslotsAvailable = "SELECT t.day,t.slot,e.cf,e.name,e.surname " +
            "FROM " + TABLE_NAME + " AS t JOIN " + EmployeesTable.TABLE_NAME + " AS e ON t.agenda_employee_cf=e.cf " +
            "WHERE t.available=1";

    /**
     * Utility query used to be able to retrieve data from the time slot table in the database.
     */
    public static String getSelectedTimeslotData = "SELECT " + TimeSlotsTable.TABLE_NAME + ".* FROM " + TimeSlotsTable.TABLE_NAME + " " +
            "JOIN " + EmployeesTable.TABLE_NAME + " ON agenda_employee_cf=cf " +
            "WHERE available=1 ";

    /**
     * Utility query used to be able to retrieve data from the agenda table in the database
     * and load it in a combo box.
     */
    public static String getAllTimeslots = "SELECT DISTINCT t.slot " +
            "FROM " + TABLE_NAME + " AS t, " + EmployeesTable.TABLE_NAME + " AS e, " + PositionsTable.TABLE_NAME + " AS p, " + AgendasTable.TABLE_NAME + " AS a " +
            "WHERE a.employee_cf=e.cf " +
            "AND t.agenda_employee_cf=a.employee_cf " +
            "AND t.agenda_day=a.day " +
            "AND e.role_id=p.id " +
            "AND t.available=1";

    /**
     * @return the maximum id of the time slots in the database incremented by one.
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
