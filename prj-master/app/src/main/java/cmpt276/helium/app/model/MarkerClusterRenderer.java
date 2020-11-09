package cmpt276.helium.app.model;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/*
    Class to render all marker items individually or cluster them into groups, holds trackingNum
    as a field to select a particular restaurant if a user went to MapsActivity by tapping on the
    coordinates of a restaurant from RestaurantDetailsActivity, referenced from
    https://medium.com/@imstudio/android-google-maps-clustering-41f220f8f4d0
 */
public class MarkerClusterRenderer <T extends ClusterItem> extends DefaultClusterRenderer<MarkerClusterItem> {

    private String restaurantTrackingNumToSelect;
    public MarkerClusterRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager, String restaurantTrackingNumToSelect) {
        super(context, map, (ClusterManager<MarkerClusterItem>) clusterManager);
        this.restaurantTrackingNumToSelect = restaurantTrackingNumToSelect;
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<MarkerClusterItem> cluster) {
        // don't cluster if 5 or less restaurants nearby
        return cluster.getSize() > 6;
    }

    @Override
    protected void onBeforeClusterItemRendered(MarkerClusterItem item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.icon(item.getIcon());
    }

    @Override
    protected void onClusterItemRendered(MarkerClusterItem clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
        if(clusterItem.getRestaurantTrackingNum().equals(restaurantTrackingNumToSelect)) {
            getMarker(clusterItem).showInfoWindow();
        }
    }
}
