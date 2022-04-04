package model.game.quiz;

import play.api.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class Quiz {
    @Inject
    private Database db;
    @Inject
    private QuestionFactory qf;

    /**
     * Checks if a user already has an open quiz.
     *
     * @param user_id A user who wants to play a quiz.
     * @return Returns true if the user already has an open quiz.
     * @throws SQLException
     */
    public Boolean hasQuiz(int user_id) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM quiz_user WHERE user_id = " + user_id + ";";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);

        boolean hasQuiz = resultSet.isBeforeFirst();

        resultSet.close();
        stmt.close();
        con.close();

        return hasQuiz;
    }

    /**
     * Creates a new quiz for a user with a given kind and stores it in the db.
     *
     * @param user_id A user who wants to play a quiz.
     * @param kind    The kind of questions for the quiz.
     * @throws SQLException
     */
    public void createQuiz(int user_id, KindOfQuiz kind) throws SQLException {
        List<Integer> questions = getRandomQuestionIds(kind);

        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO quiz_user (`user_id`, `quiz_pool_id`, `quiz_kind`) VALUES (?,?,?);");

        statement.setInt(1, user_id);
        statement.setInt(2, user_id);
        statement.setInt(3, kind.getId());

        statement.executeUpdate();
        statement.close();

        for (int question_id : questions) {
            statement = con.prepareStatement(
                    "INSERT INTO quiz_pool (`quiz_pool_pool_id`, `question_id`, `question_status`) VALUES (?,?,?);");

            statement.setInt(1, user_id);
            statement.setInt(2, question_id);
            statement.setInt(3, 0);

            statement.executeUpdate();
            statement.close();
        }

        con.close();
    }

    /**
     * Selects ten random question ids of a kind from the database.
     *
     * @param kind The kind of questions to be retrieved.
     * @return Returns a list of ids of random questions.
     * @throws SQLException
     */
    private List<Integer> getRandomQuestionIds(KindOfQuiz kind) throws SQLException {
        Connection con = db.getConnection();

        String query = "SELECT question_id FROM question JOIN question_category ON question.category_id = " +
                "question_category.question_category_id WHERE question_category.name = '" + kind.name() + "' ORDER BY RAND() LIMIT 10;";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        List<Integer> questions = new LinkedList<>();

        while (resultSet.next()) {
            questions.add(resultSet.getInt("question_id"));
        }

        resultSet.close();
        stmt.close();
        con.close();

        return questions;
    }

    /**
     * Checks if a quiz has unanswered questions.
     *
     * @param userID The user the quiz belongs to.
     * @return Returns true if there are unanswered questions.
     * @throws SQLException
     */
    public boolean hasOpenQuestion(int userID) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM quiz_pool JOIN quiz_user ON quiz_user.quiz_pool_id = quiz_pool.quiz_pool_pool_id"
                + " WHERE quiz_user.user_id = " + userID + " AND quiz_pool.question_status = " + 0 + ";";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        boolean openQuestions = resultSet.isBeforeFirst();

        resultSet.close();
        stmt.close();
        con.close();

        return openQuestions;
    }

    /**
     * Gets the next unanswered question from a quiz of a user.
     *
     * @param userID The user who plays the quiz.
     * @return Returns an unanswered question.
     * @throws SQLException
     */
    public QuestionFactory.Question getNextQuestion(int userID) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT question_id FROM quiz_pool JOIN quiz_user ON quiz_user.quiz_pool_id = quiz_pool.quiz_pool_pool_id"
                + " WHERE quiz_user.user_id = " + userID + " AND quiz_pool.question_status = " + 0 + ";";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);

        resultSet.next();
        QuestionFactory.Question question = qf.getQuestionById(resultSet.getInt("question_id"));

        resultSet.close();
        stmt.close();
        con.close();

        return question;
    }

    /**
     * Gives information about the category.
     *
     * @param user_id The player who plays the quiz.
     * @return Returns two strings. First is the category id, second the category name.
     * @throws SQLException
     */
    public String[] getCategory(int user_id) throws SQLException {
        String[] category = new String[2];
        int kindInt = getKindOfQuiz(user_id);
        KindOfQuiz kindEnum = KindOfQuiz.getByValue(kindInt);
        category[0] = Integer.toString(kindEnum.getId());
        category[1] = kindEnum.getName();
        return category;
    }

    /**
     * Gets the score for a quiz of a user by counting the correctly given answers.
     *
     * @param userID The user who played the quiz.
     * @return Returns the number of correctly answered questions.
     * @throws SQLException
     */
    public int getScore(int userID) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT COUNT(*) FROM quiz_pool WHERE quiz_pool_pool_id = " + userID + " AND question_status = " + 1 + ";";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        int score = 0;

        while (resultSet.next()) {
            score = resultSet.getInt("COUNT(*)");
        }

        resultSet.close();
        stmt.close();
        con.close();

        return score;
    }

    /**
     * Gets the number of answered questions in a quiz for a user.
     *
     * @param userID The user who plays the quiz.
     * @return Returns an integer that is the number of given answers.
     * @throws SQLException
     */
    public int getProgress(int userID) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT COUNT(*) FROM quiz_pool JOIN quiz_user ON quiz_user.quiz_pool_id = quiz_pool.quiz_pool_pool_id"
                + " WHERE quiz_user.user_id = " + userID + " AND quiz_pool.question_status != " + 0 + ";";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        int answeredQuestions = 0;

        while (resultSet.next()) {
            answeredQuestions = resultSet.getInt("COUNT(*)");
        }

        resultSet.close();
        stmt.close();
        con.close();

        return answeredQuestions + 1;
    }

    /**
     * Gets the number of questions in a quiz for a user.
     *
     * @param userID The user who plays the quiz.
     * @return Returns an integer that is the number of questions.
     * @throws SQLException
     */
    public int getNumberOfQuestions(int userID) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT COUNT(*) FROM quiz_pool JOIN quiz_user ON quiz_user.quiz_pool_id = " +
                "quiz_pool.quiz_pool_pool_id WHERE quiz_user.user_id = " + userID + ";";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        int questionsInPool = 0;

        while (resultSet.next()) {
            questionsInPool = resultSet.getInt("COUNT(*)");
        }

        resultSet.close();
        stmt.close();
        con.close();

        return questionsInPool;
    }

    /**
     * Gets the value representation of the current quiz's kind of a user.
     *
     * @param user_id The user who played the quiz.
     * @return Returns the integer of the quiz's kind.
     * @throws SQLException
     */
    private int getKindOfQuiz(int user_id) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT quiz_kind FROM quiz_user WHERE user_id = " + user_id + ";";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);

        int kind = 0;
        while (resultSet.next()) {
            kind = resultSet.getInt("quiz_kind");
        }

        resultSet.close();
        stmt.close();
        con.close();

        return kind;
    }

    /**
     * Checks if a user has not answered a given question during this quiz.
     *
     * @param user_id    The user who plays the quiz.
     * @param questionID The question the user tries to answer.
     * @return Returns true if the user has not answered the question yet.
     * @throws SQLException
     */
    public boolean isNotAnswered(int user_id, int questionID) throws SQLException {

        Connection con = db.getConnection();
        String query = "SELECT question_status FROM quiz_pool WHERE quiz_pool_pool_id=" + user_id + " AND question_id=" + questionID + ";";

        Statement statement = con.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        boolean correct = (resultSet.getInt("question_status") == 0);

        statement.close();
        con.close();

        return correct;
    }

    /**
     * Updates the quiz by marking a question as answered as either correct or incorrect.
     *
     * @param user_id    The user who plays the quiz.
     * @param questionID The question the user has answered.
     * @param isTrue     If the given answer was correct.
     * @throws SQLException
     */
    public void updateQuestionStatus(int user_id, int questionID, boolean isTrue) throws SQLException {
        int status;
        if (isTrue) {
            status = 1;
        } else {
            status = 2;
        }

        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "UPDATE quiz_pool SET `question_status`=? WHERE quiz_pool_pool_id=" + user_id + " AND question_id=" + questionID + ";");

        statement.setInt(1, status);

        statement.executeUpdate();
        statement.close();
        con.close();

        saveToQuestionStatistics(user_id, questionID, isTrue);
    }

    /**
     * Saves the question for a user to his statistics.
     *
     * @param user_id    The user who answered the question.
     * @param questionID The question that was answered.
     * @param isTrue     If the given answer was correct.
     * @throws SQLException
     */
    private void saveToQuestionStatistics(int user_id, int questionID, boolean isTrue) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM question_stats WHERE user_id=" + user_id + " AND question_id=" + questionID + ";";

        PreparedStatement statement = con.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        boolean questionAlreadyAsked = resultSet.isBeforeFirst();

        resultSet.close();
        statement.close();

        int incremental = (isTrue) ? 1 : 0;
        if (!questionAlreadyAsked) {
            statement = con.prepareStatement(
                    "INSERT INTO question_stats (`question_id`, `user_id`, `times_asked`, `times_correct`) VALUES (?,?,?,?);");

            statement.setInt(1, questionID);
            statement.setInt(2, user_id);
            statement.setInt(3, 1);
            statement.setInt(4, incremental);
        } else {
            statement = con.prepareStatement(
                    "UPDATE question_stats SET `times_asked`=`times_asked`+1, `times_correct` = `times_correct`+"
                            + incremental + " WHERE user_id=" + user_id + " AND question_id=" + questionID + ";");
        }

        statement.executeUpdate();
        statement.close();
        con.close();

    }

    /**
     * Finishes a quiz by saving the final score and deleting the "flash/session" data.
     *
     * @param user_id The User whose quiz is finished.
     * @throws SQLException
     */
    public void finish(int user_id) throws SQLException {
        int score = getScore(user_id);
        int kind = getKindOfQuiz(user_id);
        saveToStats(user_id, kind, score);
        deleteQuizInDB(user_id);
    }

    /**
     * Saves the final score of a quiz to the database.
     *
     * @param user_id The User whose quiz shall be saved.
     * @param kind    The kind of the quiz.
     * @param score   The final score of the quiz.
     * @throws SQLException
     */
    private void saveToStats(int user_id, int kind, int score) throws SQLException {
        LocalDateTime now_ldt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now_str = now_ldt.format(formatter);

        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO quiz_stats (`user_id`, `quiz_kind`, `points`, `date`) VALUES (?,?,?,?);");

        statement.setInt(1, user_id);
        statement.setInt(2, kind);
        statement.setInt(3, score);
        statement.setString(4, now_str);

        statement.executeUpdate();
        statement.close();
    }

    /**
     * Deletes a quiz from the database.
     *
     * @param userID The user who played the quiz.
     * @throws SQLException
     */
    private void deleteQuizInDB(int userID) throws SQLException {
        Connection con = db.getConnection();
        PreparedStatement statement = con.prepareStatement(
                "DELETE FROM quiz_pool WHERE quiz_pool_pool_id=" + userID + ";");

        statement.executeUpdate();
        statement.close();

        statement = con.prepareStatement(
                "DELETE FROM quiz_user WHERE user_id=" + userID + ";");

        statement.executeUpdate();
        statement.close();

        con.close();
    }

    /**
     * Gets the score of the last quiz of a user. Note: A User always plays quizzes in a certain order. That's why we don't need to query for a specific kind.
     *
     * @param user_id The User who we want a score of.
     * @return Returns an integer of the last points by date for a given user.
     * @throws SQLException
     */
    public int getScoreOfLastQuiz(int user_id) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT points FROM quiz_stats WHERE user_id = " + user_id + " ORDER BY date DESC LIMIT 1;";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);

        int score = 0;
        while (resultSet.next()) {
            score = resultSet.getInt("points");
        }

        resultSet.close();
        stmt.close();
        con.close();

        return score;
    }
}
