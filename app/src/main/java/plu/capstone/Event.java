package plu.capstone;

/**
 * Created by cboe1 on 4/4/2017.
 */

public class Event {
    public String description;
    public String link;
    public String category;

    public Event(){

    }
    public Event(String description, String link, String category){
        this.description = description;
        this.link = link;
        this.category = category;
    }
}
