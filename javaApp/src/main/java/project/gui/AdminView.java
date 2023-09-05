package project.gui;

import com.toedter.calendar.JDateChooser;
import project.db.tables.TreatmentsTable;
import project.db.tables.EmployeesTable;
import project.db.tables.PositionsTable;
import project.model.Treatment;
import project.model.Employee;
import project.model.Position;
import project.utils.GUIUtils;
import project.utils.Pair;
import project.utils.SQLUtils;
import project.utils.Utils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.List;

/**
 * This class is used to display the admin view.
 * It is used by the admin for:
 * - managing employees
 * - managing positions
 * - managing drugs
 */
public class AdminView extends JFrame implements FrameLauncher {

    // Enum used to switch between the different views and keep track of the current one.
    private static enum ViewType {
        EMPLOYEES,
        POSITIONS,
        DRUGS
    }

    private static final String ADD_NEW = "Add new ";
    private static final String MANAGE_TABLE = "Manage ";
    private static final String MODIFY = "Modify";
    private static final String ADD = "Add";

    private static Connection connection;
    private static Employee employee;
    private static ViewType view = ViewType.EMPLOYEES;
    private static int selectedRow = -1;
    private static String query;
    private static Object[] lateBinding = new Object[]{};
    private static final List<Pair<String,String>> orderBy = new ArrayList<>();
    private static Integer[] tableSettings = new Integer[]{};

    private static JFrame thisFrame;
    private static JFrame callerFrame;
    private JTabbedPane selectionPane;
    private JButton employeesBtn;
    private JButton positionsBtn;
    private JButton drugsBtn;
    private JPanel panelMain;
    private JButton addNewBtn;
    private JTable sqlTable;
    private JButton modifyOldBtn;
    private JTextField txtField1;
    private JTextField txtField2;
    private JTextField txtField3;
    private JPanel jdate;
    private JDateChooser dateChooser;
    private JTextField txtField4;
    private JTextField txtField5;
    private JPasswordField passwordTxt;
    private JButton addBtn;
    private JLabel employeeName;
    private JButton backBtn;
    private JTextField txtField6;
    private JComboBox roleCB;
    private JRadioButton maleRB;
    private JRadioButton femaleRB;
    private JButton deleteOldBtn;
    private JButton logoutBtn;
    private JLabel manageLbl;
    private JPanel selectTable;
    private JPanel insertData;
    private JButton switchToMedicViewButton;
    private JScrollPane scrollPanel;

    public AdminView() {
        // Initializes the user interface, and shows the current user's name and role.
        employeeName.setText(getEmployeeNameAndRole());

        // Adds a date chooser to the date panel, in order for the admin to select
        // the date of birth of an employee.
        dateChooser = new JDateChooser();
        jdate.add(dateChooser);

        // Fill the combo box with all the available positions.
        PositionsTable positionsTable = new PositionsTable(connection);
        List<Position> positions = positionsTable.findAll();
        for (Position position : positions) {
            roleCB.addItem(position.toString());
        }

        GUIUtils.hideTabsInTabbedPane(selectionPane);

        loadViewByIndex(ViewType.EMPLOYEES);

        // Add a listener to the table, in order to keep track of the selected row.
        sqlTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get the column name of the column clicked.
                String col = sqlTable.getModel().getColumnName(sqlTable.columnAtPoint(e.getPoint())).toLowerCase();
                // Checks if it is the first time the users clicks on a column,
                // if so it will sort ascending, if the user clicks on the same
                // column again it will sort descending, if the user clicks on
                // a different column it will sort ascending.
                if (orderBy.isEmpty()) {
                    orderBy.add(new Pair<>(col, " ASC"));
                } else if (orderBy.get(0).getX().equals(col)) {
                    if (orderBy.get(0).getY().equals(" ASC")) {
                        orderBy.removeAll(orderBy);
                        orderBy.add(new Pair<>(col, " DESC"));
                    } else {
                        orderBy.removeAll(orderBy);
                        orderBy.add(new Pair<>(col, " ASC"));
                    }
                } else {
                    orderBy.removeAll(orderBy);
                    orderBy.add(new Pair<>(col, " ASC"));
                }

                // Reload the table with the new order by clause.
                GUIUtils.showQueryInTable(connection, sqlTable, query, lateBinding, orderBy, tableSettings);
            }
        });
        // Add a listener to the `sqlTable` table, in order to get the selected row.
        ListSelectionModel selectionModel = sqlTable.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!selectionModel.isSelectionEmpty()) {
                    selectedRow = selectionModel.getMinSelectionIndex();
                }
            }
        });

        // Add a listener to the `employeeBtn` button, in order to switch to the
        // employees view.
        employeesBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manageLbl.setText(MANAGE_TABLE + "employees");
                addNewBtn.setText(ADD_NEW + "employee");
                sqlTable.clearSelection();
                view = ViewType.EMPLOYEES;
                loadViewByIndex(view);
            }
        });
        // Add a listener to the `positionsBtn` button, in order to switch to the
        // positions view.
        positionsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manageLbl.setText(MANAGE_TABLE + "positions");
                addNewBtn.setText(ADD_NEW + "position");
                sqlTable.clearSelection();
                view = ViewType.POSITIONS;
                loadViewByIndex(view);
            }
        });
        // Add a listener to the `drugsBtn` button, in order to switch to the
        // drugs view.
        drugsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manageLbl.setText(MANAGE_TABLE + "drugs");
                addNewBtn.setText(ADD_NEW + "drug");
                sqlTable.clearSelection();
                view = ViewType.DRUGS;
                loadViewByIndex(view);
            }
        });
        // Add a listener to the `logoutBtn` button, in order for the user to
        // log out and return to the login view.
        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thisFrame.dispose();
                callerFrame.setVisible(true);
            }
        });
        // Add a listener to the `switchToMedicViewButton` button, in order to
        // switch to the medic view.
        switchToMedicViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtils.clearAll(thisFrame);
                MedicView medicView = new MedicView(connection, employee);
                medicView.Launcher("Medic View", callerFrame);
                thisFrame.dispose();
            }
        });
        // Add a listener to the `addNewBtn` button, in order to switch to the
        // insert data view.
        addNewBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionPane.setSelectedIndex(1);
                addBtn.setText(ADD);
                sqlTable.clearSelection();
                selectedRow = -1;

                createUIComponents(view);
                GUIUtils.clearAll(thisFrame);
            }
        });
        // Add a listener to the `addNewBtn` button, in order to switch to the
        // insert data view whilst loading all the data from the selected row.
        // If no row is selected, an error message will be shown.
        modifyOldBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedRow == -1) {
                    GUIUtils.exceptionToast("Please select an employee that needs their credentials updated", null);
                    return;
                }
                // Switch to the insert data view.
                selectionPane.setSelectedIndex(1);

                addBtn.setText(MODIFY);
                if (view.equals(ViewType.EMPLOYEES)) {
                    createUIComponents(view);
                    EmployeesTable employeesTable = new EmployeesTable(connection);
                    String queryOrdered = SQLUtils.orderQueryByTableSortingOrder(query,orderBy,"");
                    Optional<Employee> employee = employeesTable.getEmployeeAtRowWithStatement(queryOrdered, selectedRow);

                    if(employee.isEmpty()) {
                        sqlTable.clearSelection();
                        GUIUtils.exceptionToast("The selected employee does not exist anymore", null);
                        return;
                    }

                    // Sets all the data fields to the data of the selected row.
                    txtField1.setText(employee.get().getName());
                    txtField2.setText(employee.get().getSurname());
                    txtField3.setText(employee.get().getCF());
                    dateChooser.setDate(employee.get().getBirthday());
                    maleRB.setSelected(employee.get().getGender().equals('M'));
                    femaleRB.setSelected(employee.get().getGender().equals('F'));
                    roleCB.setSelectedIndex(employee.get().getRole_id() - 1);
                    txtField4.setText(employee.get().getPhone().orElse(""));
                    txtField5.setText(employee.get().getEmail());
                    txtField6.setText("" + employee.get().getPermissions());
                }
                if (view.equals(ViewType.POSITIONS)) {
                    createUIComponents(view);
                    PositionsTable positionsTable = new PositionsTable(connection);
                    String queryOrdered = SQLUtils.orderQueryByTableSortingOrder(query,orderBy,"");
                    Optional<Position> position = positionsTable.getPositionAtRowWithStatement(queryOrdered, selectedRow);

                    if(position.isEmpty()) {
                        sqlTable.clearSelection();
                        GUIUtils.exceptionToast("The selected position does not exist anymore", null);
                        return;
                    }

                    // Sets all the data fields to the data of the selected row.
                    txtField1.setText("" + position.get().getId());
                    txtField2.setText(position.get().getRole());
                    txtField3.setText(position.get().getSpecialization());
                }
                if (view.equals(ViewType.DRUGS)) {
                    createUIComponents(view);
                    TreatmentsTable treatmentsTable = new TreatmentsTable(connection);
                    String queryOrdered = SQLUtils.orderQueryByTableSortingOrder(query,orderBy,"");
                    Optional<Treatment> drug = treatmentsTable.getTreatmentAtRowWithStatement(queryOrdered, selectedRow);

                    if(drug.isEmpty()) {
                        sqlTable.clearSelection();
                        GUIUtils.exceptionToast("The selected drug does not exist anymore", null);
                        return;
                    }

                    // Sets all the data fields to the data of the selected row.
                    txtField1.setText("" + drug.get().getId());
                    txtField2.setText(drug.get().getName());
                    txtField3.setText(drug.get().getType());
                    txtField4.setText(drug.get().getDescription());
                    txtField5.setText(drug.get().getPosology());
                    txtField6.setText(drug.get().getAllergens());
                }
            }
        });
        // Add a listener to the `deleteOldBtn` button, in order to delete the
        // selected row. If no row is selected, an error message will be shown.
        deleteOldBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedRow == -1) {
                    GUIUtils.exceptionToast("Please select an employee that needs to be deleted", null);
                    return;
                }

                if (view.equals(ViewType.EMPLOYEES)) {
                    EmployeesTable employeesTable = new EmployeesTable(connection);
                    String queryOrdered = SQLUtils.orderQueryByTableSortingOrder(query,orderBy,"");
                    Optional<Employee> row = employeesTable.getEmployeeAtRowWithStatement(queryOrdered, selectedRow);

                    if (row.isEmpty()) {
                        sqlTable.clearSelection();
                        return;
                    }

                    // Checks once again if the user really wants to delete the selected row.
                    if (GUIUtils.yesNoToast("Are you sure you want to delete row " + selectedRow + "?")) {
                        employeesTable.deleteFromTable(row.get().getCF());
                        // Checks if the user deleted their own credentials, in which case the
                        // program will return to the login screen.
                        if (employee.getCF().equals(row.get().getCF())) {
                            GUIUtils.exceptionToast("You deleted your own credentials, please login again", null);
                            thisFrame.dispose();
                            callerFrame.setVisible(true);
                        }
                        loadViewByIndex(view);
                    }
                }
                if (view.equals(ViewType.POSITIONS)) {
                    PositionsTable positionsTable = new PositionsTable(connection);
                    String queryOrdered = SQLUtils.orderQueryByTableSortingOrder(query,orderBy,"");
                    Optional<Position> position = positionsTable.getPositionAtRowWithStatement(queryOrdered, selectedRow);

                    if (position.isEmpty()) {
                        sqlTable.clearSelection();
                        return;
                    }

                    // Checks once again if the user really wants to delete the selected row.
                    if (GUIUtils.yesNoToast("Are you sure you want to delete row " + selectedRow + "?")) {
                        positionsTable.deleteFromTable(new Pair<>(position.get().getId(),new Pair<>("","")));
                        loadViewByIndex(view);
                    }
                }
                if (view.equals(ViewType.DRUGS)) {
                    TreatmentsTable treatmentsTable = new TreatmentsTable(connection);
                    String queryOrdered = SQLUtils.orderQueryByTableSortingOrder(query,orderBy,"");
                    Optional<Treatment> treatment = treatmentsTable.getTreatmentAtRowWithStatement(queryOrdered, selectedRow);

                    if (treatment.isEmpty()) {
                        sqlTable.clearSelection();
                        return;
                    }

                    // Checks once again if the user really wants to delete the selected row.
                    if (GUIUtils.yesNoToast("Are you sure you want to delete row " + selectedRow + "?")) {
                        treatmentsTable.deleteFromTable(
                                new Pair<>(
                                        treatment.get().getId(),
                                        treatment.get().getName())
                        );
                        loadViewByIndex(view);
                    }
                }
            }
        });
        // Add a listener to the `addBtn` button, in order to add a new row to the
        // table.
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sqlTable.clearSelection();

                if(view.equals(ViewType.EMPLOYEES) && createUpdateEmployee()) {
                    selectionPane.setSelectedIndex(0);
                    loadViewByIndex(view);
                    addBtn.setText(ADD);
                }
                if(view.equals(ViewType.POSITIONS) && createUpdatePosition()) {
                    selectionPane.setSelectedIndex(0);
                    loadViewByIndex(view);
                    addBtn.setText(ADD);
                }
                if(view.equals(ViewType.DRUGS) && createUpdateDrug()) {
                    selectionPane.setSelectedIndex(0);
                    loadViewByIndex(view);
                    addBtn.setText(ADD);
                }
            }
        });
        // Add a listener to the `backBtn` button, in order to go back to the
        // main menu.
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtils.clearAll(thisFrame);
                view = ViewType.EMPLOYEES;
                selectionPane.setSelectedIndex(0);
                addBtn.setText(ADD);
                selectedRow = -1;
                sqlTable.clearSelection();
            }
        });
    }

    /**
     * Constructor for the AdminView class.
     * @param provider the connection to the database.
     * @param employee the employee that is currently logged in.
     */
    public AdminView(final Connection provider,final Employee employee) {
        this.connection = Objects.requireNonNull(provider);
        this.employee = employee;
    }

    @Override
    public void Launcher(final String name, final JFrame caller) {
        callerFrame = caller;
        thisFrame = new JFrame(name);
        thisFrame.setContentPane(new AdminView().panelMain);
        thisFrame.pack();
        thisFrame.setLocationRelativeTo(null);
        thisFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        thisFrame.setResizable(false);
        thisFrame.setVisible(true);
    }

    /**
     * Method that sets the user information in the top left corner of the
     * frame.
     * @return the string to be displayed. Combines the employee's name and
     * role.
     */
    private String getEmployeeNameAndRole() {
        PositionsTable positionsTable = new PositionsTable(connection);
        Optional<Position> position = positionsTable.findByPrimaryKey(new Pair<>(employee.getRole_id(), new Pair<>("","")));
        String role = position.isEmpty() ? "Employee" : position.get().getRole();
        String specialization = position.isEmpty() || position.get().getSpecialization().isEmpty() ? ""
                : " (" + position.get().getSpecialization() + ")";
        return "Logged in: " + employee.getName() + " - " + role + specialization;
    }

    /**
     * Method that initializes all the components of the frame, based
     * on the view that is currently selected
     * @param view the view that is currently selected.
     */
    private void createUIComponents(ViewType view) {
        if (view.equals(ViewType.EMPLOYEES)) {
            GUIUtils.hideShowWithSequence(insertData, "0");
            GUIUtils.hideShowWithSequence(insertData, "");
            GUIUtils.modifyLabelText(insertData,
                    new String[] {"Name",
                            "Surname",
                            "CF",
                            "Birthday",
                            "Gender",
                            "Role",
                            "Phone",
                            "Email",
                            "Password",
                            "Permissions"
                    });
        }
        if (view.equals(ViewType.POSITIONS)) {
            GUIUtils.hideShowWithSequence(insertData, "0");
            GUIUtils.hideShowWithSequence(insertData, "001111");
            GUIUtils.modifyLabelText(insertData,
                    new String[] {"Role",
                            "Specialization"
                    });
        }
        if (view.equals(ViewType.DRUGS)) {
            GUIUtils.hideShowWithSequence(insertData, "0");
            GUIUtils.hideShowWithSequence(insertData, "0011110000000011110011");
            GUIUtils.modifyLabelText(insertData,
                    new String[] {"Name",
                            "Type",
                            "Description",
                            "Posology",
                            "Allergens"
                    });
        }
    }

    /**
     * Method that load the correct view of the `sqlTable` table, depending on
     * the `view` parameter.
     * @param view the current view.
     */
    private void loadViewByIndex(ViewType view) {
        orderBy.clear();
        if (view.equals(ViewType.EMPLOYEES)) {
            query = "SELECT * FROM " + EmployeesTable.TABLE_NAME;
            tableSettings = new Integer[]{}; // No settings.
            lateBinding = new Object[]{}; // No late binding.
            GUIUtils.showQueryInTable(connection, sqlTable, query, lateBinding, orderBy, tableSettings);
        }
        if (view.equals(ViewType.POSITIONS)) {
            query = "SELECT * FROM " + PositionsTable.TABLE_NAME;
            tableSettings = new Integer[]{0}; // Hide id.
            lateBinding = new Object[]{}; // No late binding.
            GUIUtils.showQueryInTable(connection, sqlTable, query, lateBinding, orderBy, tableSettings);
        }
        if (view.equals(ViewType.DRUGS)) {
            query = "SELECT * FROM " + TreatmentsTable.TABLE_NAME;
            tableSettings = new Integer[]{0}; // Hide id.
            lateBinding = new Object[]{}; // No late binding.
            GUIUtils.showQueryInTable(connection, sqlTable, query, lateBinding, orderBy, tableSettings);
        }
    }

    /**
     * Method that creates or updates an employee in the database
     * based on the data inserted in the `insertData` panel.
     * @return true if the operation is successful, false otherwise.
     */
    private boolean createUpdateEmployee() {
        String name = txtField1.getText();
        String surname = txtField2.getText();
        String cf = txtField3.getText().toUpperCase();
        Date birthday = dateChooser.getDate();
        Character gender = maleRB.isSelected() ? 'M' : femaleRB.isSelected() ? 'F' : '?';
        Integer role = roleCB.getSelectedIndex();
        Optional<String>  phone = Optional.ofNullable(txtField4.getText()).filter(s -> !s.isEmpty());
        String email = txtField5.getText();
        String password = passwordTxt.getText();
        Integer permissions = Utils.parseInt(txtField6.getText());

        // Checks if all the required fields are filled,
        // if not, it shows a toast message.
        if(GUIUtils.emptyFieldToast(name,"name")
                || GUIUtils.emptyFieldToast(surname,"surname")
                || GUIUtils.emptyFieldToast(cf,"cf")
                || GUIUtils.emptyFieldToast(birthday,"")
                || GUIUtils.emptyFieldToast(gender.toString(),"")
                || GUIUtils.emptyFieldToast(role.toString(),"role")
                || GUIUtils.emptyFieldToast(email,"email")
                || GUIUtils.emptyFieldToast(password,"password")
                || GUIUtils.emptyFieldToast(txtField6.getText(),"permission")
                || GUIUtils.CFFieldNotValidatedToast(cf,name,surname,birthday,gender, txtField3)
                || GUIUtils.emailNotValidatedToast(email, txtField5)) {
            return false;
        }

        EmployeesTable employeesTable = new EmployeesTable(connection);
        // If the button text is "Modify", it updates the selected employee.
        if(addBtn.getText().equals(MODIFY)) {
            Optional<Employee> employee = Optional.empty();

            if (!txtField3.getText().isEmpty()) {
                String queryOrdered = SQLUtils.orderQueryByTableSortingOrder(query,orderBy,"");
                employee = employeesTable.getEmployeeAtRowWithStatement(queryOrdered, selectedRow);
            }

            if (employee.isEmpty()) {
                GUIUtils.exceptionToast("Failed loading in employee's informations", null);
                return false;
            }

            Employee updated = new Employee(
                    cf,
                    name,
                    surname,
                    birthday,
                    gender,
                    role+1,
                    phone,
                    email,
                    Utils.passwordEncoder(password),
                    permissions
            );

            if (!employeesTable.updateTable(updated, employee.get().getCF())) {
                GUIUtils.exceptionToast("An error has occurred during account update, please try again.", null);
                GUIUtils.clearAll(thisFrame);
                return false;
            };
            GUIUtils.exceptionToast("Employee's informations updated correctly", null);
            loadViewByIndex(view);
            GUIUtils.clearAll(thisFrame);

            // If the updated employee is the current one, it updates the current employee's information
            // and if the permissions have changed, it logs out the user.
            if (this.employee.getCF().equals(employee.get().getCF())) {
                this.employee = updated;
                employeeName.setText(getEmployeeNameAndRole());
                if (permissions != 1) {
                    GUIUtils.exceptionToast("You have changed your permissions, please login again", null);
                    loadViewByIndex(view);
                    GUIUtils.clearAll(thisFrame);
                    callerFrame.setVisible(true);
                    thisFrame.dispose();
                    return false;
                }
            }
            return true;
        }

        if (!employeesTable.insertToTable(
                new Employee(cf,
                        name,
                        surname,
                        birthday,
                        gender,
                        role+1,
                        phone,
                        email,
                        Utils.passwordEncoder(password),
                        permissions
                ))) {
            GUIUtils.exceptionToast("An error has occurred during account insertion, please try again.", null);
            GUIUtils.clearAll(thisFrame);
            return false;
        };
        GUIUtils.exceptionToast("Employee created successfully", null);
        loadViewByIndex(view);
        GUIUtils.clearAll(thisFrame);
        return true;
    }

    /**
     * Method that creates or updates a position in the database
     * based on the data inserted in the `insertData` panel.
     * @return true if the operation is successful, false otherwise.
     */
    private boolean createUpdatePosition() {
        Integer id = Utils.parseInt(txtField1.getText());
        String role = txtField2.getText();
        String specialization = txtField3.getText();

        if (GUIUtils.emptyFieldToast(role,"role")) {
            return false;
        }

        PositionsTable positionsTable = new PositionsTable(connection);
        if(addBtn.getText().equals(MODIFY)) {
            Optional<Position> position = Optional.empty();

            if (!txtField1.getText().isEmpty()) {
                String queryOrdered = SQLUtils.orderQueryByTableSortingOrder(query,orderBy,"");
                position = positionsTable.getPositionAtRowWithStatement(queryOrdered, selectedRow);
            }

            if (position.isEmpty()) {
                GUIUtils.exceptionToast("Failed loading in position's informations", null);
                return false;
            }

            if (!positionsTable.updateTable(
                    new Position(id,
                            role,
                            specialization),
                    new Pair<>(position.get().getId(),
                            new Pair<>(position.get().getRole(), position.get().getSpecialization()))
            )) {
                GUIUtils.exceptionToast("An error has occurred during position update, please try again.", null);
                GUIUtils.clearAll(thisFrame);
                return false;
            };
            GUIUtils.exceptionToast("Position's informations updated correctly", null);
            employeeName.setText(getEmployeeNameAndRole());
            view = ViewType.EMPLOYEES;
            loadViewByIndex(view);
            GUIUtils.clearAll(thisFrame);
            return true;
        }

        if (!positionsTable.insertToTable(
                new Position(0,
                        role,
                        specialization
                ))) {
            GUIUtils.exceptionToast("An error has occurred during position insertion, please try again.", null);
            GUIUtils.clearAll(thisFrame);
            return false;
        };
        GUIUtils.exceptionToast("Position created successfully", null);
        view = ViewType.EMPLOYEES;
        loadViewByIndex(view);
        GUIUtils.clearAll(thisFrame);
        return true;
    }

    /**
     * Method that creates or updates a drug in the database
     * based on the data inserted in the `insertData` panel.
     * @return true if the operation is successful, false otherwise.
     */
    private boolean createUpdateDrug() {
        final Integer id = Utils.parseInt(txtField1.getText());
        final String name = txtField2.getText();
        final String type = txtField3.getText();
        final String description = txtField4.getText();
        final String posology = txtField5.getText();
        final String allergens = txtField6.getText();

        if (GUIUtils.emptyFieldToast(name,"name")
                || GUIUtils.emptyFieldToast(type,"type")
                || GUIUtils.emptyFieldToast(description,"description")
                || GUIUtils.emptyFieldToast(posology,"posology")
                || GUIUtils.emptyFieldToast(allergens,"allergens")) {
            return false;
        }

        TreatmentsTable treatmentsTable = new TreatmentsTable(connection);
        if(addBtn.getText().equals(MODIFY)) {
            Optional<Treatment> treatment = Optional.empty();

            if (!txtField1.getText().isEmpty()) {
                String queryOrdered = SQLUtils.orderQueryByTableSortingOrder(query,orderBy,"");
                treatment = treatmentsTable.getTreatmentAtRowWithStatement(queryOrdered, selectedRow);
            }

            if (treatment.isEmpty()) {
                GUIUtils.exceptionToast("Failed loading in drug's informations", null);
                return false;
            }

            if (!treatmentsTable.updateTable(
                    new Treatment(id,
                            name,
                            type,
                            description,
                            posology,
                            allergens
                    ), new Pair<>(
                            treatment.get().getId(),
                            treatment.get().getName()))) {
                GUIUtils.exceptionToast("An error has occurred during drug update, please try again.", null);
                GUIUtils.clearAll(thisFrame);
                return false;
            };
            GUIUtils.exceptionToast("Drug's informations updated correctly", null);
            view = ViewType.EMPLOYEES;
            loadViewByIndex(view);
            GUIUtils.clearAll(thisFrame);
            return true;
        }

        if (!treatmentsTable.insertToTable(
                new Treatment(0,
                        name,
                        type,
                        description,
                        posology,
                        allergens
                ))) {
            GUIUtils.exceptionToast("An error has occurred during drug insertion, please try again.", null);
            GUIUtils.clearAll(thisFrame);
            return false;
        };
        GUIUtils.exceptionToast("Drug created successfully", null);
        view = ViewType.EMPLOYEES;
        loadViewByIndex(view);
        GUIUtils.clearAll(thisFrame);
        return true;
    }
}
