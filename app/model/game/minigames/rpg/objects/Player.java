package model.game.minigames.rpg.objects;

/**
 * The player the user plays with.
 */
public class Player {
    private int health;
    private Weapon weapon;

    /**
     * Creates a player object.
     * @param health The health value.
     */
    public Player(int health) {
        this.health = health;
        this.weapon = null;
    }

    public int getHealth() {
        return health;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }
}