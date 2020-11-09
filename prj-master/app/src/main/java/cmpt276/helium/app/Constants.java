package cmpt276.helium.app;

import java.util.HashMap;

/*
    Constants class that provides keys for passing data between Intents, date formatting, and a
    constant hash table to quickly look up the icon associated with a particular violation by its ID
 */
public class Constants {

    public static final String RESTAURANT_TRACKINGNUM_INTENT_KEY = "RestaurantTrackingNumIntentKey";
    public static final String UNIQUE_REPORT_INTENT_KEY = "ReportListPositionIntentKey";
    public static final String RETURNING_TO_MAPS_KEY = "ReturningToMapsActivityKey";
    public static final String MAPS_RESTAURANT_SELECT_KEY = "MapsRestaurantSelectKey";

    public static final String DATE_FORMAT = "yyyyMMdd";
    public static final String DATE_FORMAT_MONTH_DAY = "MMM dd";
    public static final String DATE_FORMAT_MONTH_YEAR = "MMM yyyy";
    public static final String DATE_FORMAT_FULL = "MMM dd, yyyy";
    public static final String DATE_FORMAT_SERVER = "yyyy-MM-dd'T'hh:mm:ss.SSSSSS";

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public static final float DEFAULT_ZOOM = 10f;
    public static final float SELECTED_ZOOM = 16f;

    public static final String RESTAURANTS_FILE_NAME = "restaurants.txt";
    public static final String INSPECTIONS_FILE_NAME = "inspections.txt";
    public static final String RESTAURANT_DATA_URL =
            "https://data.surrey.ca/api/3/action/package_show?id=restaurants";
    public static final String INSPECTION_DATA_URL =
            "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";

    public static final String SHARED_PREFS_KEY = "HealthInspectionReportsSharedPrefsKey";
    public static final String RESTAURANTS_LAST_UPDATED_KEY = "RestaurantsLastUpdatedKey";
    public static final String INSPECTIONS_LAST_UPDATED_KEY = "InspectionsLastUpdatedKey";
    public static final String FAVOURITE_RESTAURANTS_KEY = "FavouriteRestaurantsKey";

    // Initializing a HashMap as a constant, referenced from
    // https://alvinalexander.com/java/how-to-populate-predefined-static-data-map-hashmap-in-java
    public static final HashMap<Integer, Integer> VIOLATION_ID_ICONS = new HashMap<Integer, Integer>() {{
        put(101, R.drawable.violation_rules);
        put(102, R.drawable.violation_rules);
        put(103, R.drawable.violation_rules);
        put(104, R.drawable.violation_rules);
        put(201, R.drawable.violation_contamination);
        put(202, R.drawable.violation_contamination);
        put(203, R.drawable.violation_contamination);
        put(204, R.drawable.violation_contamination);
        put(205, R.drawable.violation_contamination);
        put(206, R.drawable.violation_contamination);
        put(208, R.drawable.violation_rules);
        put(209, R.drawable.violation_contamination);
        put(210, R.drawable.violation_contamination);
        put(211, R.drawable.violation_contamination);
        put(212, R.drawable.violation_procedures);
        put(301, R.drawable.violation_cutlery);
        put(302, R.drawable.violation_cutlery);
        put(303, R.drawable.violation_washing);
        put(304, R.drawable.violation_pests);
        put(305, R.drawable.violation_pests);
        put(306, R.drawable.violation_hygiene);
        put(307, R.drawable.violation_cutlery);
        put(308, R.drawable.violation_cutlery);
        put(309, R.drawable.violation_chemicals);
        put(310, R.drawable.violation_plastic);
        put(311, R.drawable.violation_rules);
        put(312, R.drawable.violation_rules);
        put(313, R.drawable.violation_animal);
        put(314, R.drawable.violation_hygiene);
        put(315, R.drawable.violation_thermometer);
        put(401, R.drawable.violation_washing);
        put(402, R.drawable.violation_washing);
        put(403, R.drawable.violation_hygiene);
        put(404, R.drawable.violation_smoking);
        put(501, R.drawable.violation_rules);
        put(502, R.drawable.violation_rules);
    }};
}
