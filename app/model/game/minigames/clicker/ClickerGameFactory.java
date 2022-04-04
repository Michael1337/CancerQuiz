package model.game.minigames.clicker;

import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;


@Singleton
public class ClickerGameFactory {
    @Inject
    private Database db;

    /**
     * This classes purpose is to make played ClickerGames suitable for being saved to and retrieved from the database.
     */
    public class ClickerGame {
        private int user_id;
        private int points;
        private LocalDateTime end_time;

        /**
         * Creates a new ClickerGame instance. This does not get changed. It will just be put in a database.
         *
         * @param user_id  Id of the playing user.
         * @param points   Amount of points for the game.
         * @param end_time Time at which the game was ended.
         */
        public ClickerGame(int user_id, int points, LocalDateTime end_time) {
            this.user_id = user_id;
            this.points = points;
            this.end_time = end_time;
        }

        public int getPoints() {
            return points;
        }

        public LocalDateTime getEnd_time() {
            return end_time;
        }

        /**
         * Ends a game. Adds data to stats table. Potential to do more.
         */
        public void finish() throws SQLException {
            this.addToStats();
        }

        /**
         * Adds a played game to the statistics table.
         *
         * @throws SQLException
         */
        private void addToStats() throws SQLException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String time = end_time.format(formatter);

            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "INSERT INTO clicker_stats (`user_id`, `clicks`, `date`) VALUES (?,?,?);",
                    Statement.RETURN_GENERATED_KEYS);

            statement.setInt(1, user_id);
            statement.setInt(2, points);
            statement.setString(3, time);

            statement.executeUpdate();
            statement.close();
            con.close();
        }
    }

    public ClickerGame createGame(int user_id, int points, LocalDateTime time) {
        return new ClickerGameFactory.ClickerGame(user_id, points, time);
    }

    /**
     * Returns a multiplier between 1.0 and 2.0.
     *
     * @param points The points used for the calculation.
     * @return Returns a double between 1.0 and 2.0.
     */
    public double getMultiplier(int points) {
        double max = 2.0;
        double min = 1.0;
        double x = (double) points / 35;
        x += min;
        x = (x > max) ? max : x;
        return x;
    }

    /**
     * Following code is only used for statistics.
     */

    /**
     * An object to handle statistics for ClickerGames. It holds two ClickerGame objects and the amount of played ClickerGames by a user.
     */
    public class ClickerStats {
        private ClickerGame mostPoints;
        private ClickerGame mostRecent;
        private int numberOfGames;

        public ClickerStats(ClickerGame mostPoints, ClickerGame mostRecent, int numberOfGames) {
            this.mostPoints = mostPoints;
            this.mostRecent = mostRecent;
            this.numberOfGames = numberOfGames;
        }

        public ClickerGame getMostPoints() {
            return mostPoints;
        }

        public ClickerGame getMostRecent() {
            return mostRecent;
        }

        public int getNumberOfGames() {
            return numberOfGames;
        }
    }

    /**
     * Checks if a user has nay stats for played clicker games.
     *
     * @param user_id The user who may or may not has played a clicker game yet.
     * @return Returns true if the user has played at least one clicker game.
     * @throws SQLException
     */
    public boolean hasStats(int user_id) throws SQLException {
        String query = "SELECT clicker_stats_id FROM clicker_stats WHERE user_id = " + user_id + ";";

        Connection con = db.getConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        boolean b = rs.isBeforeFirst();

        rs.close();
        stmt.close();
        con.close();

        return b;
    }

    /**
     * Creates an object containing statistics about played clicker games for a user.
     *
     * @param user_id The user whose statistics shall be created.
     * @return Returns games with most points and latest date for a user.
     * @throws SQLException
     */
    public ClickerGameFactory.ClickerStats createStats(int user_id) throws SQLException {
        ClickerGame mostPoints = fromDBmostPointsForUser(user_id);
        ClickerGame mostRecent = fromDBmostRecentForUser(user_id);
        int numberOfGames = fromDBnumberOfGames(user_id);
        return new ClickerGameFactory.ClickerStats(mostPoints, mostRecent, numberOfGames);
    }

    /**
     * Gets the game with the most points from the database for a user.
     *
     * @param user_id The user whose best game shall be selected.
     * @return Returns a ClickerGame with the most points.
     * @throws SQLException
     */
    private ClickerGame fromDBmostPointsForUser(int user_id) throws SQLException {
        String query = "SELECT * FROM clicker_stats WHERE user_id = " + user_id + " ORDER BY clicks DESC LIMIT 1;";
        return fromDBForUser(query);
    }

    /**
     * Gets the game with the most recent date from the database for a user.
     *
     * @param user_id The user whose most recent game shall be selected.
     * @return Returns a ClickerGame with the most recent date.
     * @throws SQLException
     */
    private ClickerGame fromDBmostRecentForUser(int user_id) throws SQLException {
        String query = "SELECT * FROM clicker_stats WHERE user_id = " + user_id + " ORDER BY date DESC LIMIT 1;";
        return fromDBForUser(query);
    }

    /**
     * Selects a game from the database.
     *
     * @param query The query for the database selection.
     * @return Returns a ClickerGame based on the query.
     * @throws SQLException
     */
    private ClickerGame fromDBForUser(String query) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

        Connection con = db.getConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        LinkedList<ClickerGame> res = new LinkedList<ClickerGame>();
        while (rs.next()) {
            res.add(new ClickerGame(
                    rs.getInt("user_id"),
                    rs.getInt("clicks"),
                    LocalDateTime.parse(rs.getString("date"), formatter)));
        }

        rs.close();
        stmt.close();
        con.close();

        return res.getFirst();
    }

    /**
     * Gets the total number of played ClickerGames for a user.
     *
     * @param user_id The user whose data shall be selected.
     * @return Returns an integer representing the number of game played.
     * @throws SQLException
     */
    private int fromDBnumberOfGames(int user_id) throws SQLException {
        String query = "SELECT COUNT(clicker_stats_id) FROM clicker_stats WHERE user_id = " + user_id + ";";

        Connection con = db.getConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int number = rs.getInt("COUNT(clicker_stats_id)");

        rs.close();
        stmt.close();
        con.close();

        return number;
    }
}
