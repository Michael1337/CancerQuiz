package model.db;

import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;

@Singleton
public class RankFactory {
    @Inject
    private Database db;

    public class Rank {
        private int rank_id;
        private String name_thisRank;
        private int minPoints_thisRank;
        private String name_nextRank;
        private int minPoints_nextRank;

        public Rank(int rank_id, String name_thisRank, int minPoints_thisRank, String name_nextRank, int minPoints_nextRank) {
            this.rank_id = rank_id;
            this.name_thisRank = name_thisRank;
            this.minPoints_thisRank = minPoints_thisRank;
            this.name_nextRank = name_nextRank;
            this.minPoints_nextRank = minPoints_nextRank;
        }

        public int getRank_id() {
            return rank_id;
        }

        public void setRank_id(int rank_id) {
            this.rank_id = rank_id;
        }

        public String getName_thisRank() {
            return name_thisRank;
        }

        public void setName_thisRank(String name_thisRank) {
            this.name_thisRank = name_thisRank;
        }

        public int getMinPoints_thisRank() {
            return minPoints_thisRank;
        }

        public void setMinPoints_thisRank(int minPoints_thisRank) {
            this.minPoints_thisRank = minPoints_thisRank;
        }

        public String getName_nextRank() {
            return name_nextRank;
        }

        public void setName_nextRank(String name_nextRank) {
            this.name_nextRank = name_nextRank;
        }

        public int getMinPoints_nextRank() {
            return minPoints_nextRank;
        }

        public void setMinPoints_nextRank(int minPoints_nextRank) {
            this.minPoints_nextRank = minPoints_nextRank;
        }
    }

    public Rank getRankByPointsOverall(int points_overall) throws SQLException {
        Connection con = db.getConnection();
        String query = "SELECT * FROM rank WHERE points_overall <= " + points_overall + " ORDER BY points_overall DESC LIMIT 1;";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);

        resultSet.next();
        int rank_id = resultSet.getInt("rank_id");
        String name_thisRank = resultSet.getString("name");
        int minPoints_thisRank = resultSet.getInt("points_overall");

        resultSet.close();
        stmt.close();
        query = "SELECT * FROM rank WHERE points_overall > " + points_overall + " ORDER BY points_overall ASC LIMIT 1;";

        stmt = con.createStatement();
        resultSet = stmt.executeQuery(query);

        String name_nextRank;
        int minPoints_nextRank;

        if (resultSet.isBeforeFirst()) {
            resultSet.next();
            name_nextRank = resultSet.getString("name");
            minPoints_nextRank = resultSet.getInt("points_overall");
        } else {
            name_nextRank = "Höchste Stufe erreicht!";
            minPoints_nextRank = points_overall;
        }

        Rank rank = new Rank(rank_id, name_thisRank, minPoints_thisRank, name_nextRank, minPoints_nextRank);

        resultSet.close();
        stmt.close();
        con.close();

        return rank;
    }
}