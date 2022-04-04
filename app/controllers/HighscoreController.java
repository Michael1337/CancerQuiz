package controllers;

import model.db.UserFactory;
import play.mvc.*;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class HighscoreController extends Controller {
    @Inject
    private UserFactory uf;

    private int user_id;

    /**
     * Default for /highscore.
     *
     * @return Renders a list of all Users and their points. Shall also highlight the logged in User.
     */
    public Result index() {

        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            List<UserFactory.User> users = uf.fromDB();
            return ok(views.html.highscore.render(users, user_id));
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

}
