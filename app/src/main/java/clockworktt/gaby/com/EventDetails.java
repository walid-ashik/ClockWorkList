package clockworktt.gaby.com;

/**
 * Created by user on 1/22/2018.
 */

public class EventDetails {

    private String date;
    private String icon;
    private String description;
    private String title;
    private String total_guests;

    public EventDetails() {
    }

    public EventDetails(String date, String icon, String description, String title, String total_guests) {
        this.date = date;
        this.icon = icon;
        this.description = description;
        this.title = title;
        this.total_guests = total_guests;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTotal_guests() {
        return total_guests;
    }

    public void setTotal_guests(String total_guests) {
        this.total_guests = total_guests;
    }
}
