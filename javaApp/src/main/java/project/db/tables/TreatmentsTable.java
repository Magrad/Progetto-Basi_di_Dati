package project.db.tables;

import project.db.Table;
import project.model.Treatment;
import project.utils.GUIUtils;
import project.utils.Pair;
import project.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TreatmentsTable implements Table<Treatment, Pair<Integer,String>> {
    public static final String TABLE_NAME = "Treatment";
    private final Connection connection;

    public TreatmentsTable(final Connection connection) {
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
                            "id int NOT NULL," +
                            "name varchar(50) NOT NULL," +
                            "type varchar(50) NOT NULL," +
                            "description varchar(1000) NOT NULL," +
                            "posology varchar(1000) NOT NULL," +
                            "allergens varchar(100) NOT NULL," +
                            "PRIMARY KEY (name)," +
                            "UNIQUE KEY treatment_id (id)" +
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
    public boolean insertToTable(Treatment value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (id,name,type,description,posology,allergens)" +
                " VALUES (?,?,?,?,?,?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, getMaxId());
            statement.setString(2, Utils.smartStringCapitalize(value.getName()));
            statement.setString(3, Utils.smartStringCapitalize(value.getType()));
            statement.setString(4, Utils.smartStringCapitalize(value.getDescription()));
            statement.setString(5, Utils.smartStringCapitalize(value.getPosology()));
            statement.setString(6, Utils.smartStringCapitalize(value.getAllergens()));
            statement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new treatment parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new treatment insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(Treatment value, Pair<Integer,String> primaryKey) {
        final Integer id = primaryKey.getX();
        final String name = primaryKey.getY();

        final String query = "UPDATE " + TABLE_NAME + " SET " +
                "id = ?, " +
                "name = ?, " +
                "type = ?, " +
                "description = ?, " +
                "posology = ?, " +
                "allergens = ? " +
                "WHERE id = ?" +
                "OR name = ?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, value.getId());
            statement.setString(2, value.getName());
            statement.setString(3, value.getType());
            statement.setString(4, value.getDescription());
            statement.setString(5, value.getPosology());
            statement.setString(6, value.getAllergens());
            statement.setInt(7, id);
            statement.setString(8, name);
            statement.executeUpdate();
            return true;
        }  catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
        }
        return false;
    }

    @Override
    public boolean deleteFromTable(Pair<Integer,String> primaryKey) {
        Integer id = primaryKey.getX();
        String name = primaryKey.getY();

        final String query = "DELETE FROM " + TABLE_NAME + " WHERE id = ? OR name = ?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
        }
        return false;
    }

    @Override
    public List<Treatment> findResultSet(ResultSet result) {
        final List<Treatment> treatments = new ArrayList<>();
        try {
            while (result.next()) {
                Integer id = result.getInt("id");
                String name = result.getString("name");
                String type = result.getString("type");
                String description = result.getString("description");
                String posology = result.getString("posology");
                String allergens = result.getString("allergens");
                final Treatment treatment = new Treatment(id, name, type, description, posology, allergens);
                treatments.add(treatment);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL result set error", e);
        }
        return treatments;
    }

    @Override
    public List<Treatment> findAll() {
        List<Treatment> treatment = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            treatment = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return treatment;
    }

    @Override
    public Optional<Treatment> findByPrimaryKey(Pair<Integer,String> primaryKey) {
        Integer id = primaryKey.getX();
        String name = primaryKey.getY();

        Optional<Treatment> treatment = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id = ? OR name = ?";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, name);
            final ResultSet resultSet = statement.executeQuery();
            return findResultSet(resultSet).stream().findFirst();
        } catch (final SQLException e) { }
        return treatment;
    }

    /**
     * Retrieves the treatment at the specified row of the result set of the given query.
     * @param query the query to execute.
     * @param row the row of the result set to retrieve.
     * @return the treatment at the specified row of the result set of the given query.
     */
    public Optional<Treatment> getTreatmentAtRowWithStatement(final String query, final int row) {
        Optional<Treatment> treatment = Optional.empty();
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            final ResultSet resultSet = statement.executeQuery();
            treatment = Optional.of(findResultSet(resultSet).get(row));
        } catch (Exception e) {
            GUIUtils.exceptionToast("View columns don't match GUI fields", null);
        }
        return treatment;
    }

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
