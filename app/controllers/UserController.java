package controllers;

import model.db.RankFactory;
import model.db.UserFactory;
import model.friends.MessageFactory;
import model.game.GameStatsFactory;
import play.mvc.*;

import javax.inject.Inject;
import java.sql.SQLException;

public class UserController extends Controller {
    @Inject
    private UserFactory uf;
    @Inject
    private RankFactory rf;
    @Inject
    private GameStatsFactory gsf;
    @Inject
    private MessageFactory mf;
    
    private int user_id;

    /**
     * Default for /user. Shows the profile of the logged in user.
     */
    public Result index() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        UserFactory.User user;
        RankFactory.Rank rank;
        try {
            user = uf.fromDBWithID(user_id);
            rank = rf.getRankByPointsOverall(user.getPoints_overall());
            int unreadMessages = mf.countUnreadMessages(user_id);
            if (gsf.hasStats(user_id)) {
                GameStatsFactory.GameStatsGroup gsg = gsf.getGameStatsGroup(user_id);
                return ok(views.html.profile_self.render(user, rank, unreadMessages, gsg));
            } else {
                return ok(views.html.profile_self.render(user, rank, unreadMessages, null));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }
}