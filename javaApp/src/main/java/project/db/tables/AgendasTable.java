package project.db.tables;

import project.db.Table;
import project.model.Agenda;
import project.utils.GUIUtils;
import project.utils.Pair;
import project.utils.Utils;

import java.sql.*;
import java.util.*;
public class AgendasTable implements Table<Agenda, Pair<String,String>> {
    public static final String TABLE_NAME = "Agenda";
    private final Connection connection;

    public AgendasTable(final Connection connection) {
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
                            "day varchar(25) NOT NULL," +
                            "employee_cf varchar(16) NOT NULL," +
                            "ambulatory_id int NOT NULL," +
                            "daystart time NOT NULL," +
                            "breakstart time NOT NULL," +
                            "breakend time NOT NULL," +
                            "dayend time NOT NULL," +
                            "deltatime time NOT NULL," +
                            "PRIMARY KEY (day, employee_cf)," +
                            "FOREIGN KEY (employee_cf) REFERENCES " + EmployeesTable.TABLE_NAME + "(cf)," +
                            "FOREIGN KEY (ambulatory_id) REFERENCES " + AmbulatoryTable.TABLE_NAME + "(id)," +
                            "CHECK (daystart < breakstart AND breakstart < breakend AND breakend < dayend AND deltatime < daystart)" +
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
    public boolean insertToTable(Agenda value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (day,employee_cf,ambulatory_id,daystart,breakstart,breakend,dayend,deltatime)" +
                " VALUES (?,?,?,?,?,?,?,?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, Utils.smartStringCapitalize(value.getDay()));
            statement.setString(2, value.getEmployee_cf().toUpperCase());
            statement.setInt(3, value.getAmbulatory_id());
            statement.setTime(4, Utils.stringToSqlTime(value.getDaystart()));
            statement.setTime(5, Utils.stringToSqlTime(value.getBreakstart()));
            statement.setTime(6, Utils.stringToSqlTime(value.getBreakend()));
            statement.setTime(7, Utils.stringToSqlTime(value.getDayend()));
            statement.setTime(8, Utils.stringToSqlTime(value.getDeltatime()));
            statement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new agenda parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new agenda insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(Agenda value, Pair<String, String> primaryKey) {
        String day = primaryKey.getX();
        String employee_cf = primaryKey.getY();

        final String query = "UPDATE " + TABLE_NAME +
                " SET day=?,employee_cf=?,ambulatory_id=?,daystart=?,breakstart=?,breakend=?,dayend=?,deltatime=?" +
                " WHERE day=? AND employee_cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, Utils.smartStringCapitalize(value.getDay()));
            statement.setString(2, value.getEmployee_cf().toUpperCase());
            statement.setInt(3, value.getAmbulatory_id());
            statement.setTime(4, Utils.stringToSqlTime(value.getDaystart()));
            statement.setTime(5, Utils.stringToSqlTime(value.getBreakstart()));
            statement.setTime(6, Utils.stringToSqlTime(value.getBreakend()));
            statement.setTime(7, Utils.stringToSqlTime(value.getDayend()));
            statement.setTime(8, Utils.stringToSqlTime(value.getDeltatime()));
            statement.setString(9, Utils.smartStringCapitalize(day));
            statement.setString(10, employee_cf);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
            return false;
        }
    }

    @Override
    public boolean deleteFromTable(Pair<String, String> primaryKey) {
        String day = primaryKey.getX();
        String employee_cf = primaryKey.getY();

        final String query = "DELETE FROM " + TABLE_NAME + " WHERE day=? AND employee_cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, Utils.smartStringCapitalize(day));
            statement.setString(2, employee_cf);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
            return false;
        }
    }

    @Override
    public List<Agenda> findResultSet(ResultSet result) {
        final List<Agenda> agendas = new ArrayList<>();
        try {
            while (result.next()) {
                final String day = result.getString("day");
                final String employee_cf = result.getString("employee_cf");
                final int ambulatory_id = result.getInt("ambulatory_id");
                final String daystart = Utils.sqlTimeToString(result.getTime("daystart"));
                final String breakstart = Utils.sqlTimeToString(result.getTime("breakstart"));
                final String breakend = Utils.sqlTimeToString(result.getTime("breakend"));
                final String dayend = Utils.sqlTimeToString(result.getTime("dayend"));
                final String deltatime = Utils.sqlTimeToString(result.getTime("deltatime"));
                final Agenda agenda = new Agenda(day, employee_cf, ambulatory_id, daystart, breakstart, breakend, dayend, deltatime);
                agendas.add(agenda);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL result set error", e);
        }
        return agendas;
    }

    @Override
    public List<Agenda> findAll() {
        List<Agenda> agendas = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            agendas = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return agendas;
    }

    @Override
    public Optional<Agenda> findByPrimaryKey(Pair<String, String> primaryKey) {
        String day = primaryKey.getX();
        String employee_cf = primaryKey.getY();

        Optional<Agenda> agenda = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE day=? AND employee_cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, Utils.smartStringCapitalize(day));
            statement.setString(2, employee_cf);
            final ResultSet resultSet = statement.executeQuery();
            final List<Agenda> agendas = findResultSet(resultSet);
            if (!agendas.isEmpty()) {
                agenda = Optional.of(agendas.get(0));
            }
        } catch (final SQLException e) { }
        return agenda;
    }

    /**
     * Retrieves the agenda at the specified row of the result set of the given query
     * by using the given employee cf.
     * @param query the query to execute.
     * @param row the row of the result set to retrieve.
     * @param employee_cf the employee cf to use.
     * @return the agenda at the specified row of the result set of the given query.
     */
    public Optional<Agenda> getAgendaAtRowWithStatement(final String query, final int row, final String employee_cf) {
        Optional<Agenda> agenda = Optional.empty();
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, employee_cf);
            final ResultSet resultSet = statement.executeQuery();
            agenda = Optional.of(findResultSet(resultSet).get(row));
        } catch (Exception e) {
            GUIUtils.exceptionToast("View columns don't match GUI fields", null);
        }
        return agenda;
    }

    /**
     * Utility query used to be able to retrieve data from the agenda table in the database
     * and display it in a table.
     */
    public static String getAgendaByEmployeeCFQuery = "SELECT * FROM " + TABLE_NAME + " WHERE employee_cf = ?";
}
