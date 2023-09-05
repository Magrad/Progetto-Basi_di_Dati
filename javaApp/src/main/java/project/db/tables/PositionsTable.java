package project.db.tables;

import project.db.Table;
import project.model.Position;
import project.utils.GUIUtils;
import project.utils.Pair;
import project.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PositionsTable implements Table<Position, Pair<Integer,Pair<String,String>>> {
    public static final String TABLE_NAME = "Position";
    private final Connection connection;

    public PositionsTable(final Connection connection) {
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
                            "role varchar(25) NOT NULL," +
                            "specialization varchar(25) NOT NULL," +
                            "PRIMARY KEY (role, specialization)," +
                            "UNIQUE KEY position_id (id)" +
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
    public boolean insertToTable(Position value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (id,role,specialization)" +
                " VALUES (?,?,?)";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, value.getId());
            statement.setString(2, Utils.smartStringCapitalize(value.getRole()));
            statement.setString(3, value.getSpecialization());
            statement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new position parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new position insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(Position value, Pair<Integer,Pair<String,String>> primaryKey) {
        int id = primaryKey.getX();
        String role = primaryKey.getY().getX();
        String speicialization = primaryKey.getY().getY();

        final String query = "UPDATE " + TABLE_NAME + " SET " +
                "id = ?, " +
                "role = ?, " +
                "specialization = ? " +
                "WHERE id = ? OR (role=? AND specialization=?)";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, value.getRole());
            statement.setString(3, value.getSpecialization());
            statement.setInt(4, id);
            statement.setString(5, role);
            statement.setString(6, speicialization);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
        }
        return false;
    }

    @Override
    public boolean deleteFromTable(Pair<Integer,Pair<String,String>> primaryKey) {
        int id = primaryKey.getX();
        String role = primaryKey.getY().getX();
        String speicialization = primaryKey.getY().getY();

        final String query = "DELETE FROM " + TABLE_NAME + " WHERE id=? OR (role=? AND specialization=?)";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, role);
            statement.setString(3, speicialization);
            return statement.executeUpdate() > 0;
        } catch (final SQLIntegrityConstraintViolationException sql) {
            GUIUtils.exceptionToast("Cannot delete this item because it is used in another table.\n"
                    + "In order to delete element at row: " + (id-1)
                    + " additional steps must be taken concerning employees who are presently assigned to this role.", null);
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", null);
        }
        return false;
    }

    @Override
    public List<Position> findResultSet(ResultSet result) {
        List<Position> positions = new ArrayList<>();
        try {
            while (result.next()) {
                Integer id = result.getInt("id");
                String role = result.getString("role");
                String specialization = result.getString("specialization");
                Position position = new Position(id, role, specialization);
                positions.add(position);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL error", e);
        }
        return positions;
    }

    @Override
    public List<Position> findAll() {
        List<Position> position = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            position = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return position;
    }

    @Override
    public Optional<Position> findByPrimaryKey(Pair<Integer,Pair<String,String>> primaryKey) {
        int id = primaryKey.getX();
        String role = primaryKey.getY().getX();
        String speicialization = primaryKey.getY().getY();

        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id=? OR (role=? AND specialization=?)";
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, role);
            statement.setString(3, speicialization);
            final ResultSet resultSet = statement.executeQuery();
            return findResultSet(resultSet).stream().findFirst();
        } catch (final SQLException e) { }
        return Optional.empty();
    }

    /**
     * Retrieves the position at the specified row of the result set of the given query.
     * @param query the query to execute.
     * @param row the row of the result set.
     * @return the position at the specified row of the result set of the given query.
     */
    public Optional<Position> getPositionAtRowWithStatement(final String query, final int row) {
        Optional<Position> position = Optional.empty();
        try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
            final ResultSet resultSet = statement.executeQuery();
            position = Optional.of(findResultSet(resultSet).get(row));
        } catch (Exception e) {
            GUIUtils.exceptionToast("View columns don't match GUI fields", null);
        }
        return position;
    }
}
