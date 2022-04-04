package model.db;

import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class UserFactory {
    @Inject
    private Database db;

    public class User {
        private int user_id;
        private String name;
        private String email;
        private String password;
        private int role;
        private LocalDateTime last_login;
        private int points_overall;
        private int points_max;

        /**
         * Creates a user withe the given properties.
         *
         * @param user_id        The unique id of a user.
         * @param name           The name of the user.
         * @param email          The email address of the user.
         * @param password       The password (plain text) of the user.
         * @param role           The role (player/admin) of the user.
         * @param last_login     When the user logged in most recently.
         * @param points_overall The points a user got in all his games.
         * @param points_max     The most points a user got in a single game.
         */
        public User(int user_id, String name, String email, String password, int role, LocalDateTime last_login, int points_overall, int points_max) {
            this.user_id = user_id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
            this.last_login = last_login;
            this.points_overall = points_overall;
            this.points_max = points_max;
        }


        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getRole() {
            return role;
        }

        public void setRole(int role) {
            this.role = role;
        }

        public LocalDateTime getLast_login() {
            return last_login;
        }

        public void setLast_login(LocalDateTime last_login) {
            this.last_login = last_login;
        }

        public int getPoints_overall() {
            return points_overall;
        }

        public void setPoints_overall(int points_overall) {
            this.points_overall = points_overall;
        }

        public void addPoints_overall(int points) {
            this.points_overall += points;
        }

        public int getPoints_max() {
            return points_max;
        }

        public void setPoints_max(int points_max) {
            this.points_max = points_max;
        }

        public void updatePoints_max(int points) {
            if (points > this.points_max) {
                this.points_max = points;
            }
        }

        public String getRank() throws SQLException {
            Connection con = db.getConnection();
            String query = "SELECT * FROM rank WHERE points_overall <= " + points_overall + " ORDER BY points_overall DESC LIMIT 1;";

            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);

            resultSet.next();
            String rank = resultSet.getString("name");

            resultSet.close();
            stmt.close();
            con.close();

            return rank;
        }

        /**
         * Updates the row corresponding to this.user_id with the properties of this user instance.
         */
        public void update() throws SQLException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String last_login_str = last_login.format(formatter);

            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "UPDATE user SET `name`=?, `email`=?, `password`=?, `role`=?, `last_login`=?, `points_overall`=?, `points_max`=? WHERE user_id=" + user_id + ";");

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setInt(4, role);
            statement.setString(5, last_login_str);
            statement.setInt(6, points_overall);
            statement.setInt(7, points_max);

            statement.executeUpdate();
            statement.close();
            con.close();
        }

        /**
         * Deletes the row corresponding to this.user_id.
         */
        public void delete() throws SQLException {
            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "DELETE FROM user WHERE user_id=" + user_id + ";");
            statement.executeUpdate();
            statement.close();
            con.close();
        }

    }


    /**
     * Checks if a given name is already taken by another user.
     *
     * @param username The desired name.
     * @return Returns true if a user with the given name already exists.
     * @throws SQLException
     */
    public boolean nameExists(String username) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM user WHERE name = '" + username + "';";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        boolean nameExists = resultSet.isBeforeFirst();

        resultSet.close();
        stmt.close();
        con.close();

        return nameExists;
    }

    /**
     * Checks if a given email is already taken by another user.
     *
     * @param email The desired email.
     * @return Returns true if a user with the given email already exists.
     * @throws SQLException
     */
    public boolean emailExists(String email) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM user WHERE email = '" + email + "';";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        boolean emailExists = resultSet.isBeforeFirst();

        resultSet.close();
        stmt.close();
        con.close();

        return emailExists;
    }

    /**
     * Creates a new user, inserts it into the User table, and returns a corresponding (proxy) User instance.
     *
     * @param name     Name of the user.
     * @param email    Email address of the user.
     * @param password Password of the user.
     * @return Returns a user instance.
     * @throws SQLException
     */
    public User create(String name, String email, String password) throws SQLException {
        LocalDateTime now_ldt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now_str = now_ldt.format(formatter);

        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO user (`name`, `email`, `password`, `role`, `last_login`, `points_overall`, `points_max`) VALUES (?,?,?,?,?,?,?);",
                Statement.RETURN_GENERATED_KEYS);

        statement.setString(1, name);
        statement.setString(2, email);
        statement.setString(3, password);
        statement.setInt(4, 0);
        statement.setString(5, now_str);
        statement.setInt(6, 0);
        statement.setInt(7, 0);

        statement.executeUpdate();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            User res = new User(generatedKeys.getInt(1), email, name, password, 0, now_ldt, 0, 0);
            generatedKeys.close();
            statement.close();
            con.close();
            return res;
        } else {
            generatedKeys.close();
            statement.close();
            con.close();
            throw new SQLException("Creating User failed, no ID obtained.");
        }
    }

    /**
     * Returns all Users from the Users table.
     */
    public List<User> fromDB() throws SQLException {
        return fromDBWhere(null);
    }

    /**
     * Queries the User with user_id from the database and create a corresponding (proxy) User instance.
     *
     * @param user_id The unique id of a user.
     * @return Returns the user corresponding to the given id.
     */
    public User fromDBWithID(int user_id) throws SQLException {
        return fromDBWhere("user_id=" + user_id).get(0);
    }

    /**
     * Returns all Users from the Users table, that have the given name.
     *
     * @param name The name the database gets searched for.
     * @return Returns a list of users with the given email.
     */
    public List<User> fromDBWithName(String name) throws SQLException {
        return fromDBWhere("name='" + name + "'");
    }

    /**
     * Returns all Users from the Users table, that have the given email.
     *
     * @param email The name the database gets searched for.
     * @return Returns a list of users with the given email.
     */
    public List<User> fromDBWithEmail(String email) throws SQLException {
        return fromDBWhere("email='" + email + "'");
    }

    /**
     * Returns all Users from the Users table, that have the given role.
     *
     * @param role The name the database gets searched for.
     * @return Returns a list of users with the given role.
     */
    public List<User> fromDBWithRole(int role) throws SQLException {
        return fromDBWhere("role=" + role + "");
    }

    /**
     * Returns all Users from the Users table, that have the given credentials.
     *
     * @param name     The name the database gets searched for.
     * @param password The password the database gets searched for.
     * @return Returns a list of users with the given name and passwprd.
     */
    public List<User> fromDBWithCredentials(String name, String password) throws SQLException {
        return fromDBWhere("name='" + name + "' AND password='" + password + "'");
    }


    /**
     * Returns all Users that meet a given condition.
     *
     * @param where An SQL where condition for User.
     * @return All database entries that meet the condition.
     * @throws SQLException
     */
    private List<User> fromDBWhere(String where) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        Connection con = db.getConnection();
        String query = "SELECT * FROM user";

        if (where == null) query += ";";
        else query += " WHERE " + where + ";";

        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(query);
        LinkedList<User> res = new LinkedList<User>();

        while (rs.next()) {
            res.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getInt("role"),
                    LocalDateTime.parse(rs.getString("last_login"), formatter),
                    rs.getInt("points_overall"),
                    rs.getInt("points_max")));
        }

        rs.close();
        stmt.close();
        con.close();

        return res;
    }
}