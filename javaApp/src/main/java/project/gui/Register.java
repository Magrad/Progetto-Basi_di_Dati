package project.gui;

import com.toedter.calendar.JDateChooser;
import project.db.tables.UsersTable;
import project.model.User;
import project.utils.GUIUtils;
import project.utils.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * This class represents the registration form,
 * it allows the user to register to the system.
 */
public class Register extends JFrame implements FrameLauncher {

    private static UsersTable usersTable;
    private static Connection connection;
    private static JFrame thisFrame;
    private static JFrame callerFrame;

    private JPanel panelMain;
    private JButton registerBtn;
    private JTextField nameTxt;
    private JTextField surnameTxt;
    private JTextField CF;
    private JPanel jdate;
    private JDateChooser dateChooser;
    private JRadioButton maleRadioButton;
    private JRadioButton femaleRadioButton;
    private JTextField phoneTxt;
    private JTextField emailTxt;
    private JPasswordField passwordTxt;
    private JPasswordField passwordConfTxt;
    private JButton backBtn;

    public Register() {
        getContentPane().setLayout(null);

        // Adds a date chooser to the date panel, in order for the user to select
        // their birthday.
        dateChooser = new JDateChooser();
        jdate.add(dateChooser);

        // Add a listener to the `maleRadioButton` and `femaleRadioButton` in order
        // to uncheck the other one when one is checked.
        maleRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                femaleRadioButton.setSelected(false);
            }
        });
        femaleRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maleRadioButton.setSelected(false);
            }
        });
        // Add a listener to the `registerBtn` in order to register the user to the system.
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameTxt.getText();
                String surname = surnameTxt.getText();
                String cf = CF.getText().toUpperCase();
                Date birthday = dateChooser.getDate();
                Character gender = maleRadioButton.isSelected() ? 'M' : femaleRadioButton.isSelected() ? 'F' : '?';
                Optional<String> phone = Optional.ofNullable(phoneTxt.getText()).filter(s -> !s.isEmpty());
                String email = emailTxt.getText();
                String password = passwordTxt.getText();
                String passwordCheck = passwordConfTxt.getText();

                // Checks if all the required fields are filled,
                // if not, it shows a toast message.
                if(GUIUtils.emptyFieldToast(name, "name")
                        || GUIUtils.emptyFieldToast(surname, "surname")
                        || GUIUtils.emptyFieldToast(cf, "cf")) {
                    return;
                }
                // Checks if no gender has been chosen.
                if (gender.equals('?')) {
                    GUIUtils.exceptionToast("No gender has been chosen, select a valid gender.", null);
                    return;
                }
                // Tests if the `name`, `surname`, `birthday` and `gender` fields
                // match the `cf` field, if not, it shows a toast message.
                if(!Utils.CFValidator(cf, name, surname, birthday, gender)) {
                    GUIUtils.exceptionToast("CF text field doesn't match user's information, enter a valid TIN.", null);
                    GUIUtils.clearField(CF);
                    return;
                }
                // Checks if the `email` field is empty or if it doesn't match the
                // email required regex.
                if (email.isEmpty() || !Utils.emailValidator(email)) {
                    GUIUtils.exceptionToast("Email field is empty.\n" +
                            "Email must contains characters: \'a-z,A-Z,0-9 and at least one special character.\'", null);
                    GUIUtils.clearField(emailTxt);
                    return;
                }
                // Checks if the `password` field is empty or if it doesn't match the
                // password required regex.
                if (password.isEmpty() || !Utils.passwordValidator(password)) {
                    GUIUtils.exceptionToast("Password field is empty.\n" +
                            "Password must contains characters: \'a-z,A-Z,0-9 and at least one special character.\'", null);
                    GUIUtils.clearField(passwordTxt);
                    GUIUtils.clearField(passwordConfTxt);
                    return;
                }
                // Checks if both the password fields match.
                if (!password.equals(passwordCheck)) {
                    GUIUtils.exceptionToast("Different passwords entered.", null);
                    GUIUtils.clearField(passwordConfTxt);
                    return;
                }

                if (!usersTable.insertToTable(
                        new User(cf,
                                name,
                                surname,
                                birthday,
                                gender,
                                phone,
                                email,
                                Utils.passwordEncoder(password)
                        ))) {
                    GUIUtils.exceptionToast("An error has occurred during account creation, please try again.", null);
                    GUIUtils.clearAll(thisFrame);
                    return;
                };

                GUIUtils.exceptionToast("Account created successfully.", null);
                thisFrame.dispose();
                callerFrame.setVisible(true);
            }
        });
        // Add a listener to the `backBtn` in order to go back to the
        // login form.
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thisFrame.dispose();
                callerFrame.setVisible(true);
            }
        });
    }

    /**
     * Constructor for the Register class.
     * @param connection the connection to the database.
     */
    public Register(final Connection connection) {
        this.connection = Objects.requireNonNull(connection);
        this.usersTable = new UsersTable(this.connection);
    }

    @Override
    public void Launcher(String name, JFrame caller) {
        callerFrame = caller;
        thisFrame = new JFrame(name);
        thisFrame.setContentPane(new Register().panelMain);
        thisFrame.pack();
        thisFrame.setLocationRelativeTo(null);
        thisFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        thisFrame.setResizable(false);
        thisFrame.setVisible(true);
    }
}
