package project.gui;

import project.db.tables.EmployeesTable;
import project.db.tables.UsersTable;
import project.model.Employee;
import project.model.User;
import project.utils.GUIUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.Objects;
import java.util.Optional;

/**
 * This class represents the login frame, it allows the user
 * to log in as a user or as an employee or to register as a user.
 */
public class Login extends JFrame implements FrameLauncher {

    private static UsersTable usersTable;
    private static EmployeesTable employeesTable;
    private static Connection connection;
    private static JFrame thisFrame;

    private JPanel panelMain;
    private JButton btn_register;
    private JTextField usernameTxt;
    private JPasswordField passwordTxt;
    private JButton loginButton;
    private JLabel usernameLbl;
    private JLabel passwordLbl;
    private JRadioButton employeeRadioButton;

    public Login() {
        // Add a listener to the `loginButton` button, when clicked
        // it will try to log in the user or the employee (as Admin
        // or normal employee).
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameTxt.getText();
                String password = passwordTxt.getText();
                boolean isEmployee = employeeRadioButton.isSelected();

                Optional<User> user = Optional.empty();
                Optional<Employee> employee = Optional.empty();

                // Based on the selected radio button, it will try to log in
                // as an employee or as a user.
                if(isEmployee) {
                    employee = employeesTable.logIn(username, password);
                } else {
                    user = usersTable.logIn(username, password);
                }

                // If the username or the password are wrong, it will show
                // an error message without giving away which of the two
                // is wrong.
                if(user.isEmpty() && employee.isEmpty()) {
                    GUIUtils.exceptionToast("Username or password don't match any known " + (isEmployee ? "employee" : "user") + "'s account.", null);
                    GUIUtils.clearField(passwordTxt);
                    return;
                }

                thisFrame.setVisible(false);
                if(employeeRadioButton.isSelected()) {
                    Employee registered = employee.get();

                    // Login as employee
                    int permissions = employeesTable.getEmployeePermissions(registered.getCF());
                    GUIUtils.clearAll(thisFrame);

                    // If the employee is an admin, it will open the admin view
                    if(permissions == 1) {
                        AdminView adminView = new AdminView(connection, registered);
                        adminView.Launcher("Admin View", thisFrame);
                        return;
                    }

                    GUIUtils.clearAll(thisFrame);
                    MedicView medicView = new MedicView(connection, registered);
                    medicView.Launcher("Medic View", thisFrame);
                    return;
                }

                // Login as user
                GUIUtils.clearAll(thisFrame);
                User registered = user.get();
                UserView userView = new UserView(connection, registered);
                userView.Launcher("User View", thisFrame);
            }
        });
        // Add a listener to the `btn_register` button, when clicked
        // it will open the register frame.
        btn_register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Register register = new Register(connection);
                register.Launcher("Register", thisFrame);
                thisFrame.setVisible(false);
            }
        });
    }

    /**
     * This constructor is used to initialize the connection
     * to the database.
     * @param provider
     */
    public Login(Connection provider) {
        this.connection = Objects.requireNonNull(provider);
        this.usersTable = new UsersTable(this.connection);
        this.employeesTable = new EmployeesTable(this.connection);
    }

    @Override
    public void Launcher(String name, JFrame caller) {
        thisFrame = new JFrame(name);
        thisFrame.setContentPane(new Login().panelMain);
        thisFrame.pack();
        thisFrame.setLocationRelativeTo(null);
        thisFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        thisFrame.setResizable(false);
        thisFrame.setVisible(true);
    }
}