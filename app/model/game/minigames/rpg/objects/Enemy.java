package model.game.minigames.rpg.objects;

import java.util.ArrayList;

/**
 * An enemy which can be fought by the player.
 */
public class Enemy {

    private String name;
    private int health;
    private int damage;
    private ArrayList<Integer> defenseValues;

    /**
     * Creates an enemy object.
     *
     * @param name   Its name to output.
     * @param health Its health value.
     * @param damage The damage it gives to the player.
     */
    public Enemy(String name, int health, int damage, ArrayList<Integer> defenseValues) {
        this.name = name;
        this.health = health;
        this.damage = damage;
        this.defenseValues = defenseValues;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getDamage() {
        return damage;
    }

    public ArrayList<Integer> getDefenseValues() {
        return defenseValues;
    }
}
