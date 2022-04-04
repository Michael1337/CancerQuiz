package controllers;

import play.mvc.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * Default for "/". This should redirect to /login or /user.
     *
     * @return Renders either a general information page or the profile of the logged in user.
     */
    public Result index() {
        if (!session().containsKey("user_id")) {
            return ok(views.html.index.render());
        } else {
            return redirect("/user");
        }
    }

    /**
     * Default for "/impressum".
     *
     * @return Renders a page showing legal information.
     */
    public Result impressum() {
        if (!session().containsKey("user_id")) {
            return ok(views.html.impressum.render(0));
        } else {
            return ok(views.html.impressum.render(1));
        }
    }
}
