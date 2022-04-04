package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import model.db.UserFactory;
import model.friends.FriendsFactory;
import model.friends.MessageFactory;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class MessagesController extends Controller {
    @Inject
    private FormFactory formFactory;
    @Inject
    private FriendsFactory ff;
    @Inject
    private MessageFactory mf;

    private int user_id;

    /**
     * Default for /messages. Displays all messages a user has.
     *
     * @return Renders a list of messages (received and sent) for the logged in user.
     */
    public Result showMessages() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            List<MessageFactory.Message> messages_received = mf.fromDBWithReceiver(user_id);
            List<MessageFactory.Message> messages_sent = mf.fromDBWithSender(user_id);
            boolean hasFriends = ff.hasFriends(user_id);
            return ok(views.html.messages.render(messages_received, messages_sent, hasFriends));
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Handles post request for /messagesRead. Marks a message as read.
     *
     * @return Set "read" to 1 for a message and returns to /messages.
     */
    public Result readMessage() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            JsonNode json = request().body().asJson();
            int message_id = json.get("message_id").asInt();
            MessageFactory.Message message = mf.fromDBWithID(message_id);
            message.setRead(1);
            message.update();
            return ok();
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Handles post requests for /messagesSend. Creates new messages with form input.
     *
     * @return Creates a new message and returns to /messages.
     */
    public Result sendMessage() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            JsonNode json = request().body().asJson();
            int receiver_id = json.get("receiver_id").asInt();
            int sender_id = user_id;
            String title = json.get("title").asText();
            String text = json.get("text").asText();
            if (ff.areFriends(receiver_id, sender_id)) {
                mf.create(receiver_id, sender_id, title, text);
            }
            return ok();
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Handles post requests for /messagesDelete. Deletes a message by it's id.
     *
     * @return Deletes a message and returns to /messages.
     */
    public Result deleteMessage() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            JsonNode json = request().body().asJson();
            int message_id = json.get("message_id").asInt();
            mf.deletebyId(message_id);
            return ok();
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Default for /message. Shows a form to write a message.
     *
     * @return Renders a form with empty input field to create a new message.
     */
    public Result newMessage() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            return ok(views.html.messages_form.render(ff.getFriendList(user_id), 0, "", ""));
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Handles post requests for /message. Shows a form to write a message as an answer.
     *
     * @return Renders a form with default values for Receiver and Title according to a received message.
     */
    public Result newMessageWithData() {
        if (!session().containsKey("user_id")) {
            return redirect("/login");
        }
        user_id = Integer.parseInt(session("user_id"));

        try {
            DynamicForm dynamicForm = formFactory.form().bindFromRequest();
            int receiver_id = Integer.parseInt(dynamicForm.get("receiver_id"));
            String title = dynamicForm.get("title");
            String text = dynamicForm.get("text");
            if (ff.areFriends(user_id, receiver_id)) {
                return ok(views.html.messages_form.render(ff.getFriendList(user_id), receiver_id, title, text));
            } else {
                return ok(views.html.messages_form.render(ff.getFriendList(user_id), 0, "", "ACHTUNG: Der gesuchte Adressat ist leider nicht mehr dein Freund. Um ihm/ihr eine Nachricht schicken zu können, müsst ihr zunächst erneut eine Freundschaft schließen."));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

}
