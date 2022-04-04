package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.db.UserFactory;
import model.game.GameFactory;
import model.game.quiz.Answer;
import model.game.quiz.KindOfQuiz;
import model.game.quiz.QuestionFactory;
import model.game.quiz.Quiz;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's quiz page.
 */
public class QuizController extends Controller {
    @Inject
    private Quiz quiz;
    @Inject
    private QuestionFactory qf;
    @Inject
    private GameFactory gf;

    private QuestionFactory.Question question;
    private GameFactory.Game game;
    private int user_id;

    /**
     * Default for /quiz. Generates or gets group of questions for a user.
     * Or ends the game if all questions of a group are answered.
     *
     * @return Renders first unanswered question from the group of questions.
     */
    public Result index() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            if (!gf.hasGame(user_id)) {
                return redirect("/game");
            } else {
                game = gf.getGameByUserId(user_id);
                // for /quiz requests when your not in a quiz round
                if (!(game.getProgress_count() == 0 ||
                        game.getProgress_count() == 2 ||
                        game.getProgress_count() == 4)) {
                    return redirect("/game");
                }
            }

            KindOfQuiz kind;
            if (quiz.hasQuiz(user_id)) {
                if (quiz.hasOpenQuestion(user_id)) {
                    question = quiz.getNextQuestion(user_id);
                } else {
                    quiz.finish(user_id);
                    game.updateProgress();
                    game.update();
                    return redirect("/game");
                }
            } else {
                kind = getKind();
                quiz.createQuiz(user_id, kind);
                question = quiz.getNextQuestion(user_id);
            }
            return ok(views.html.quiz.render(game.getPoints_game(), question, question.getAnswers(), quiz.getCategory(user_id), quiz.getScore(user_id), quiz.getProgress(user_id), quiz.getNumberOfQuestions(user_id), game.getProgress_count() + 1));

        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Handles POST requests for /quiz. Checks a given answer to the current question.
     *
     * @return Renders the next unanswered question from a pool for a user.
     * @throws SQLException
     */
    public Result checkAnswer() throws SQLException {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        JsonNode json = request().body().asJson();
        int question_id = json.get("question_id").asInt();
        int answer_id = json.get("answer_id").asInt();

        question = qf.getQuestionById(question_id);
        List<Answer> answersCorrect = question.getAnswersCorrect();
        JsonNode answersJson = Json.toJson(answersCorrect);

        String status = "alreadyAnswered";
        if (quiz.isNotAnswered(user_id, question_id)) {
            boolean answerIsCorrect = question.isCorrectAnswer(answer_id);
            status = Boolean.toString(answerIsCorrect);
            if (answerIsCorrect) {
                quiz.updateQuestionStatus(user_id, question_id, true);
            } else {
                quiz.updateQuestionStatus(user_id, question_id, false);
            }
        }

        int currentScore = quiz.getScore(user_id);
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        final ObjectNode node = factory.objectNode();
        node.put("status", status);
        node.put("score", currentScore);
        node.set("answers", answersJson);
        String jsonAnswer = node.toString();

        return ok(jsonAnswer);
    }

    private KindOfQuiz getKind() {
        switch (game.getProgress_count()) {
            case 0:
                return KindOfQuiz.General;
            case 2:
                return KindOfQuiz.Diseases;
            case 4:
                return KindOfQuiz.Treatment;
            default:
                return KindOfQuiz.General;
        }
    }
}
