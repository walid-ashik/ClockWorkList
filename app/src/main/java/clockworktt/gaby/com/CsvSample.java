package clockworktt.gaby.com;

/**
 * Created by user on 1/20/2018.
 */

public class CsvSample {

    private String salutation;
    private String first_name;
    private String last_name;
    private String email;
    private String contact_number;
    private String company;
    private String occupation;
    private String expected_numbers;
    private String notes;

    public CsvSample() {
    }

    public CsvSample(String salutation, String first_name, String last_name, String email, String contact_number, String company, String occupation, String expected_numbers, String notes) {
        this.salutation = salutation;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.contact_number = contact_number;
        this.company = company;
        this.occupation = occupation;
        this.expected_numbers = expected_numbers;
        this.notes = notes;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getExpected_numbers() {
        return expected_numbers;
    }

    public void setExpected_numbers(String expected_numbers) {
        this.expected_numbers = expected_numbers;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CsvSample{" +
                "salutation='" + salutation + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", email='" + email + '\'' +
                ", contact_number='" + contact_number + '\'' +
                ", company='" + company + '\'' +
                ", occupation='" + occupation + '\'' +
                ", expected_numbers='" + expected_numbers + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
