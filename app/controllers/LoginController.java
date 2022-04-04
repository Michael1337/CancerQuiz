package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.db.UserFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class LoginController extends Controller {
    @Inject
    private UserFactory uf;

    /**
     * Default for /login. Logged in Users shall not access this page.
     *
     * @return Renders a simple login form.
     */
    public Result loginForm() {
        if (!session().containsKey("user_id")) {
            return ok(views.html.login.render());
        }
        return redirect("/");
    }

    /**
     * Default for /register. Logged in Users shall not access this page.
     *
     * @return Renders a simple registration form.
     */
    public Result registerForm() {
        if (!session().containsKey("user_id")) {
            return ok(views.html.register.render());
        }
        return redirect("/");

    }

    /**
     * Handles a POST request for a new user registration.
     * Ideally creates a new User with given information.
     *
     * @return Creates and logs in User and sends him an answer.
     */
    public Result register() {
        JsonNode json = request().body().asJson();
        String username = json.get("username").asText();
        String email = json.get("email").asText();
        String password = json.get("password").asText();

        try {
            if (uf.nameExists(username)) {
                String error = "Ein Benutzer mit diesem Namen existiert bereits.";
                return sendAnswer(error);
            }
            if (uf.emailExists(email)) {
                String error = "Ein Benutzer mit dieser E-Mail-Adresse existiert bereits.";
                return sendAnswer(error);
            }

            UserFactory.User user = uf.create(username, email, password);
            session("user_id", Integer.toString(user.getUser_id()));
            String error = "";
            return sendAnswer(error);
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Handles a POST request for a login attempt.
     * Ideally logs in a User with given credentials.
     *
     * @return Logs in User and sends him an answer.
     */
    public Result login() {

        JsonNode json = request().body().asJson();
        String username = json.get("username").asText();
        String password = json.get("password").asText();

        List<UserFactory.User> users;
        try {
            users = uf.fromDBWithCredentials(username, password);
            if (users.isEmpty()) {
                String error = "Logindaten falsch.";
                return sendAnswer(error);
            } else {
                UserFactory.User user = users.get(0);
                user.setLast_login(LocalDateTime.now());
                user.update();
                session("user_id", Integer.toString(user.getUser_id()));
                String error = "";
                return sendAnswer(error);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Default for /logout.
     *
     * @return Destroys a user session and redirects a visitor to the login page.
     */
    public Result logout() {
        session().remove("user_id");
        return ok(views.html.index.render());
    }

    /**
     * Sends an answer as Json for an AJAX request.
     *
     * @param message The Message of the answer.
     * @return Returns ok(200) and a message to the client.
     */
    private Result sendAnswer(String message) {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        final ObjectNode node = factory.objectNode();
        node.put("error", message);

        return ok(node);
    }

}
