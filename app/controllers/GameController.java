package controllers;

import model.db.RankFactory;
import model.db.UserFactory;
import model.game.GameFactory;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import java.sql.SQLException;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class GameController extends Controller {
    @Inject
    private UserFactory uf;
    @Inject
    private RankFactory rf;
    @Inject
    private GameFactory gf;

    private UserFactory.User user;
    private GameFactory.Game game;
    private RankFactory.Rank oldRank;
    private RankFactory.Rank newRank;
    private int user_id;

    /**
     * Default for /game. Starts a new game or redirects the logged in user to the fitting part of his current game.
     *
     * @return Renders a page to start a new game or redirects to a quiz or a minigame according to the progress of the game.
     */
    public Result index() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            if (!gf.hasGame(user_id)) {
                return ok(views.html.game.render());
            } else {
                game = gf.getGameByUserId(user_id);
                int progress = game.getProgress_count();
                switch (progress) {
                    case 0:
                        return redirect("/quiz");
                    case 1:
                        return redirect("/memory");
                    case 2:
                        return redirect("/quiz");
                    case 3:
                        return redirect("/clicker");
                    case 4:
                        return redirect("/quiz");
                    case 5:
                        return redirect("/fightGame");
                    case 6:
                        user = uf.fromDBWithID(user_id);
                        oldRank = rf.getRankByPointsOverall(user.getPoints_overall());
                        user.updatePoints_max(game.getPoints_game());
                        user.addPoints_overall(game.getPoints_game());
                        user.update();
                        newRank = rf.getRankByPointsOverall(user.getPoints_overall());
                        game.finish();
                        return ok(views.html.game_finish.render(game, user, oldRank, newRank));
                    default:
                        return redirect("/highscore");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Handles POST requests for /game. Gets called when a user starts a game.
     *
     * @return Redirects to first stage of the game, i.e. a quiz.
     */
    public Result startGame() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            if (gf.hasGame(user_id)) {
                return ok();
            } else {
                game = gf.create(user_id);
                return ok();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

}
