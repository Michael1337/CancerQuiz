package model.game.minigames.memory;

import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.LinkedList;

@Singleton
public class MemoryFactory {
    @Inject
    private Database db;

    /**
     * This class defines a single memory card with its various attributes.
     */
    public class Card {
        private int cardId;
        private String cardName;
        private String cardPath;

        /**
         * Creates a new Card instance.
         *
         * @param cardId   Id of the card.
         * @param cardName Name of the card.
         * @param cardPath Filepath of the card image.
         */
        public Card(int cardId, String cardName, String cardPath) {
            this.cardId = cardId;
            this.cardName = cardName;
            this.cardPath = cardPath;
        }

        public int getCardId() {
            return cardId;
        }

        public String getCardName() {
            return cardName;
        }

        public String getCardPath() {
            return cardPath;
        }
    }

    /**
     * Calculates a reasonable multiplier, that is needed to calculate the overall points of one quiz round.
     *
     * @param moves The amount of moves with which the user finished the memory.
     * @return Returns multiplier.
     */
    public double getMultiplier(int moves) {
        double movesDouble = (double) moves;
        if (movesDouble >= 10 && movesDouble <= 15) {
            return 2.0;
        } else if (movesDouble > 15 && movesDouble < 65) {
            return (2.0 - ((movesDouble - 15) / 50));
        } else {
            //if needed moves were 65 or more, or if memory was skipped
            return 1.0;
        }
    }

    /**
     * Defines the SQL query and calls the executing method fromDBgetCards(String query).
     *
     * @return Returns a list of ten card objects that were randomly selected out of the database table memory_img.
     * @throws SQLException if query is not valid.
     */
    public LinkedList<MemoryFactory.Card> fromDBgetRandomCards() throws SQLException {
        String query = "SELECT * FROM memory_img ORDER BY RAND() LIMIT 10;";
        return fromDBgetCards(query);
    }

    /**
     * Connects to database and executes the passed SQL query. Creates card objects using the received data from the database.
     *
     * @param query The passed SQL query.
     * @return Returns a list of card objects.
     * @throws SQLException if query is not valid.
     */
    private LinkedList<MemoryFactory.Card> fromDBgetCards(String query) throws SQLException {
        Connection con = db.getConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        LinkedList<Card> res = new LinkedList<Card>();
        while (rs.next()) {
            res.add(new MemoryFactory.Card(
                    rs.getInt("memory_img_id"),
                    rs.getString("name"),
                    rs.getString("file_path")
            ));
        }

        rs.close();
        stmt.close();
        con.close();

        return res;
    }

    /**
     * Creates a new card object.
     *
     * @param cardId   Id of the card.
     * @param cardName Name of the card.
     * @param cardPath Filepath of the card image.
     * @return Returns a new card object.
     */
    public MemoryFactory.Card createMemoryCard(int cardId, String cardName, String cardPath) {
        return new MemoryFactory.Card(cardId, cardName, cardPath);
    }
}
