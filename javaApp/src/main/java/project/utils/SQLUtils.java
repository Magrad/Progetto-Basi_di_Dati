package project.utils;

import project.db.tables.*;
import project.model.*;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class contains some useful methods for SQL utilities.
 * @param <K> the type of the table.
 */
public final class SQLUtils<K> {

    private SQLUtils() {}

    /**
     * This method is used at the start of the application to
     * verify the integrity of the database.
     * @param connection the connection to the database.
     */
    public static void onFirstLogin(Connection connection) {
        final BuildingsTable buildingsTable = new BuildingsTable(connection);
        final WardsTable wardsTable = new WardsTable(connection);
        final AmbulatoryTable ambulatoryTable = new AmbulatoryTable(connection);
        final PositionsTable positionsTable = new PositionsTable(connection);
        final EmployeesTable employeesTable = new EmployeesTable(connection);
        final AgendasTable agendasTable = new AgendasTable(connection);
        final TimeSlotsTable timeSlotsTable = new TimeSlotsTable(connection);
        final UsersTable usersTable = new UsersTable(connection);
        final TreatmentsTable treatmentsTable = new TreatmentsTable(connection);
        final PrescriptionsTable prescriptionsTable = new PrescriptionsTable(connection);
        final MedicalReportsTable medicalReportsTable = new MedicalReportsTable(connection);
        final BookingsTable bookingsTable = new BookingsTable(connection);

        buildingsTable.createTable();
        wardsTable.createTable();
        ambulatoryTable.createTable();
        positionsTable.createTable();
        employeesTable.createTable();
        agendasTable.createTable();
        timeSlotsTable.createTable();
        usersTable.createTable();
        treatmentsTable.createTable();
        medicalReportsTable.createTable();
        prescriptionsTable.createTable();
        bookingsTable.createTable();

        // Initializes the admin position and employee.
        final Position position = new Position(
                1,
                "admin",
                "");
        final Employee admin = new Employee(
                "MCHNDR97E15C573Y",
                "Andrea",
                "Micheli",
                Utils.buildDate(15, 05, 1997),
                'M',
                1,
                Optional.empty(),
                "admin@admin.bo",
                Utils.passwordEncoder("admin"),
                1);

        // Generates all possible positions.
        ArrayList<Pair<String,String>> positions = new ArrayList<>();
        positions.addAll(
                Stream.concat(
                        Stream.of(new Pair<>(position.getRole(), position.getSpecialization())),
                        Utils.generateAllPossiblePositions().stream()
                ).collect(Collectors.toList())
        );
        int idx = position.getId();
        for (Pair<String,String> p : positions) {
            if (positionsTable.findByPrimaryKey(new Pair<>(idx,p)).isEmpty())
                positionsTable.insertToTable(
                        new Position(
                                idx,
                                p.getX(),
                                p.getY()
                        ));
                idx++;
        }

        // If the numbers of employees is less than 50, generates 50 random
        // new employees.
        if (employeesTable.findAll().size() < 50) {
            ArrayList<Employee> employees = new ArrayList<>();
            employees.addAll(
                    Stream.concat(
                            Stream.of(admin),
                            generateNRandomEmployees(connection, 50).stream()
                    ).collect(Collectors.toList())
            );

            boolean changeOne = true;
            for (Employee e : employees) {
                if (changeOne &&
                        (e.getRole_id() != 1 && e.getRole_id() != 17)) {
                    e = new Employee(
                            e.getCF(),
                            e.getName(),
                            e.getSurname(),
                            e.getBirthday(),
                            e.getGender(),
                            e.getRole_id(),
                            e.getPhone(),
                            "medic@medic.bo",
                            e.getPassword(),
                            e.getPermissions()
                    );
                    changeOne = false;
                }

                if (employeesTable.findByPrimaryKey(e.getCF()).isEmpty())
                    employeesTable.insertToTable(e);
            }
        }

        if (usersTable.findAll().isEmpty()) {
            for (User u : generateNRandomUsers(connection, 10)) {
                if (usersTable.findByPrimaryKey(u.getCF()).isEmpty())
                    usersTable.insertToTable(u);
            }
        }

        // If the buildings table is empty, generates all the buildings.
        if (buildingsTable.findAll().isEmpty()) {
            if (buildingsTable.findAll().isEmpty()) {
                for (Building b : generateBuildings()) {
                    buildingsTable.insertToTable(b);
                }
            }

            // Generates all the wards for each building.
            for (Building b : buildingsTable.findAll()) {
                for (Ward w : generateWardForBuilding(positionsTable,b.getId())) {
                    wardsTable.insertToTable(w);
                }
            }

            // Generates all the ambulatories for each ward.
            for (Ward w: wardsTable.findAll()) {
                for (Ambulatory a : generateRandomAmbulatories(w.getId())) {
                    ambulatoryTable.insertToTable(a);
                }
            }
        }

        // If the treatments table is empty, generates all the treatments.
        if (treatmentsTable.findAll().isEmpty()) {
            for (Treatment t : readAndCreateAllDrugs(treatmentsTable)) {
                treatmentsTable.insertToTable(t);
            }
        }

        bookingsTable.deleteAllPastDays();
        timeSlotsTable.deleteAllPastDays();
    }

    /**
     * Method that generates a random person, containing all the information
     * needed to create a new employee or user.
     * @return an optional containing the person, if the person is valid.
     */
    public static Optional<Person> generateRandomPerson() {
        Pair<String,Character> rndPerson = Utils.generateRandomFullname();
        String name = Utils.smartStringCapitalize(rndPerson.getX().split(" ")[0]);
        String surname = Utils.smartStringCapitalize(Utils.getNameAfterSubstring(rndPerson.getX(), " "));
        Character gender = rndPerson.getY();

        if (gender.equals('?')) {
            return Optional.empty();
        }

        // Generates a random birthday.
        final int day = (int) (Math.random() * 31 + 1);
        final int month = (int) (Math.random() * 12 + 1);
        final int year = (int) (Math.random() * 50 + 1950);
        final Date birthday = Utils.buildDate(day,month,year);
        // Generates at random the last 5 characters of the CF.
        final String rndCharacters = ""+(char) (Math.random() * 26 + 'A')
                + ""+(int) (Math.random() * 1000)
                + ""+(char) (Math.random() * 26 + 'A');

        String CF = Utils.CFgenerator(name,surname,birthday,gender) + rndCharacters;

        // Checks if the CF is valid and if the length is 16.
        if (!Utils.CFValidator(CF,name,surname,birthday,gender) || CF.length() != 16) {
            return Optional.empty();
        }
        return Optional.of(
                new Person(
                        CF,
                        name,
                        surname,
                        birthday,
                        gender
                ));
    }

    /**
     * Method that generates a random user, containing all the information
     * needed to create a new user.
     * @param connection the connection to the database.
     * @return an optional containing the user, if the user is valid.
     */
    public static Optional<User> generateRandomUser(Connection connection) {
        Optional<Person> person = generateRandomPerson();

        if (person.isEmpty()) {
            return Optional.empty();
        }

        Person p = person.get();

        return Optional.of(
                new User(
                        p.getCF(),
                        p.getName(),
                        p.getSurname(),
                        p.getBirthday(),
                        p.getGender(),
                        Optional.empty(),
                        p.getName().toLowerCase() + "." + p.getSurname().toLowerCase() + "@gmail.com",
                        Utils.passwordEncoder(p.getName() + p.getSurname() + "1!")
                ));
    }

    /**
     * Method that generates n random users.
     * @param connection the connection to the database.
     * @param n the number of users to generate.
     * @return an array list containing the employees.
     */
    public static ArrayList<User> generateNRandomUsers(Connection connection, int n) {
        ArrayList<User> users = new ArrayList<>();
        while(users.size() < n) {
            Optional<User> user = generateRandomUser(connection);
            if (user.isPresent()) {
                users.add(user.get());
            }
        }
        return users;
    }

    /**
     * Method that generates a random employee, containing all the information
     * needed to create a new employee and with a random role.
     * @param connection the connection to the database.
     * @return an optional containing the employee, if the employee is valid.
     */
    public static Optional<Employee> generateRandomEmployee(Connection connection) {
        Optional<Person> person = generateRandomPerson();

        if (person.isEmpty()) {
            return Optional.empty();
        }

        Person p = person.get();

        PositionsTable positionsTable = new PositionsTable(connection);
        // Generates a random position id, excluding the first one (the admin).
        int rndPositionId = (int) (Math.random() * (positionsTable.findAll().size() - 1) + 2);
        return Optional.of(
                new Employee(
                        p.getCF(),
                        p.getName(),
                        p.getSurname(),
                        p.getBirthday(),
                        p.getGender(),
                        rndPositionId,
                        Optional.empty(),
                        p.getName().toLowerCase() + "." + p.getSurname().toLowerCase() + "@ospedale.bo",
                        Utils.passwordEncoder("password"),
                        0
                ));
    }

    /**
     * Method that generates n random employees.
     * @param connection the connection to the database.
     * @param n the number of employees to generate.
     * @return an array list containing the employees.
     */
    public static ArrayList<Employee> generateNRandomEmployees(Connection connection, int n) {
        ArrayList<Employee> employees = new ArrayList<>();
        while(employees.size() < n) {
            Optional<Employee> employee = generateRandomEmployee(connection);
            if (employee.isPresent()) {
                employees.add(employee.get());
            }
        }
        return employees;
    }

    /**
     * Method that generates all the hospitals.
     * @return an array list containing all the hospitals.
     */
    public static List<Building> generateBuildings() {
        List<Building> buildings = new ArrayList<>();
        String[] nomi = new String[] {"Ospedale \"M. Bufalini\" di Cesena","Ospedale \"G.Marconi\" di Cesenatico - AUSL della Romagna"};
        String[] indirizzi = new String[] {"Viale Giovanni Ghirotti, 286, 47521 Cesena FC","Viale C. Abba, 102, 47042 Cesenatico FC"};
        String[] city = new String[] {"Cesena","Cesenatico"};
        Integer[] cap = new Integer[] {47521,47042};
        String[] province = new String[] {"FC","FC"};
        String[] region = new String[] {"Emilia-Romagna","Emilia-Romagna"};
        String[] phone = new String[] {"0547 352111","0547 674811"};

        for (int i=0;i<nomi.length;i++) {
            buildings.add(new Building(
                    0,
                    nomi[i],
                    indirizzi[i],
                    city[i],
                    cap[i],
                    province[i],
                    region[i],
                    phone[i]
            ));
        }

        return buildings;
    }

    /**
     * Method that generates all the wards by specialization for a building.
     * @param pos the `PositionsTable` table.
     * @param building_id  the id of the building.
     * @return an array list containing all the wards.
     */
    public static List<Ward> generateWardForBuilding(PositionsTable pos, Integer building_id) {
        List<Ward> wards = new ArrayList<>();

        List<Position> positions = pos.findAll();

        for (int i=0;i<positions.size();i++) {
            if (positions.get(i).getRole().equals("Doctor")) {
                wards.add(new Ward(
                        0,
                        building_id,
                        Character.toString((char) (i-1) + 'A'),
                        positions.get(i).getSpecialization()
                ));
            }
        }

        return wards;
    }

    /**
     * Method that generates a room number based on the hashcode of the employee.
     * Method deprecated to not overload the database by having a room for each
     * employee on each ward at each hospital.
     * @param employee
     * @return
     */
    public static Integer generateRoomNumberBasedOnEmployee(Employee employee) {
        return Math.abs(employee.hashCode()) % 1000 / 2;
    }

    /**
     * Method that generates 5 ambulatories for a ward.
     * @param ward_id the id of the ward.
     * @return an array list containing all the ambulatories.
     */
    public static List<Ambulatory> generateRandomAmbulatories(Integer ward_id) {
        final Integer N_ROOMS = 5;
        List<Ambulatory> ambulatories = new ArrayList<>();

        for (int i=0;i<N_ROOMS;i++) {
            ambulatories.add(new Ambulatory(
                    0,
                    i+1,
                    ward_id
            ));
        }

        return ambulatories;
    }

    /**
     * Method that generates all the drugs available from a txt file.
     * @param treatmentsTable the `TreatmentsTable` table.
     * @return an array list containing all the drugs.
     */
    public static List<Treatment> readAndCreateAllDrugs(TreatmentsTable treatmentsTable) {
        List<Treatment> drugs = new ArrayList<>();

        try {
            final String namesPath = "/src/main/java/project/text/drugs.txt";
            // Reads the file and splits it by `#\n` regex.
            final String[] csvDrugs = Utils.readFile(namesPath).split("#\n");
            for (int i=0; i < csvDrugs.length; i++) {
                // Splits each drug by `;` to get the required fields.
                String[] drug = csvDrugs[i].split(";",-1);
                drugs.add(new Treatment(
                        0,
                        drug[0],
                        drug[1],
                        drug[2],
                        drug[3],
                        drug[4]
                ));
            }
        } catch (final Exception e) {
            GUIUtils.exceptionToast("An error occurred while generating drugs", null);
        }
        return drugs;
    }

    /**
     * Method that adds the `order by` clause to a query based on the column
     * that the user wants the table to be sorted by.
     * @param query the query to add the `order by` clause to.
     * @param orderBy the column to sort by.
     * @param primaryKey the primary key of the table, sometimes useful for
     *                   correctly sorting the table.
     * @return the query with the `order by` clause.
     */
    public static String orderQueryByTableSortingOrder(String query, final List<Pair<String,String>> orderBy, String primaryKey) {
        if (!orderBy.isEmpty()) {
            String column = orderBy.get(0).getX();
            String order = orderBy.get(0).getY();
            query += " ORDER BY " + column;
            if (!primaryKey.isEmpty()) {
                query += "," + primaryKey + " ";
            }
            query += order;
        }
        return query;
    }

    /**
     * Method that based on a query and possible primary keys, shows the
     * relative data in a table.
     * (This method was used because it was the only way to fill the table
     * with the required data).
     * @param connection the connection to the database.
     * @param query the query to execute.
     * @param primaryKey the primary keys of the table.
     * @return a pair containing the column names and the rows data.
     */
    public static Pair<String[],ArrayList<Object[]>> showDataInTable(Connection connection, String query, Object[] primaryKey) {
        String[] colName = {};
        ArrayList<Object[]> rowsData = new ArrayList<>();
        try {
            final PreparedStatement st = connection.prepareStatement(query);

            // Count how many `?` are in the query.
            int count = 0;
            for(int i=0;i<query.length();i++) {
                if (query.charAt(i) == '?') {
                    count++;
                }
            }

            // Check if the number of keys is the same as the number of `?`.
            if (count != primaryKey.length) {
                GUIUtils.exceptionToast("The number of primary keys is not correct", null);
                return new Pair<>(colName,rowsData);
            }

            for(int i=0;i<primaryKey.length;i++) {
                st.setObject(i+1,primaryKey[i]);
            }

            ResultSet rs = st.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();

            int cols = rsmd.getColumnCount();
            // Get the column names.
            colName = new String[cols];
            for(int i=0;i<cols;i++) {
                String name = rsmd.getColumnName(i+1);
                name = Utils.smartStringCapitalize(name);
                colName[i] = name;
            }
            // Get each row data.
            while(rs.next()) {
                Object[] rowData = new Object[cols];
                for(int i=0;i<cols;i++) {
                    rowData[i] = rs.getObject(i + 1);
                    if(rs.getObject(i+1) == null) {
                        rowData[i] = "null";
                    }
                }
                rowsData.add(rowData);
            }
            // Close the result set and the statement, required in order
            // for the table to be correctly filled without errors.
            st.close();

        } catch (SQLException sql) {
            GUIUtils.exceptionToast("An error as occurred during the initialization of the " + Utils.getNameAfterSubstring(query,"from ") + "'s table", sql);
            return new Pair<>(colName,rowsData);
        }
        return new Pair<>(colName,rowsData);
    }
}
