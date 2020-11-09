package cmpt276.helium.app.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cmpt276.helium.app.Constants;
import cmpt276.helium.app.R;
import cmpt276.helium.app.Utils;

/*
    RestaurantManager class that holds all of our restaurant, report, and violation data and
    provides a Singleton to access the data from
 */
public class RestaurantManager {

    private ArrayList<Restaurant> restaurants = new ArrayList<>();
    private ArrayList<Restaurant> filteredRestaurants = null;
    private ArrayList<Report> reports = new ArrayList<>();
    private ArrayList<String> favRestaurantTrackingNums = new ArrayList<>();

    // Violations are stored as a hash table with violationID keys and Violation object values,
    // as we have been provided essentially such a table, and we can have O(1) Violation lookups
    // and not have to store any data beyond the IDs in the Report class
    private HashMap<Integer, Violation> violationsTable = new HashMap<>();

    // Singleton
    private static RestaurantManager instance;

    // Read all the 3 data files and populate the data structures
    private RestaurantManager(Context context) throws IOException {
        loadFavouriteRestaurants(context);
        InputStreamReader reader;

        try {
            reader = new InputStreamReader(
                    context.openFileInput(Constants.RESTAURANTS_FILE_NAME),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            reader = new InputStreamReader(
                    context.getResources().openRawResource(R.raw.restaurants_itr1),
                    StandardCharsets.UTF_8);
        }
        readRestaurantData(reader);

        try {
            reader = new InputStreamReader(
                    context.openFileInput(Constants.INSPECTIONS_FILE_NAME),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            reader = new InputStreamReader(
                    context.getResources().openRawResource(R.raw.inspectionreports_itr1),
                    StandardCharsets.UTF_8);
        }
        readInspectionReportData(reader);

        if (Locale.getDefault().getLanguage().equalsIgnoreCase("fr")) {
            reader = new InputStreamReader(
                    context.getResources().openRawResource(R.raw.all_violations_fr),
                    StandardCharsets.UTF_8);
        } else {
            reader = new InputStreamReader(
                    context.getResources().openRawResource(R.raw.all_violations),
                    StandardCharsets.UTF_8);
        }

        readViolationData(new BufferedReader(reader));
    }

    private void loadFavouriteRestaurants(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String serializedRestaurants = preferences.getString(Constants.FAVOURITE_RESTAURANTS_KEY, "");

        String[] restaurantsData = serializedRestaurants.split(",");
        for(String restaurantData : restaurantsData) {
            if(restaurantData != null && !restaurantData.isEmpty()) {
                String trackingNum = restaurantData.split(":")[0];
                favRestaurantTrackingNums.add(trackingNum);
            }
        }
    }

    private void readRestaurantData(InputStreamReader reader) throws IOException {
        restaurants.clear();

        // CSVReader implementation from
        // https://stackoverflow.com/questions/43055661/reading-csv-file-in-android-app
        CSVReader csvReader = new CSVReader(reader);
        String[] line;
        csvReader.readNext();
        while ((line = csvReader.readNext()) != null) {
            restaurants.add(new Restaurant(line));
        }

        Collections.sort(restaurants);
    }

    private void readInspectionReportData(InputStreamReader reader) throws IOException {
        reports.clear();

        CSVReader csvReader = new CSVReader(reader);
        String[] line;
        csvReader.readNext();

        int key = 0;
        while ((line = csvReader.readNext()) != null) {
            if(line.length > 0 && !line[0].isEmpty()) {
                reports.add(new Report(key, line));
                key++;
            }
        }
    }

    private void readViolationData(BufferedReader reader) throws IOException {
        // Step over first 2 lines of .txt
        reader.readLine();
        reader.readLine();
        String line = reader.readLine();

        while (line != null) {
            String[] violationData = line.split(",");
            int violationID = Integer.parseInt(Utils.unquote(violationData[0]));
            violationsTable.put(violationID, new Violation(violationData));
            line = reader.readLine();
        }
    }

    public static RestaurantManager getInstance(Context context) {
        if (instance == null) {
            try {
                instance = new RestaurantManager(context);
            } catch (IOException e) {
                Toast.makeText(context, context.getString(R.string.data_read_error_msg), Toast.LENGTH_LONG).show();
            }
        }
        return instance;
    }

    public void filter(String name, String hazardLevel, Integer min, Integer max, boolean favouritesOnly) {
        boolean filterName = !name.isEmpty();
        boolean filterHazard = hazardLevel != null;
        boolean filterMin = min != null;
        boolean filterMax = max != null;

        name = name.toLowerCase();

        filteredRestaurants = new ArrayList<>();
        for(Restaurant restaurant : restaurants) {

            if(filterName && !restaurant.getName().toLowerCase().contains(name)) {
                continue;
            }

            if(filterHazard) {
                Report mostRecent = getMostRecentReport(restaurant.getTrackingNum());
                if(mostRecent == null || !mostRecent.getHazardRating().equalsIgnoreCase(hazardLevel)) {
                    continue;
                }
            }

            int criticalIssuesInLastYear = getNumCriticalIssuesInLastYear(restaurant.getTrackingNum());
            if(filterMin && criticalIssuesInLastYear < min) {
                continue;
            }
            if(filterMax && criticalIssuesInLastYear > max) {
                continue;
            }

            if(favouritesOnly && !restaurantIsFavourite(restaurant.getTrackingNum())) {
                continue;
            }

            filteredRestaurants.add(restaurant);
        }
    }

    public ArrayList<Report> getNewReportsForFavourites(Context context) {
        ArrayList<Report> newReports = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());

        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String serializedRestaurants = preferences.getString(Constants.FAVOURITE_RESTAURANTS_KEY, "");

        String[] restaurantsData = serializedRestaurants.split(",");
        for(String restaurantData : restaurantsData) {
            if(restaurantData != null && !restaurantData.isEmpty()) {

                String[] chunks = restaurantData.split(":");
                String trackingNum = chunks[0];
                Long lastMostRecentReportTime = Long.parseLong(chunks[1]);

                Report report = getMostRecentReport(trackingNum);
                if(report != null) {
                    try {
                        Long newMostRecentReportTime = format.parse(report.getDate()).getTime();
                        if(newMostRecentReportTime > lastMostRecentReportTime) {
                            newReports.add(report);
                        }
                    } catch (ParseException ignored) {}
                }
            }
        }

        saveFavourites(context);
        return newReports;
    }

    public void clearFilter() {
        filteredRestaurants = null;
    }

    public ArrayList<Restaurant> getRestaurants() {
        return restaurants;
    }

    public ArrayList<Restaurant> getFilteredRestaurants() {
        return filteredRestaurants == null ? restaurants : filteredRestaurants;
    }

    public ArrayList<Report> getReportsFor(String restaurantTrackingNum) {
        ArrayList<Report> restaurantReports = new ArrayList<>();
        for (Report report : reports) {
            if (report.getTrackingNum() != null) {
                if (report.getTrackingNum().equals(restaurantTrackingNum)) {
                    restaurantReports.add(report);
                }
            }
        }
        return restaurantReports;
    }

    public void addToFavourites(String restaurantTrackingNum) {
        favRestaurantTrackingNums.add(restaurantTrackingNum);
    }

    public void removeFromFavourites(String restaurantTrackingNum) {
        favRestaurantTrackingNums.remove(restaurantTrackingNum);
    }

    public boolean restaurantIsFavourite(String trackingNum) {
        return favRestaurantTrackingNums.contains(trackingNum);
    }

    public void saveFavourites(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());

        StringBuilder serializedRestaurants = new StringBuilder();
        for(String trackingNum : favRestaurantTrackingNums) {

            serializedRestaurants.append(trackingNum).append(":");
            Report report = getMostRecentReport(trackingNum);

            if(report == null) {
                serializedRestaurants.append(0);
            } else {
                try {
                    Date date = format.parse(report.getDate());
                    serializedRestaurants.append(date.getTime());
                } catch (ParseException e) {
                    serializedRestaurants.append(0);
                }
            }

            serializedRestaurants.append(",");
        }
        editor.putString(Constants.FAVOURITE_RESTAURANTS_KEY, serializedRestaurants.toString());
        editor.commit();
    }

    public Restaurant getRestaurant(String restaurantTrackingNum) {
        for (Restaurant restaurant : restaurants) {
            if (restaurant.getTrackingNum().equals(restaurantTrackingNum)) {
                return restaurant;
            }
        }
        return null;
    }

    public Report getMostRecentReport(String restaurantTrackingNum) {
        ArrayList<Report> reports = getReportsFor(restaurantTrackingNum);
        if (reports != null && !reports.isEmpty()) {

            Report mostRecentReport = reports.get(0);
            for (int i = 1; i < reports.size(); i++) {

                Report report = reports.get(i);
                if (report.isMoreRecentThan(mostRecentReport)) {
                    mostRecentReport = report;
                }
            }
            return mostRecentReport;
        }
        return null;
    }

    private int getNumCriticalIssuesInLastYear(String restaurantTrackingNum) {
        ArrayList<Report> reports = getReportsFor(restaurantTrackingNum);
        int numCriticalIssues = 0;
        for(Report report : reports) {

            long now = new Date().getTime();

            String reportStr = report.getDate();
            try {
                Date dateNum = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).parse(reportStr);
                long dateTime = Objects.requireNonNull(dateNum).getTime();

                // TimeUnit.YEARS is not an option
                long days = TimeUnit.DAYS.convert(now - dateTime, TimeUnit.MILLISECONDS);
                if(days <= 365) {
                    numCriticalIssues += report.getNumCriticalIssues();
                }

            } catch (ParseException ignored) {}
        }

        return numCriticalIssues;
    }

    public Report getReport(int uniqueKey) {
        for (Report report : reports) {
            if (report.getUniqueKey() == uniqueKey) {
                return report;
            }
        }
        return null;
    }

    public Violation getViolation(int violationID) {
        return violationsTable.get(violationID);
    }
}
