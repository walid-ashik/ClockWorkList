package clockworktt.gaby.com;

/**
 * Created by user on 1/23/2018.
 */

public class GuestDetails {

    private String first_name;
    private String last_name;
    private String occupation;
    private String image;
    private String company;
    private String salutation;

    public boolean getArrived() {
        return arrived;
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }

    private boolean arrived;

    public GuestDetails() {
    }

    public GuestDetails(String first_name, String last_name, String occupation, String image, String company, String salutation, boolean arrived) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.occupation = occupation;
        this.image = image;
        this.company = company;
        this.salutation = salutation;
        this.arrived = arrived;
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

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }
}
