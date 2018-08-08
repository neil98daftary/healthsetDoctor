package in.ashnehete.healthsetdoctor.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static in.ashnehete.healthsetdoctor.AppConstants.DATE_FORMAT;
import static in.ashnehete.healthsetdoctor.AppConstants.HUMAN_DATE_FORMAT;

/**
 * Created by Aashish Nehete on 09-Feb-18.
 */

@IgnoreExtraProperties
public class Record {
    private String device;
    private String timestamp;
    private String value;

    public Record(String device, String timestamp, String value) {
        this.device = device;
        this.timestamp = timestamp;
        this.value = value;
    }

    public Record() {

    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getNormalTimestamp() {
        return timestamp;
    }

    public String getTimestamp() {
        try {
            Date date = new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(timestamp);
            return new SimpleDateFormat(HUMAN_DATE_FORMAT, Locale.US).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return timestamp;
        }
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
