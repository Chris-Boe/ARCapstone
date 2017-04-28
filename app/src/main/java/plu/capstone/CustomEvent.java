package plu.capstone;

/**
 * Created by cboe1 on 4/4/2017.
 */

public class CustomEvent {
    private String description;
    private String loc;
    private String link;
    private String category;

    public CustomEvent(){

    }
    public CustomEvent(String description, String loc, String link, String category){
        this.description = description;
        this.loc = loc;
        this.link = link;
        this.category = category;
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
}
