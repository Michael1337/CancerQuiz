package model.game.quiz;

import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class QuestionFactory {
    @Inject
    private Database db;

    public class Question {
        private int question_id;
        private int category_id;
        private int difficulty;
        private String question_text;
        private List<Answer> answers;

        /**
         * Creates a new Question.
         *
         * @param question_id   The unique id of the question.
         * @param category_id   The category/kind of the question.
         * @param difficulty    The difficulty of the question.
         * @param question_text The actual question.
         * @throws SQLException
         */
        public Question(int question_id, int category_id, int difficulty, String question_text) throws SQLException {
            this.question_id = question_id;
            this.category_id = category_id;
            this.difficulty = difficulty;
            this.question_text = question_text;
            this.answers = getAnswersFromDB();
        }

        public int getQuestion_id() {
            return question_id;
        }

        public int getCategory_id() {
            return category_id;
        }

        public int getDifficulty() {
            return difficulty;
        }

        public String getQuestion_text() {
            return question_text;
        }

        public List<Answer> getAnswers() {
            return answers;
        }

        /**
         * Get all correct answers for a question.
         *
         * @return Returns a List with all answers that are correct.
         */
        public List<Answer> getAnswersCorrect() {
            List<Answer> correctAnswers = answers.stream()
                    .filter(p -> p.isCorrect()).collect(Collectors.toList());
            return correctAnswers;
        }

        /**
         * Checks if a given answer is correct.
         *
         * @param answer_id The unique id of an answer.
         * @return Returns true if the answer is correct for it's question.
         * @throws SQLException
         */
        public boolean isCorrectAnswer(int answer_id) {
            boolean correct = answers.stream()
                    .filter(p -> p.isCorrect()).anyMatch(p -> p.getAnswer_id() == answer_id);
            return correct;
        }

        /**
         * Gets all possible answers for a question from the db.
         *
         * @return Returns a list of Answers.
         * @throws SQLException
         */
        private List<Answer> getAnswersFromDB() throws SQLException {
            Connection con = db.getConnection();

            String query = "SELECT * FROM answer WHERE question_id = '" + question_id + "';";

            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);
            List<Answer> answers = new LinkedList<>();

            while (resultSet.next()) {
                answers.add(new Answer(
                        resultSet.getInt("answer_id"),
                        resultSet.getInt("question_id"),
                        resultSet.getString("text"),
                        resultSet.getBoolean("correct"))
                );
            }

            Collections.shuffle(answers);

            resultSet.close();
            stmt.close();
            con.close();

            return answers;
        }
    }

    /**
     * Gets a question from the database and creates a question instance for it.
     *
     * @param questionID The unique id of the question.
     * @return Returns a Question.
     * @throws SQLException
     */
    public Question getQuestionById(int questionID) throws SQLException {
        Connection con = db.getConnection();

        String query = "SELECT * FROM question WHERE question_id = '" + questionID + "';";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);

        resultSet.next();
        Question question = new Question(
                resultSet.getInt("question_id"),
                resultSet.getInt("category_id"),
                resultSet.getInt("difficulty"),
                resultSet.getString("text"));

        resultSet.close();
        stmt.close();
        con.close();

        return question;
    }
}