package in.snowcraft.msm;

/**
 * Created by howard on 3/31/14.
 */
public class Player {

    private String name;
    private int health;
    private int armor;
    private int food;

    public Player(String name, int health, int food){
        this.name = name;
        this.health = health;
        this.food = food;
    }

    public String getName() { return name; }
    public Integer getHealth() { return health; }
    public Integer getFood() { return food; }



}
