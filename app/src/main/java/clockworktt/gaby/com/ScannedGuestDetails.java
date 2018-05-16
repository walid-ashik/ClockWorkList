package clockworktt.gaby.com;

/**
 * Created by user on 2/4/2018.
 */

public class ScannedGuestDetails {

    private String first_name;
    private String last_name;
    private String image;
    private String gender;
    private long timestamp;
    private String fb_link;

    private String fb_username;

    public ScannedGuestDetails() {
    }



    public ScannedGuestDetails(String first_name, String last_name, String image, String gender, long timestamp, String fb_link, String fb_username) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.image = image;
        this.gender = gender;
        this.fb_link = fb_link;
        this.timestamp = timestamp;
        this.fb_username = fb_username;
    }


    public String getFb_username() {
        return fb_username;
    }

    public void setFb_username(String fb_username) {
        this.fb_username = fb_username;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFb_link() {
        return fb_link;
    }

    public void setFb_link(String fb_link) {
        this.fb_link = fb_link;
    }

}
