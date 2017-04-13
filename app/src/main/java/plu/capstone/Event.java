package plu.capstone;

/**
 * Created by cboe1 on 4/4/2017.
 */

public class Event {
    public String description;
    public String loc;
    public String link;
    public String category;

    public Event(){

    }
    public Event(String description, String loc, String link, String category){
        this.description = description;
        this.loc = loc;
        this.link = link;
        this.category = category;
    }
}
