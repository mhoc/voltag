package edu.purdue.voltag.data;

/**
 * Created by mike on 4/26/14.
 */
public class Game {

    private String id;
    private String name;

    public Game(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
