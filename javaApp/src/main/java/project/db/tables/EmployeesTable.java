package project.db.tables;

import project.db.Table;
import project.model.Employee;
import project.utils.GUIUtils;
import project.utils.Utils;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class EmployeesTable implements Table<Employee, String> {

    public static final String TABLE_NAME = "Employee";
    private final Connection connection;

    public EmployeesTable(final Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public String getTableName() { return TABLE_NAME; }

    @Override
    public boolean createTable() {
        try (final Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE " + TABLE_NAME + " ( " +
                            "cf varchar(16) NOT NULL PRIMARY KEY," +
                            "name varchar(25) NOT NULL," +
                            "surname varchar(25) NOT NULL," +
                            "birthday date NOT NULL," +
                            "gender char(1) NOT NULL," +
                            "role_id int NOT NULL," +
                            "phone varchar(10)," +
                            "email varchar(50) NOT NULL UNIQUE," +
                            "password varchar(255) NOT NULL," +
                            "permissions int NOT NULL" +
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
    public boolean insertToTable(Employee value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                "(cf,name,surname,birthday,gender,role_id,phone,email,password,permissions)" +
                " VALUES (?,?,?,?,?,?,CASE WHEN (? REGEXP '^[0-9]+$')=1 THEN ? ELSE null END,?,?,?)";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, value.getCF());
            statement.setString(2, value.getName());
            statement.setString(3, value.getSurname());
            statement.setDate(4, Utils.dateToSqlDate(value.getBirthday()));
            statement.setString(5, String.valueOf(value.getGender()));
            statement.setInt(6, value.getRole_id());
            statement.setString(7, value.getPhone().orElse(null));
            statement.setString(8, value.getPhone().orElse(null));
            statement.setString(9, value.getEmail());
            statement.setString(10, value.getPassword());
            statement.setInt(11, value.getPermissions());
            statement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new employee parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new employee insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(Employee value, String primaryKey) {
        final String query =
                "UPDATE " + TABLE_NAME + " SET " +
                        "cf=?," +
                        "name=?," +
                        "surname=?, " +
                        "birthday=?, " +
                        "gender=?, " +
                        "role_id=?, " +
                        "phone=CASE WHEN (? REGEXP '^[0-9]+$')=1 THEN ? ELSE null END, " +
                        "email=?, " +
                        "password=?, " +
                        "permissions=? " +
                        "WHERE cf=?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, value.getCF());
            statement.setString(2, Utils.smartStringCapitalize(value.getName()));
            statement.setString(3, Utils.smartStringCapitalize(value.getSurname()));
            statement.setDate(4, Utils.dateToSqlDate(value.getBirthday()));
            statement.setString(5, String.valueOf(value.getGender()));
            statement.setInt(6, value.getRole_id());
            statement.setString(7, value.getPhone().orElse(null));
            statement.setString(8, value.getPhone().orElse(null));
            statement.setString(9, value.getEmail());
            statement.setString(10, value.getPassword());
            statement.setInt(11, value.getPermissions());
            statement.setString(12, primaryKey);
            return statement.executeUpdate() > 0;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
        }
        return false;
    }

    @Override
    public boolean deleteFromTable(String primaryKey) {
        final String query = "DELETE FROM " + TABLE_NAME + " WHERE cf=?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, primaryKey);
            return statement.executeUpdate() > 0;
        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<Employee> findResultSet(ResultSet result) {
        List<Employee> employees = new ArrayList<>();

        try {
            while (result.next()) {
                String CF = result.getString("cf");
                String name = result.getString("name");
                String surname = result.getString("surname");
                Date birthday = Utils.sqlDateToDate(result.getDate("birthday"));
                char gender = result.getString("gender").charAt(0);
                Integer role_id = result.getInt("role_id");
                Optional<String> phone = Optional.ofNullable(result.getString("phone"));
                String email = result.getString("email");
                String password = result.getString("password");
                int permissions = result.getInt("permissions");
                final Employee employee = new Employee(CF,name,surname,birthday,gender,role_id,phone,email,password,permissions);
                employees.add(employee);
            }
        } catch (final SQLException e) { }

        return employees;
    }

    @Override
    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            employees = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return employees;
    }

    @Override
    public Optional<Employee> findByPrimaryKey(String primaryKey) {
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE cf=?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, primaryKey);
            final ResultSet resultSet = statement.executeQuery();
            return findResultSet(resultSet).stream().findFirst();
        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param email the email to search.
     * @return if present, the employee with the given email, otherwise an empty optional.
     */
    public Optional<Employee> findByEmail(String email) {
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE email=?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, email);
            final ResultSet resultSet = statement.executeQuery();
            return findResultSet(resultSet).stream().findFirst();
        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param email the email to search.
     * @return if the email is already present in the database or not.
     */
    public boolean emailAlreadyExists(final String email) {
        final String query = "SELECT COUNT(*) FROM " + TABLE_NAME +
                "WHERE email=?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) >= 1 ? true : false;
        } catch (final SQLException e) {}
        return false;
    }

    /**
     * Gets the employee's password and checks if it matches the given one.
     * @param username the username to search.
     * @param password the password to check.
     * @return if the password matches, the employee with the given username, otherwise an empty optional.
     */
    public Optional<Employee> logIn(final String username, final String password) {
        Optional<Employee> employee = username.contains("@") ? findByEmail(username) : findByPrimaryKey(username);

        if (employee.isPresent()) {
            return Utils.passwordMatches(password,employee.get().getPassword()) ? employee : Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * @param CF the CF to search.
     * @return the employee's permissions.
     */
    public int getEmployeePermissions(final String CF) {
        final String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE cf=?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, CF);
            final ResultSet resultSet = statement.executeQuery();
            List<Employee> result = findResultSet(resultSet);

            if (!result.isEmpty()) {
                return result.get(0).getPermissions();
            }
            throw new SQLException();
        } catch (SQLException e) { }
        return -1;
    }

    /**
     * Retrieves the employee at the specified row of the result set of the given query.
     * @param query the query to execute.
     * @param row the row of the result set.
     * @return if present, the employee at the specified row, otherwise an empty optional.
     */
    public Optional<Employee> getEmployeeAtRowWithStatement(final String query, final int row) {
        Optional<Employee> employee = Optional.empty();
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            final ResultSet resultSet = statement.executeQuery();
            employee = Optional.of(findResultSet(resultSet).get(row));
        } catch (Exception e) {
            GUIUtils.exceptionToast("View columns don't match GUI fields", null);
        }
        return employee;
    }

    /**
     * Utility query used to be able to retrieve all the employees that are medics.
     */
    public static String getMedics = "SELECT e.* " +
        "FROM " + TABLE_NAME + " AS e " +
        "JOIN " + PositionsTable.TABLE_NAME + " AS p ON e.role_id=p.id " +
        "WHERE p.role!=\"Admin\" " +
        "AND p.role!=\"Nurse\"";

    public static String getMedicsWithAgenda = "SELECT e.* " +
            "FROM " + TABLE_NAME + " AS e " +
            "JOIN " + PositionsTable.TABLE_NAME + " AS p ON e.role_id=p.id " +
            "JOIN " + AgendasTable.TABLE_NAME + " AS a ON e.cf=a.employee_cf " +
            "WHERE p.role!=\"Admin\" " +
            "AND p.role!=\"Nurse\"";

    /**
     * Utility query used to be able to retrieve all the employees that are medics
     * with their respective specialization to be able to show them in a table.
     */
    public static String getMedicsWithRespectiveSpecialization = "SELECT name,surname,specialization,email " +
            "FROM " + TABLE_NAME + " AS e " +
            "JOIN " + PositionsTable.TABLE_NAME + " AS p ON e.role_id=p.id " +
            "WHERE p.role!=\"Admin\" " +
            "AND p.role!=\"Nurse\" ";

    public static String getAvailableMedicsWithRespectiveSpecialization = "SELECT name,surname,specialization,email " +
            "FROM " + TABLE_NAME + " AS e " +
            "JOIN " + PositionsTable.TABLE_NAME + " AS p ON e.role_id=p.id " +
            "JOIN " + AgendasTable.TABLE_NAME + " AS a ON e.cf=a.employee_cf " +
            "WHERE p.role!=\"Admin\" " +
            "AND p.role!=\"Nurse\" ";
}
