package cmpt276.helium.app.model;

import androidx.annotation.NonNull;

/*
    Model class that holds the data associated with a single restaurant, but not any reports of
    that restaurants-- that's in another ArrayList that we'll look up the trackingNum in
    Implements the Comparable interface to sort by name
 */
public class Restaurant implements Comparable<Restaurant> {

    private String trackingNum;
    private String name;
    private String address;
    private String city;
    private String type;
    private double latitude;
    private double longitude;

    // Constructor that parses a .csv line split by ","
    public Restaurant(String[] restaurantData) {
        trackingNum = restaurantData[0];
        name = restaurantData[1];
        address = restaurantData[2];
        city = restaurantData[3];
        type = restaurantData[4];
        latitude = Double.parseDouble(restaurantData[5]);
        longitude = Double.parseDouble(restaurantData[6]);
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getTrackingNum() {
        return trackingNum;
    }

    public String getCoords() {
        return "(" + latitude + ", " + longitude + ")";
    }
    
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    
    @Override
    public int compareTo(Restaurant other) {
        return this.getName().compareTo(other.getName());
    }

    @NonNull
    @Override
    public String toString() {
        return name + '\t' +
                trackingNum + '\t' +
                address + '\t' +
                city + '\t' +
                type + '\t' +
                latitude + '\t' +
                longitude + '\t';
    }
}
