package model.game;

import model.game.quiz.Quiz;
import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
public class GameFactory {
    @Inject
    private Database db;
    @Inject
    private Quiz quiz;

    public class Game {
        private int game_id;
        private int user_id;
        private int progress_count;
        private int points_game;

        public Game(int game_id, int user_id, int progress_count, int points_game) {
            this.game_id = game_id;
            this.user_id = user_id;
            this.progress_count = progress_count;
            this.points_game = points_game;
        }

        public int getGame_id() {
            return game_id;
        }

        public int getUser_id() {
            return user_id;
        }

        public int getProgress_count() {
            return progress_count;
        }

        public int getPoints_game() {
            return points_game;
        }

        public void addPoints(int points) {
            points_game += points;
        }

        public void multiplyPoints(double multiplier) throws SQLException {
            int points_quiz = quiz.getScoreOfLastQuiz(user_id);
            System.out.println("Punkte: " + points_quiz + "; Multiplikator: " + multiplier + "; " +
                    "Ergibt: " + points_quiz * multiplier + "; aufgerundet: " + (int) Math.ceil(points_quiz * multiplier));
            addPoints((int) Math.ceil(points_quiz * multiplier));
        }

        public void updateProgress() {
            progress_count++;
        }

        /**
         * Updates the row corresponding to this.game_id with the properties of this game instance.
         */
        public void update() throws SQLException {

            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "UPDATE game SET `progress_count`=?, `points_game`=? WHERE game_id=" + game_id + ";");

            statement.setInt(1, progress_count);
            statement.setInt(2, points_game);

            statement.executeUpdate();
            statement.close();
            con.close();
        }

        /**
         * Finishes the game by saving it to the statistics and deleting it from the currently running games.
         */
        public void finish() throws SQLException {
            saveToStats(user_id, points_game);
            delete();
        }

        /**
         * Saves the final score of a game to the database.
         *
         * @param user_id The User whose game shall be saved.
         * @param score   The final score of the game.
         * @throws SQLException
         */
        private void saveToStats(int user_id, int score) throws SQLException {
            LocalDateTime now_ldt = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now_str = now_ldt.format(formatter);

            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "INSERT INTO game_stats (`user_id`, `points`, `date`) VALUES (?,?,?);");

            statement.setInt(1, user_id);
            statement.setInt(2, score);
            statement.setString(3, now_str);

            statement.executeUpdate();
            statement.close();
        }

        /**
         * Deletes the game.
         */
        private void delete() throws SQLException {
            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "DELETE FROM game WHERE game_id=" + game_id + ";");
            statement.executeUpdate();
            statement.close();
            con.close();
        }
    }

    /**
     * Checks if a user already has an open game.
     *
     * @param user_id A user who wants to play a game.
     * @return Returns true if the user already has an open game.
     * @throws SQLException
     */
    public boolean hasGame(int user_id) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT user_id FROM game WHERE user_id = " + user_id + ";";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);

        boolean hasGame = resultSet.isBeforeFirst();

        resultSet.close();
        stmt.close();
        con.close();

        return hasGame;
    }

    /**
     * Creates a new game for a user.
     *
     * @param user_id The user who wants to play a game.
     * @return Creates a game, stores it in the database and creates an instance of it.
     * @throws SQLException
     */
    public Game create(int user_id) throws SQLException {
        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO game (`user_id`, `progress_count`, `points_game`) VALUES (?,?,?);",
                Statement.RETURN_GENERATED_KEYS);

        statement.setInt(1, user_id);
        statement.setInt(2, 0);
        statement.setInt(3, 0);

        statement.executeUpdate();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            Game res = new Game(generatedKeys.getInt(1), user_id, 0, 0);
            generatedKeys.close();
            statement.close();
            con.close();
            return res;
        } else {
            generatedKeys.close();
            statement.close();
            con.close();
            throw new SQLException("Creating Game failed, no ID obtained.");
        }
    }

    /**
     * Gets a game from the database and creates a game instance for it.
     *
     * @param user_id The logged in user.
     * @return Returns a game for the user.
     * @throws SQLException
     */
    public Game getGameByUserId(int user_id) throws SQLException {
        Connection con = db.getConnection();

        String query = "SELECT * FROM game WHERE user_id = '" + user_id + "';";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        resultSet.next();
        Game game = new Game(
                resultSet.getInt("game_id"),
                resultSet.getInt("user_id"),
                resultSet.getInt("progress_count"),
                resultSet.getInt("points_game"));

        resultSet.close();
        stmt.close();
        con.close();

        return game;
    }
}