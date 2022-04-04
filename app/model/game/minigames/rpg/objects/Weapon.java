package model.game.minigames.rpg.objects;

/**
 * A weapon which can be equipped by the player.
 */
public class Weapon {

    private String name;
    private int damage;
    private int defenseType;
    private String defenseTypeName;

    /**
     * Creates a weapon object.
     *
     * @param name            The weapons name.
     * @param damage          Its attack value.
     * @param defenseType     Its defenseType number.
     * @param defenseTypeName The name of the defenseType.
     */
    public Weapon(String name, int damage, int defenseType, String defenseTypeName) {
        this.name = name;
        this.damage = damage;
        this.defenseType = defenseType;
        this.defenseTypeName = defenseTypeName;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getDefenseType() {
        return defenseType;
    }

    public String getDefenseTypeName() {
        return defenseTypeName;
    }
}
