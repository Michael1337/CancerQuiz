package model.friends;

import model.db.UserFactory;
import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class MessageFactory {
    @Inject
    private UserFactory uf;
    @Inject
    private Database db;

    public class Message {
        private int message_id;
        private UserFactory.User receiver;
        private UserFactory.User sender;
        private String title;
        private String text;
        private LocalDateTime date;
        private int read;

        /**
         * Creates a message. The message holds all user data for receiver and sender.
         *
         * @param message_id  Unique id of the message.
         * @param receiver_id Id of receiving user.
         * @param sender_id   Id of sending user.
         * @param title       Title of the message.
         * @param text        Text of the message (message itself).
         * @param date        Date when the message was sent.
         * @param read        Whether the message was read already.
         * @throws SQLException
         */
        public Message(int message_id, int receiver_id, int sender_id, String title, String text, LocalDateTime date, int read) throws SQLException {
            this.message_id = message_id;
            this.receiver = uf.fromDBWithID(receiver_id);
            this.sender = uf.fromDBWithID(sender_id);
            this.title = title;
            this.text = text;
            this.date = date;
            this.read = read;
        }

        public int getMessage_id() {
            return message_id;
        }

        public UserFactory.User getReceiver() {
            return receiver;
        }

        public UserFactory.User getSender() {
            return sender;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public int getRead() {
            return read;
        }

        public void setRead(int status) {
            this.read = status;
        }

        /**
         * Updates the message.
         */
        public void update() throws SQLException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String date_str = date.format(formatter);

            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "UPDATE message SET `receiver_id`=?, `sender_id`=?, `title`=?, `text`=?, `date`=?, `read`=? WHERE message_id=" + message_id + ";");

            statement.setInt(1, receiver.getUser_id());
            statement.setInt(2, sender.getUser_id());
            statement.setString(3, title);
            statement.setString(4, text);
            statement.setString(5, date_str);
            statement.setInt(6, read);

            statement.executeUpdate();
            statement.close();
            con.close();
        }

        /**
         * Deletes the message.
         */
        public void delete() throws SQLException {
            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "DELETE FROM message WHERE message_id=" + message_id + ";");
            statement.executeUpdate();
            statement.close();
            con.close();
        }

    }

    /**
     * Counts the unread messages a user has.
     *
     * @param user_id The logged in user.
     * @return Returns true if the user has at least one unread message.
     * @throws SQLException
     */
    public int countUnreadMessages(int user_id) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT COUNT(*) as 'count' FROM message WHERE (receiver_id = " + user_id + ") AND (`read` = 0);";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        rs.next();

        int count = rs.getInt("count");
        rs.close();
        stmt.close();
        con.close();

        return count;
    }

    /**
     * Creates a new message, inserts it into the Message table, and returns a corresponding (proxy) Message instance.
     *
     * @param receiver_id Id of the receiving user.
     * @param sender_id   Id of the sending user.
     * @param title       Title of the message.
     * @param text        Text of the message.
     * @return Returns a message instance.
     * @throws SQLException
     */
    public Message create(int receiver_id, int sender_id, String title, String text) throws SQLException {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String date_str = date.format(formatter);

        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO message (`receiver_id`, `sender_id`, `title`, `text`, `date`, `read`) VALUES (?,?,?,?,?,?);",
                Statement.RETURN_GENERATED_KEYS);

        statement.setInt(1, receiver_id);
        statement.setInt(2, sender_id);
        statement.setString(3, title);
        statement.setString(4, text);
        statement.setString(5, date_str);
        statement.setInt(6, 0);

        statement.executeUpdate();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            Message res = new Message(generatedKeys.getInt(1), receiver_id, sender_id, title, text, date, 0);
            generatedKeys.close();
            statement.close();
            con.close();
            return res;
        } else {
            generatedKeys.close();
            statement.close();
            con.close();
            throw new SQLException("Creating Message failed, no ID obtained.");
        }
    }

    /**
     * Returns all Messages from the Messages table.
     */
    public List<Message> fromDB() throws SQLException {
        return fromDBWhere(null);
    }

    /**
     * Queries the Message with message_id from the database and create a corresponding (proxy) Message instance.
     *
     * @param message_id The unique id of a message.
     * @return Returns the message corresponding to the given id.
     */
    public Message fromDBWithID(int message_id) throws SQLException {
        return fromDBWhere("message_id=" + message_id).get(0);
    }

    /**
     * Returns all Messages from the Messages table, that have the given receiver_id.
     *
     * @param receiver_id The id of a user who shall be the receiver of the given messages.
     * @return Returns a list of messages received by the given user.
     */
    public List<Message> fromDBWithReceiver(int receiver_id) throws SQLException {
        List<Message> messagesReceiver = fromDBWhere("receiver_id='" + receiver_id + "'");
        /* changing sort from old -> new to new -> old */
        Collections.reverse(messagesReceiver);
        return messagesReceiver;
    }

    /**
     * Returns all Messages from the Messages table, that have the given sender_id.
     *
     * @param sender_id The id of a user who shall be the sender of the given messages.
     * @return Returns a list of messages sent by the given user.
     */
    public List<Message> fromDBWithSender(int sender_id) throws SQLException {
        List<Message> messagesSender = fromDBWhere("sender_id='" + sender_id + "'");
        /* changing sort from old -> new to new -> old */
        Collections.reverse(messagesSender);
        return messagesSender;
    }


    /**
     * Returns all Messages that meet a given condition.
     *
     * @param where An SQL where condition for Message.
     * @return All database entries that meet the condition.
     * @throws SQLException
     */
    private List<Message> fromDBWhere(String where) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        Connection con = db.getConnection();
        String query = "SELECT * FROM message";

        if (where == null) query += ";";
        else query += " WHERE " + where + ";";

        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(query);
        LinkedList<Message> res = new LinkedList<Message>();

        while (rs.next()) {
            res.add(new Message(
                    rs.getInt("message_id"),
                    rs.getInt("receiver_id"),
                    rs.getInt("sender_id"),
                    rs.getString("title"),
                    rs.getString("text"),
                    LocalDateTime.parse(rs.getString("date"), formatter),
                    rs.getInt("read")
            ));
        }

        rs.close();
        stmt.close();
        con.close();

        return res;
    }


    /**
     * Deletes the row corresponding to this.message_id.
     *
     * @param message_id The id of the message that shall be deleted.
     */
    public void deletebyId(int message_id) throws SQLException {
        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "DELETE FROM message WHERE message_id=" + message_id + ";");
        statement.executeUpdate();
        statement.close();
        con.close();
    }
}