package controllers;

import model.game.GameFactory;
import model.game.minigames.clicker.ClickerGameFactory;
import model.game.quiz.Quiz;
import play.mvc.Controller;
import play.mvc.Result;
import com.fasterxml.jackson.databind.JsonNode;

import javax.inject.Inject;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ClickerController extends Controller {
    @Inject
    private ClickerGameFactory cgf;
    @Inject
    private GameFactory gf;
    @Inject
    private Quiz quiz;
    private int user_id;
    private ClickerGameFactory.ClickerGame cg;
    private GameFactory.Game game;

    /**
     * Default for /clicker.
     *
     * @return Renders the CancerClicker game
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
                if (game.getProgress_count() != 3) {
                    return redirect("/game");
                }
            }

            ClickerGameFactory.ClickerStats clickerStats = null;
            if (cgf.hasStats(user_id)) {
                clickerStats = cgf.createStats(user_id);
            }
            int points_game = game.getPoints_game();
            int points_quiz = quiz.getScoreOfLastQuiz(user_id);
            return ok(views.html.clicker.render(points_game, points_quiz, clickerStats));
        } catch (SQLException e) {
            return badRequest();
        }
    }

    /**
     * Handels post requests for /clicker. Called when the game is ended by the client.
     * Gets the amount of points from the client and stores them into the database.
     *
     * @return Returns ok(). The client will redirect the user to the next stage.
     */
    public Result endGame() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            if (!gf.hasGame(user_id)) {
                return ok();
            } else {
                game = gf.getGameByUserId(user_id);
                if (game.getProgress_count() != 3) {
                    return ok();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JsonNode json = request().body().asJson();
        int points = json.get("points").asInt();
        double multiplier = cgf.getMultiplier(points);
        try {
            game = gf.getGameByUserId(user_id);
            game.multiplyPoints(multiplier);
            game.updateProgress();
            game.update();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // for statistics
        try {
            cg = cgf.createGame(user_id, points, LocalDateTime.now());
            cg.finish();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ok();
    }
}
