package project.db.tables;

import project.db.Table;
import project.model.Drug;
import project.utils.GUIUtils;
import project.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DrugsTable implements Table<Drug, Integer> {
    public static final String TABLE_NAME = "Drug";
    private final Connection connection;

    public DrugsTable(final Connection connection) {
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
                            "id int AUTO_INCREMENT," +
                            "name varchar(25) NOT NULL," +
                            "type varchar(25) NOT NULL," +
                            "description varchar(255) NOT NULL," +
                            "posology varchar(100) NOT NULL," +
                            "allergens varchar(100) NOT NULL," +
                            "PRIMARY KEY (id, name)" +
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
    public boolean insertToTable(Drug value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (name,type,description,posology,allergens)" +
                " VALUES (?,?,?,?,?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, Utils.smartStringCapitalize(value.getName()));
            statement.setString(2, Utils.smartStringCapitalize(value.getType()));
            statement.setString(3, Utils.smartStringCapitalize(value.getDescription()));
            statement.setString(4, Utils.smartStringCapitalize(value.getPosology()));
            statement.setString(5, Utils.smartStringCapitalize(value.getAllergens()));
            statement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new drug parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new drug insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(Drug value, Integer primaryKey) {
        //update table with late binding
        final String query = "UPDATE " + TABLE_NAME + " SET " +
                "name = ?, " +
                "type = ?, " +
                "description = ?, " +
                "posology = ?, " +
                "allergens = ? " +
                "WHERE id = ?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, value.getName());
            statement.setString(2, value.getType());
            statement.setString(3, value.getDescription());
            statement.setString(4, value.getPosology());
            statement.setString(5, value.getAllergens());
            statement.setInt(6, primaryKey);
            statement.executeUpdate();
            return true;
        }  catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
        }
        return false;
    }

    @Override
    public boolean deleteFromTable(Integer primaryKey) {
        final String query = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, primaryKey);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
        }
        return false;
    }

    @Override
    public List<Drug> findResultSet(ResultSet result) {
        final List<Drug> drugs = new ArrayList<>();
        try {
            while (result.next()) {
                Integer id = result.getInt("id");
                String name = result.getString("name");
                String type = result.getString("type");
                String description = result.getString("description");
                String posology = result.getString("posology");
                String allergens = result.getString("allergens");
                final Drug drug = new Drug(id, name, type, description, posology, allergens);
                drugs.add(drug);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL result set error", e);
        }
        return drugs;
    }

    @Override
    public List<Drug> findAll() {
        List<Drug> drug = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            drug = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return drug;
    }

    @Override
    public Optional<Drug> findByPrimaryKey(Integer primaryKey) {
        Optional<Drug> drug = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, primaryKey);
            final ResultSet resultSet = statement.executeQuery();
            return findResultSet(resultSet).stream().findFirst();
        } catch (final SQLException e) { }
        return drug;
    }

    public Optional<Drug> getDrugAtRowWithStatement(final String query, final int row) {
        Optional<Drug> drug = Optional.empty();
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            final ResultSet resultSet = statement.executeQuery();
            drug = Optional.of(findResultSet(resultSet).get(row));
        } catch (Exception e) {
            GUIUtils.exceptionToast("View columns don't match GUI fields", null);
        }
        return drug;
    }
}
