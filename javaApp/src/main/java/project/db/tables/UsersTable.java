package project.db.tables;

import project.db.Table;
import project.model.User;
import project.utils.GUIUtils;
import project.utils.Utils;

import java.sql.*;
import java.util.*;
import java.util.Date;
public class UsersTable implements Table<User, String> {

    public static final String TABLE_NAME = "User";
    private final Connection connection;

    public UsersTable(final Connection connection) {
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
                    "CREATE TABLE " + TABLE_NAME + " ( " +
                            "cf varchar(16) NOT NULL PRIMARY KEY," +
                            "name varchar(25) NOT NULL," +
                            "surname varchar(25) NOT NULL," +
                            "birthday date NOT NULL," +
                            "gender char(1) NOT NULL," +
                            "phone varchar(10)," +
                            "email varchar(50) NOT NULL UNIQUE," +
                            "password varchar(255) NOT NULL" +
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
    public boolean insertToTable(final User value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                "(cf,name,surname,birthday,gender,phone,email,password)" +
                " VALUES (?,?,?,?,?,CASE WHEN (? REGEXP '^[0-9]+$')=1 THEN ? ELSE null END,?,?)";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, value.getCF());
            statement.setString(2, Utils.smartStringCapitalize(value.getName()));
            statement.setString(3, Utils.smartStringCapitalize(value.getSurname()));
            statement.setDate(4, Utils.dateToSqlDate(value.getBirthday()));
            statement.setString(5, String.valueOf(value.getGender()));
            statement.setString(6, value.getPhone().orElse(null));
            statement.setString(7, value.getPhone().orElse(null));
            statement.setString(8, value.getEmail());
            statement.setString(9, value.getPassword());
            statement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new user parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new user insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(final User value, final String primaryKey) {
        final String query = "UPDATE " + TABLE_NAME +
                " SET cf=?," +
                "     name=?," +
                "     surname=?," +
                "     birthday=?," +
                "     gender=?," +
                "     phone=CASE WHEN (? REGEXP '^[0-9]+$')=1 THEN ? ELSE null END," +
                "     email=?," +
                "     password=?" +
                " WHERE cf=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, value.getCF());
            statement.setString(2, Utils.smartStringCapitalize(value.getName()));
            statement.setString(3, Utils.smartStringCapitalize(value.getSurname()));
            statement.setDate(4, Utils.dateToSqlDate(value.getBirthday()));
            statement.setString(5, String.valueOf(value.getGender()));
            statement.setString(6, value.getPhone().orElse(null));
            statement.setString(7, value.getPhone().orElse(null));
            statement.setString(8, value.getEmail());
            statement.setString(9, value.getPassword());
            statement.setString(10, primaryKey);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
            return false;
        }
    }

    @Override
    public boolean deleteFromTable(final String primaryKey) {
        final String query = "DELETE FROM " + TABLE_NAME + " WHERE cf=?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, primaryKey);
            return statement.executeUpdate() > 0;
        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<User> findResultSet(final ResultSet result) {
        List<User> users = new ArrayList<>();

        try {
            while (result.next()) {
                final String CF = result.getString("cf");
                final String name = result.getString("name");
                final String surname = result.getString("surname");
                final Date birthday = Utils.sqlDateToDate(result.getDate("birthday"));
                final char gender = result.getString("gender").charAt(0);
                final Optional<String> phone = Optional.ofNullable(result.getString("phone"));
                final String email = result.getString("email");
                final String password = result.getString("password");
                final User user = new User(CF,name,surname,birthday,gender,phone,email,password);
                users.add(user);
            }
        } catch (final SQLException e) { }

        return users;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            users = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return users;
    }

    @Override
    public Optional<User> findByPrimaryKey(String primaryKey) {
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
     * @return if present, the user with the given email, otherwise an empty optional.
     */
    public Optional<User> findByEmail(String email) {
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
     * Gets the user's password and checks if it matches the given one.
     * @param username the username to search.
     * @param password the password to check.
     * @param password the password to check.
     * @return if the password matches, the user with the given username, otherwise an empty optional.
     */
    public Optional<User> logIn(final String username, final String password) {
        Optional<User> user = username.contains("@") ? findByEmail(username) : findByPrimaryKey(username);

        if (user.isPresent()) {
            return Utils.passwordMatches(password,user.get().getPassword()) ? user : Optional.empty();
        }
        return Optional.empty();
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
     * Retrieves the user at the specified row of the result set of the given query.
     * @param query the query to execute.
     * @param row the row of the result set.
     * @return if present, the user at the specified row, otherwise an empty optional.
     */
    public Optional<User> getUserAtRowWithStatement(final String query, final int row) {
        Optional<User> user = Optional.empty();
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            final ResultSet resultSet = statement.executeQuery();
            user = Optional.of(findResultSet(resultSet).get(row));
        } catch (Exception e) {
            GUIUtils.exceptionToast("View columns don't match GUI fields", null);
        }
        return user;
    }

    /**
     * Utility query used to be able to retrieve data from the user table in the database
     * and display it in a table.
     */
    public static String getUsersQuery = "SELECT cf,name,surname,birthday,email " +
            "FROM " + TABLE_NAME;
}
