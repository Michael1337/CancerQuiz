package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import model.db.UserFactory;
import model.game.GameFactory;
import model.game.minigames.rpg.FightFactory;
import model.game.quiz.Quiz;
import play.libs.Json;
import play.mvc.*;

import javax.inject.Inject;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class FightGameController extends Controller {

    @Inject
    private FightFactory ff;
    @Inject
    private GameFactory gf;
    @Inject
    private UserFactory uf;
    @Inject
    private Quiz quiz;

    private int user_id;
    private FightFactory.Fight fight;
    private GameFactory.Game game;
    private String username;
    private UserFactory.User user;

    /**
     * Default for /fightGame.
     *
     * @return Renders the fightGame page.
     */
    public Result index() {

        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }

        try {
            fight = ff.create();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        user_id = Integer.parseInt(session("user_id"));

        try {
            if (!gf.hasGame(user_id)) {
                return redirect("/game");
            } else {
                game = gf.getGameByUserId(user_id);
                user = uf.fromDBWithID(user_id);
                username = user.getName();
                if (game.getProgress_count() != 5) {
                    return redirect("/game");
                }
            }

            JsonNode fightJson = Json.toJson(fight);
            return ok(views.html.fightGame.render(game.getPoints_game(), quiz.getScoreOfLastQuiz(user_id), username, fight, fight.getWeapons(), fightJson));

        } catch (SQLException e) {
            return badRequest();
        }


    }

    /**
     * Handels post requests for /fightGame. Called when the game is ended by the client.
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
                if (game.getProgress_count() != 5) {
                    return ok();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JsonNode json = request().body().asJson();
        int points = json.get("turns").asInt();
        double multiplier = ff.getMultiplier(points);
        try {
            game = gf.getGameByUserId(user_id);
            game.multiplyPoints(multiplier);
            game.updateProgress();
            game.update();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ok();
    }

}
