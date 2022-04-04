package model.game.minigames.rpg;

import model.game.minigames.rpg.objects.*;

import play.api.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

@Singleton
public class FightFactory {
    @Inject
    private Database db;

    /**
     * Fight object contains the player, list of enemies and weapons.
     */
    public class Fight {
        private ArrayList<Weapon> weapons;
        private ArrayList<Enemy> enemies;
        private Player player;

        public Fight(ArrayList<Weapon> weapons, ArrayList<Enemy> enemies, Player player) {
            this.weapons = weapons;
            this.enemies = enemies;
            this.player = player;
        }

        public Player getPlayer() {
            return player;
        }

        public ArrayList<Weapon> getWeapons() {
            return weapons;
        }

        public ArrayList<Enemy> getEnemies() {
            return enemies;
        }
    }

    /**
     * Creates a fight object.
     *
     * @return Fight object with the data from the database.
     * @throws SQLException
     */
    public Fight create() throws SQLException {
        int health = 100;
        Player p = new Player(health);
        ArrayList<Weapon> w = fromDBgetWeapons();
        ArrayList<Enemy> e = fromDBgetEnemies();
        Fight fight = new Fight(w, e, p);
        return fight;
    }

    /**
     * Getting all enemies from the database.
     *
     * @return An arraylist with all enemies.
     * @throws SQLException
     */
    private ArrayList<Enemy> fromDBgetEnemies() throws SQLException {
        Connection con = db.getConnection();

        /**
         * Selecting all enemies and the defense values of each one.
         */
        String query = "SELECT enemy.*, GROUP_CONCAT(enemy_defense.value ORDER BY enemy_defense.enemy_defense_id ASC) " +
                "AS defenseValues FROM enemy, enemy_defense WHERE enemy_defense.enemy_id = enemy.enemy_id " +
                "GROUP BY enemy.enemy_id;";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);


        ArrayList<Enemy> enemies = new ArrayList<>();

        while (resultSet.next()) {
            int id = resultSet.getInt("enemy_id");
            String name = resultSet.getString("name");
            int health = resultSet.getInt("health");
            int damage = resultSet.getInt("damage");
            String valueString = resultSet.getString("defenseValues");

            /**
             * Defense values come as an string separated by commas.
             * String needs to be splitted and parsed as integers.
             */
            ArrayList<String> valueList = new ArrayList<>(Arrays.asList((valueString.split(","))));
            ArrayList<Integer> defenseValues = new ArrayList<>();
            for (int i = 0; i < valueList.size(); i++) {
                defenseValues.add(Integer.parseInt(valueList.get(i)));
            }

            Enemy tmp = new Enemy(name, health, damage, defenseValues);
            enemies.add(tmp);
        }

        resultSet.close();
        stmt.close();
        con.close();

        return enemies;
    }

    private ArrayList<Weapon> fromDBgetWeapons() throws SQLException {
        Connection con = db.getConnection();

        /**
         * Selecting all weapons with their defense types.
         */
        String query = "SELECT weapon.*, defense_type.name AS typeName, defense_type.defense_type_id AS type " +
                "FROM weapon, defense_type WHERE weapon.weapon_id = defense_type.weapon_id;";

        Statement stmt = con.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        ArrayList<Weapon> weapons = new ArrayList<>();

        while (resultSet.next()) {
            String name = resultSet.getString("name");
            int damage = resultSet.getInt("damage");
            int type = resultSet.getInt("type");
            String typeName = resultSet.getString("typeName");

            Weapon tmp = new Weapon(name, damage, type, typeName);
            weapons.add(tmp);
        }

        resultSet.close();
        stmt.close();
        con.close();

        return weapons;
    }

    /**
     * Calculates the multiplier.
     *
     * @param turns The turns the player needed for finishing the game.
     * @return The calculated multiplier.
     */
    public double getMultiplier(int turns) {
        double turnsMinimum = 10;
        double factorMaximum = 2;
        double turns_double = (double) turns;
        double result;
        /**
         * Minimum of needed turns is 10, so everything below isn't possible and returns 1.
         * More than 19 are considered as a weak performance, so the factor is again 1.
         */
        if (turns > 19 || turns < 10) {
            result = 1;
        } else {
            /**
             * 10 turns return a factor of 2.
             * Every further turn decreases factor by 0.1.
             */
            double turnsResult = (turns_double - turnsMinimum) / 10;
            result = factorMaximum - turnsResult;
        }
        return result;
    }

}
