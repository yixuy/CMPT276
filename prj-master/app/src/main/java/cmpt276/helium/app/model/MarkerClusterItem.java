package cmpt276.helium.app.model;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/*
    Model class for a single marker item to be used with the clusterManager to be clustered,
    referenced from https://medium.com/@imstudio/android-google-maps-clustering-41f220f8f4d0
 */
public class MarkerClusterItem implements ClusterItem {

    private LatLng latLng;
    private String title;
    private String snippet;
    private String restaurantTrackingNum;
    private BitmapDescriptor icon;

    public MarkerClusterItem(LatLng latLng, String title, String snippet, String restaurantTrackingNum, BitmapDescriptor icon) {
        this.latLng = latLng;
        this.title = title;
        this.snippet = snippet;
        this.restaurantTrackingNum = restaurantTrackingNum;
        this.icon = icon;
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public String getRestaurantTrackingNum() {
        return restaurantTrackingNum;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }
}
