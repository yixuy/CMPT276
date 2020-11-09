package cmpt276.helium.app.model;

import android.content.Context;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import cmpt276.helium.app.Constants;
import cmpt276.helium.app.R;

/*
    Model class that holds the data associated with a particular inspection report of a restaurant,
    identified by the trackingNum. Implements the Comparable interface to sort by date
 */
public class Report implements Comparable<Report> {

    // Used as a primary key to identify reports when passing through Intents, as trackingNum +
    // date is surprisingly not always unique
    private int uniqueKey;

    private String trackingNum;
    private String date;
    private String type;
    private int numCriticalIssues;
    private int numNonCriticalIssues;
    private String hazardRating;
    private ArrayList<Integer> violationsIDs = new ArrayList<>();

    // Constructor that sets a primary key and parses a .csv line that has been split by ","
    public Report(int uniqueKey, String[] reportData) {

        this.uniqueKey = uniqueKey;

        trackingNum = reportData[0];
        date = reportData[1];
        type = reportData[2];

        hazardRating = reportData[6];
        if (hazardRating == null || hazardRating.isEmpty()) {
            hazardRating = "Unknown";
        }

        if (reportData[3] != null) {
            numCriticalIssues = Integer.parseInt(reportData[3]);
            numNonCriticalIssues = Integer.parseInt(reportData[4]);
        }

        // If the array has a 6th element, that means there were violations in this report
        if (reportData.length == 7 && !reportData[5].isEmpty()) {

            String violLump = reportData[5];
            String[] violationsData = violLump.split("\\|");

            for (String violationLump : violationsData) {

                // We don't need to store the violation data itself, only its ID
                // because we can look it up in our hash table later
                String[] violationData = violationLump.split(",");
                int violationID = Integer.parseInt(violationData[0]);
                violationsIDs.add(violationID);
            }
        }
    }

    public int getUniqueKey() {
        return uniqueKey;
    }

    public String getDate() {
        return date;
    }

    public int getNumCriticalIssues() {
        return numCriticalIssues;
    }

    public int getNumNonCriticalIssues() {
        return numNonCriticalIssues;
    }

    public int getNumIssues() {
        return numCriticalIssues + numNonCriticalIssues;
    }

    public String getHazardRating() {
        return hazardRating;
    }

    // Abbreviated form for UI (e.g, long words like "Moderate" -> "Med.")
    public String getHazardAbbr(Context context) {
        switch (hazardRating) {
            case "High":
                return context.getString(R.string.high_abbr);
            case "Moderate":
                return context.getString(R.string.moderate_abbr);
            case "Low":
                return context.getString(R.string.low_abbr);
            default:
                return "?";
        }
    }

    public String getType() {
        return type;
    }

    public ArrayList<Integer> getViolationsIDs() {
        return violationsIDs;
    }

    public String getTrackingNum() {
        return trackingNum;
    }

    public boolean isMoreRecentThan(Report other) {
        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());

        try {
            Date dateValue = format.parse(date);
            Date otherDateValue = format.parse(other.getDate());
            return Objects.requireNonNull(dateValue).compareTo(otherDateValue) > 0;

        } catch (ParseException e) {
            return true;
        }
    }

    @Override
    public int compareTo(@NonNull Report other) {
        return isMoreRecentThan(other) ? -1 : 1;
    }

    @NonNull
    @Override
    public String toString() {
        return "Report{" +
                "trackingNum='" + trackingNum + '\'' +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", numCriticalIssues=" + numCriticalIssues +
                ", numNonCriticalIssues=" + numNonCriticalIssues +
                ", hazardRating='" + hazardRating + '\'' +
                '}';
    }
}
