package clockworktt.gaby.com;

/**
 * Created by user on 2/1/2018.
 */

public class AnalyticsDetails {

    private String first_name;
    private String last_name;
    private boolean arrived;
    private long timestamp;

    public AnalyticsDetails() {
    }

    public AnalyticsDetails(String first_name, String last_name, boolean arrived, long timestamp) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.arrived = arrived;
        this.timestamp = timestamp;
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

    public boolean isArrived() {
        return arrived;
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
