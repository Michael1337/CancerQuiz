package model.friends;

import model.db.UserFactory;
import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class FriendsFactory {
    @Inject
    private Database db;
    @Inject
    private UserFactory uf;

    public class Friend {

        private UserFactory.User user;
        private LocalDateTime friends_since;

        /**
         * Creates a friend. A friend is basically a normal User who is known to another User since a moment in time
         *
         * @param user          The user that is friends with a given User
         * @param friends_since The moment in time since when the User is Friends with the given User
         */
        public Friend(UserFactory.User user, LocalDateTime friends_since) {
            this.user = user;
            this.friends_since = friends_since;
        }

        public UserFactory.User getUser() {
            return user;
        }

        public void setUser(UserFactory.User user) {
            this.user = user;
        }

        public LocalDateTime getFriends_since() {
            return friends_since;
        }

        public void setFriends_since(LocalDateTime friends_since) {
            this.friends_since = friends_since;
        }
    }

    /**
     * Returns a list of friends of a specific user.
     *
     * @param user_id The id of a user (most likely the logged in user).
     * @return A list of all the friends the given user has with their user_ids and the time since when they are friends.
     * @throws SQLException
     */
    public List<Friend> getFriendList(int user_id) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        Connection con = db.getConnection();
        //to understand: Read up on "aliases" and "union" for MySQL
        String query = "SELECT user_id_2 AS friend_id, friends_since FROM friend WHERE user_id = " + user_id + " " +
                "UNION SELECT user_id, friends_since FROM friend WHERE user_id_2 = " + user_id + ";";

        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(query);
        LinkedList<Friend> res = new LinkedList<Friend>();

        while (rs.next()) {
            res.add(new Friend(uf.fromDBWithID(rs.getInt("friend_id")),
                    LocalDateTime.parse(rs.getString("friends_since"), formatter)));
        }

        rs.close();
        stmt.close();
        con.close();

        return res;
    }

    /**
     * Returns a list of friend requests for a specific user.
     *
     * @param user_id The id of a user/receiver (most likely the logged in user).
     * @return A list of all the Users that have sent the given user a friend request.
     * @throws SQLException
     */
    public List<UserFactory.User> getFriendRequests(int user_id) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM friend_request WHERE (receiver_id = " + user_id + ");";

        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(query);
        LinkedList<UserFactory.User> res = new LinkedList<UserFactory.User>();

        while (rs.next()) {
            res.add(uf.fromDBWithID(rs.getInt("sender_id")));
        }

        rs.close();
        stmt.close();
        con.close();

        return res;
    }

    /**
     * Checks if a user has friends.
     *
     * @param user_id The logged in user.
     * @return Returns true if the user has at least one friend.
     * @throws SQLException
     */
    public boolean hasFriends(int user_id) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM friend WHERE (user_id = " + user_id + ") " +
                "OR (user_id_2 = " + user_id + ");";

        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(query);

        boolean b = rs.isBeforeFirst();
        rs.close();
        stmt.close();
        con.close();

        return b;
    }

    /**
     * Checks if a user has open friend requests.
     *
     * @param user_id The logged in user.
     * @return Returns true if the user has at least one friend request.
     * @throws SQLException
     */
    public boolean hasRequests(int user_id) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM friend_request WHERE (receiver_id = " + user_id + ");";

        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(query);

        boolean b = rs.isBeforeFirst();
        rs.close();
        stmt.close();
        con.close();

        return b;
    }

    /**
     * Checks if two users are friends.
     *
     * @param user_id_1 A user. Most likely the logged in user.
     * @param user_id_2 A user. Most likely a user the logged in user wants to see the profile of.
     * @return Returns true if the users are friends.
     * @throws SQLException
     */
    public boolean areFriends(int user_id_1, int user_id_2) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM friend WHERE (user_id = " + user_id_1 + " AND user_id_2 = " + user_id_2 + ") " +
                "OR (user_id_2 = " + user_id_1 + " AND user_id = " + user_id_2 + ");";

        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(query);

        boolean b = rs.isBeforeFirst();
        rs.close();
        stmt.close();
        con.close();

        return b;
    }

    /**
     * Checks if a user has a friend request from another specific user.
     *
     * @param user_id_1 A user. Receiver. Most likely the logged in user.
     * @param user_id_2 A user. Sender.
     * @return Returns true if the user has a friend request.
     * @throws SQLException
     */
    public boolean hasRequest(int user_id_1, int user_id_2) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM friend_request WHERE (receiver_id = " + user_id_1 + " AND sender_id = " + user_id_2 + ");";

        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(query);

        boolean b = rs.isBeforeFirst();
        rs.close();
        stmt.close();
        con.close();

        return b;
    }

    /**
     * Checks if a user has send a friend request to another specific user.
     *
     * @param user_id_1 A user. Sender. Most likely the logged in user.
     * @param user_id_2 A user. Receiver.
     * @return Returns true if the user has sent a friend request.
     * @throws SQLException
     */
    public boolean hasSentRequest(int user_id_1, int user_id_2) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM friend_request WHERE (receiver_id = " + user_id_2 + " AND sender_id = " + user_id_1 + ");";

        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(query);

        boolean b = rs.isBeforeFirst();
        rs.close();
        stmt.close();
        con.close();

        return b;
    }

    /**
     * Sends a friend request from one user to another one.
     *
     * @param user_id_1 A user. Sender. Most likely the logged in user.
     * @param user_id_2 A user. Receiver.
     * @throws SQLException
     */
    public void sendFriendRequest(int user_id_1, int user_id_2) throws SQLException {
        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO friend_request (`receiver_id`, `sender_id`) VALUES (?,?);",
                Statement.RETURN_GENERATED_KEYS);

        statement.setInt(1, user_id_2);
        statement.setInt(2, user_id_1);

        statement.executeUpdate();
        statement.close();
        con.close();
    }

    /**
     * Accepts a friend request for a user from another user. Creates friendship. \o/
     *
     * @param user_id_1 A user.
     * @param user_id_2 A user.
     * @throws SQLException
     */
    public void acceptFriendRequest(int user_id_1, int user_id_2) throws SQLException {
        LocalDateTime now_ldt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now_str = now_ldt.format(formatter);
        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO friend (`user_id`, `user_id_2`, `friends_since`) VALUES (?,?,?);",
                Statement.RETURN_GENERATED_KEYS);

        statement.setInt(1, user_id_1);
        statement.setInt(2, user_id_2);
        statement.setString(3, now_str);

        statement.executeUpdate();
        statement.close();

        statement = con.prepareStatement(
                "DELETE FROM friend_request WHERE (receiver_id = " + user_id_1 + " AND sender_id = " + user_id_2 + ");",
                Statement.RETURN_GENERATED_KEYS);

        statement.executeUpdate();
        statement.close();

        con.close();
    }

    /**
     * Deletes a friendship between two users.
     *
     * @param user_id_1 A user.
     * @param user_id_2 A user.
     * @throws SQLException
     */
    public void deleteFriend(int user_id_1, int user_id_2) throws SQLException {
        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "DELETE FROM friend WHERE (user_id = " + user_id_1 + " AND user_id_2 = " + user_id_2 + ") " +
                        "OR (user_id_2 = " + user_id_1 + " AND user_id = " + user_id_2 + ");");
        statement.executeUpdate();
        statement.close();
        con.close();
    }
}
