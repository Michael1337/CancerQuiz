package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import model.game.GameFactory;
import model.game.minigames.memory.MemoryFactory;
import model.game.quiz.Quiz;
import play.mvc.*;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.LinkedList;

public class MemoryController extends Controller {
    @Inject
    private MemoryFactory mf;
    @Inject
    private GameFactory gf;
    @Inject
    private Quiz quiz;

    private GameFactory.Game game;
    private int user_id;

    /**
     * Default for /memory.
     *
     * @return Delivers a card deck from the database and renders the memory game page
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

                if (game.getProgress_count() != 1) {
                    return redirect("/game");
                }
            }

            //Adds each of the ten randomly selected cards from the database twice to the card deck
            LinkedList<MemoryFactory.Card> cardDeck = new LinkedList<>();
            for (MemoryFactory.Card card : mf.fromDBgetRandomCards()) {
                card = mf.createMemoryCard(card.getCardId(), card.getCardName(), card.getCardPath());
                cardDeck.add(card);
                cardDeck.add(card);
            }
            return ok(views.html.memory.render(quiz.getScoreOfLastQuiz(user_id), cardDeck));
        } catch (SQLException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    /**
     * Is called when all the memory card pairs are found or if the client skips the game.
     * Gets the amount of needed moves by handling an ajax POST request, calculates the multiplier and stores the resulting points in the database.
     *
     * @return Returns Ok(). The increase of the game progress by updateProgress() redirects to the next page.
     */
    public Result endGame() {
        try {
            JsonNode json = request().body().asJson();
            int moves = json.get("moves").asInt();
            double multiplier = mf.getMultiplier(moves);

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