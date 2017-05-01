package plu.capstone.Models;

/**
 * Created by cboe1 on 4/4/2017.
 */

public class CustomEvent {
    private String description;
    private String loc;
    private String link;
    private String category;
    private String startTime;
    private String endTime;

    public CustomEvent(){

    }
    public CustomEvent(String description, String loc, String link, String category, String startTime, String endTime){
        this.description = description;
        this.loc = loc;
        this.link = link;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
