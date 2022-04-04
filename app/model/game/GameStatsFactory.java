package model.game;

import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
public class GameStatsFactory {
    @Inject
    private Database db;

    /**
     * Class that holds various GameStats
     */
    public class GameStatsGroup {
        private GameStats gameMostPoints;
        private GameStats gameMostRecent;
        double average;
        int count;

        /**
         * Creates a GameStatsGroup that holds the GameStats for most significant stats of a user.
         *
         * @param gameMostPoints The game with the most points.
         * @param gameMostRecent The game that was played most recently.
         * @param average        The average amount of points of a user.
         * @param count          The number of games a user played.
         */
        public GameStatsGroup(GameStats gameMostPoints, GameStats gameMostRecent, double average, int count) {
            this.gameMostPoints = gameMostPoints;
            this.gameMostRecent = gameMostRecent;
            this.average = average;
            this.count = count;
        }

        public GameStats getGameMostPoints() {
            return gameMostPoints;
        }

        public GameStats getGameMostRecent() {
            return gameMostRecent;
        }

        public double getAverage() {
            return average;
        }

        public int getCount() {
            return count;
        }
    }

    /**
     * Class that holds data for a game in the stats database.
     */
    public class GameStats {
        private int user_id;
        private int points;
        private LocalDateTime date;

        /**
         * Creates stats for a single game of a user.
         *
         * @param user_id The user who played the game.
         * @param points  The amount of points the user got in the game.
         * @param date    The date and time of the game.
         */
        public GameStats(int user_id, int points, LocalDateTime date) {
            this.user_id = user_id;
            this.points = points;
            this.date = date;
        }

        public int getUser_id() {
            return user_id;
        }

        public int getPoints() {
            return points;
        }

        public LocalDateTime getDate() {
            return date;
        }
    }

    /**
     * Creates a GameStatsGroup for a user by creating various stats.
     *
     * @param user_id The user.
     * @return Returns a GameStatsGroup object with multiple GameStats.
     * @throws SQLException
     */
    public GameStatsGroup getGameStatsGroup(int user_id) throws SQLException {
        GameStats mostPoints = getFromStatsMostPoints(user_id);
        GameStats mostRecent = getFromStatsMostRecent(user_id);
        double average = getFromStatsAvgPoints(user_id);
        int numberOfGames = getFromStatsCountGames(user_id);
        return new GameStatsGroup(mostPoints, mostRecent, average, numberOfGames);
    }

    /**
     * Checks if a user has played a game before.
     *
     * @param user_id A user.
     * @return Returns true if there are any stats for plaed games by a user.
     * @throws SQLException
     */
    public boolean hasStats(int user_id) throws SQLException {
        String query = "SELECT game_stats_id FROM game_stats WHERE user_id = " + user_id + ";";

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
     * Gets GameStats for the game with the most points by a user.
     *
     * @param user_id A user.
     * @return Returns a GameStats object.
     * @throws SQLException
     */
    private GameStats getFromStatsMostPoints(int user_id) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

        Connection con = db.getConnection();
        String query = "SELECT * FROM game_stats WHERE user_id = '" + user_id + "' ORDER by points DESC;";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        resultSet.next();
        GameStats gameStats = new GameStats(
                resultSet.getInt("user_id"),
                resultSet.getInt("points"),
                LocalDateTime.parse(resultSet.getString("date"), formatter));

        resultSet.close();
        stmt.close();
        con.close();

        return gameStats;
    }

    /**
     * Gets GameStats for the game that was played most recently by a user.
     *
     * @param user_id A user.
     * @return Returns a GameStats object.
     * @throws SQLException
     */
    private GameStats getFromStatsMostRecent(int user_id) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

        Connection con = db.getConnection();
        String query = "SELECT * FROM game_stats WHERE user_id = '" + user_id + "' ORDER by date DESC;";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        resultSet.next();
        GameStats gameStats = new GameStats(
                resultSet.getInt("user_id"),
                resultSet.getInt("points"),
                LocalDateTime.parse(resultSet.getString("date"), formatter));

        resultSet.close();
        stmt.close();
        con.close();

        return gameStats;
    }

    /**
     * Gets the average amount of points a user got in all of his games.
     *
     * @param user_id A user.
     * @return Returns a double as the average of points.
     * @throws SQLException
     */
    private double getFromStatsAvgPoints(int user_id) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT AVG(points) as 'average' FROM game_stats WHERE user_id = '" + user_id + "' GROUP BY user_id;";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        resultSet.next();
        double average = resultSet.getDouble("average");

        resultSet.close();
        stmt.close();
        con.close();

        return average;
    }

    /**
     * Gets the amount of played games by a user.
     *
     * @param user_id A user.
     * @return Returns an integer that is the count of played games.
     * @throws SQLException
     */
    private int getFromStatsCountGames(int user_id) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT COUNT(points) as 'count' FROM game_stats WHERE user_id = '" + user_id + "' GROUP BY user_id;";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        resultSet.next();
        int count = resultSet.getInt("count");

        resultSet.close();
        stmt.close();
        con.close();

        return count;
    }
}
