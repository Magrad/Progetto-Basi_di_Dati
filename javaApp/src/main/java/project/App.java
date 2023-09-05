package project;

import project.db.ConnectionProvider;
import project.utils.GUIUtils;
import project.utils.SQLUtils;
import project.utils.Utils;
import project.gui.Login;

/**
 * Main class of the project, it creates the connection to
 * the database, creates the tables if they don't exist and
 * launches the login frame.
 */
public class App {
    public static void main(String[] args) {
        try {
            final ConnectionProvider connectionProvider = new ConnectionProvider(Utils.USERNAME, Utils.PASSWORD, Utils.DBNAME);
            SQLUtils.onFirstLogin(connectionProvider.getMySQLConnection());
            Login login = new Login(connectionProvider.getMySQLConnection());
            login.Launcher("Login", null);
        } catch (final Exception e) {
            GUIUtils.exceptionToast("Error during app loading.", e);
        }
    }
}