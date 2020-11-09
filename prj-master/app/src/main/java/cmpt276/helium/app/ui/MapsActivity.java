package cmpt276.helium.app.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.android.clustering.ClusterManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import cmpt276.helium.app.Constants;
import cmpt276.helium.app.R;
import cmpt276.helium.app.Utils;
import cmpt276.helium.app.model.MarkerClusterItem;
import cmpt276.helium.app.model.MarkerClusterRenderer;
import cmpt276.helium.app.model.Report;
import cmpt276.helium.app.model.Restaurant;
import cmpt276.helium.app.model.RestaurantManager;
import cmpt276.helium.app.model.SearchDialogBuilder;

/*
    Activity that allows for user to update restaurant data and displays a map view of all the
    restaurants with markers, showing restaurant details and allowing user to tap on them

    Some map code referenced from https://medium.com/@imstudio/android-google-maps-clustering-41f220f8f4d0
    and provided Youtube playlist https://www.youtube.com/playlist?list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private RestaurantManager restaurantManager;
    private ProgressDialog progressDialog;

    private boolean shouldUpdateRestaurantData = false;
    private boolean shouldUpdateInspectionData = false;

    // string representations of updated data .csv-- stored as a field so that we can
    // save to file after an asynchronous operation and guarantee atomicity if user
    // cancels during the download
    private String restaurantUpdateData = null;
    private String inspectionUpdateData = null;

    private Boolean mLocationPermissionsGranted = false;
    private ClusterManager<MarkerClusterItem> clusterManager;

    private String restaurantTrackingNumToSelect = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initToListButton();
        initSearchButton();
        getSelectedRestaurantIfExists();

        boolean connected = checkConnected(this);
        if (!connected) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_LONG).show();
            getLocationPermission();
            return;
        }

        // if savedInstanceState is not null, that means the activity was destroyed and re-created
        // with a screen orientation change, and we've set up an Intent key to recognize when we're
        // returning to the MapsActivity from another Activity
        // i.e. the update checking code below this conditional only runs on app startup
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null || (extras != null && extras.containsKey(Constants.RETURNING_TO_MAPS_KEY))) {
            getLocationPermission();
            return;
        }

        if (needToCheckForUpdates()) {
            new UpdateChecker().execute(this);
        } else {
            getLocationPermission();
        }
    }


    private void initToListButton() {
        FloatingActionButton toListButton = findViewById(R.id.toListButton);
        toListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(RestaurantListActivity.makeIntent(MapsActivity.this));
            }
        });
    }


    private void initSearchButton() {
        FloatingActionButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SearchDialogBuilder searchDialog = new SearchDialogBuilder(MapsActivity.this);
                final AlertDialog show = searchDialog.show();
                searchDialog.setClearButtonListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        restaurantManager.clearFilter();
                        setUpClusterManager();
                        show.dismiss();
                    }
                });
                searchDialog.setFilterButtonListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = searchDialog.getRestaurantName();
                        String hazardLevel = searchDialog.getSelectedHazardLevel();
                        Integer min = searchDialog.getMinViolations();
                        Integer max = searchDialog.getMaxViolations();
                        boolean favourites = searchDialog.shouldOnlyShowFavourites();
                        restaurantManager.filter(name, hazardLevel, min, max, favourites);
                        setUpClusterManager();
                        show.dismiss();
                    }
                });
            }
        });
    }


    // Get restaurant tracking num if we came from RestaurantDetailsActivity where user tapped on coords
    private void getSelectedRestaurantIfExists() {
        restaurantTrackingNumToSelect = getIntent().getStringExtra(Constants.MAPS_RESTAURANT_SELECT_KEY);
    }


    private boolean needToCheckForUpdates() {
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS_KEY, MODE_PRIVATE);

        // dates are stored as numbers of milliseconds since Unix Epoch
        long lastRestaurantsUpdateTime = prefs.getLong(Constants.RESTAURANTS_LAST_UPDATED_KEY, 0);
        long lastInspectionsUpdateTime = prefs.getLong(Constants.INSPECTIONS_LAST_UPDATED_KEY, 0);

        // if values are the default value, we didn't have a key, i.e. we have never updated that data
        if (lastRestaurantsUpdateTime == 0 || lastInspectionsUpdateTime == 0) {
            return true;
        }

        long currentTime = new Date().getTime();
        long restaurantsDiff = currentTime - lastRestaurantsUpdateTime;
        long inspectionsDiff = currentTime - lastInspectionsUpdateTime;
        long hoursSinceRestaurantsUpdate = TimeUnit.HOURS.convert(restaurantsDiff, TimeUnit.MILLISECONDS);
        long hoursSinceInspectionsUpdate = TimeUnit.HOURS.convert(inspectionsDiff, TimeUnit.MILLISECONDS);

        return hoursSinceRestaurantsUpdate > 20 || hoursSinceInspectionsUpdate > 20;
    }


    // MapsActivity passed in since class is static to prevent memory leaks
    private static class UpdateChecker extends AsyncTask<MapsActivity, Void, Pair<Boolean, MapsActivity>> {

        @Override
        protected Pair<Boolean, MapsActivity> doInBackground(MapsActivity... activities) {
            MapsActivity mapsActivity = activities[0];
            SharedPreferences prefs = mapsActivity.getSharedPreferences(Constants.SHARED_PREFS_KEY, MODE_PRIVATE);

            long lastRestaurantsUpdateTime = prefs.getLong(Constants.RESTAURANTS_LAST_UPDATED_KEY, 0);
            long lastInspectionsUpdateTime = prefs.getLong(Constants.INSPECTIONS_LAST_UPDATED_KEY, 0);

            boolean restaurantsNeedUpdating = needsUpdating(Constants.RESTAURANT_DATA_URL, lastRestaurantsUpdateTime);
            boolean inspectionsNeedUpdating = needsUpdating(Constants.INSPECTION_DATA_URL, lastInspectionsUpdateTime);

            mapsActivity.setUpdateRestaurantData(restaurantsNeedUpdating);
            mapsActivity.setUpdateInspectionData(inspectionsNeedUpdating);

            return new Pair<>(restaurantsNeedUpdating || inspectionsNeedUpdating, mapsActivity);
        }

        private boolean needsUpdating(String url, long lastUpdateTime) {
            if (lastUpdateTime == 0) {
                return true;
            }

            try {
                // using GSON to read JSON Data from a webpage, referenced from
                // https://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java
                URLConnection connection = new URL(url).openConnection();
                connection.connect();

                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(new InputStreamReader((InputStream) connection.getContent()));
                JsonObject result = element.getAsJsonObject().get("result").getAsJsonObject();
                JsonArray resArray = result.getAsJsonObject().get("resources").getAsJsonArray();
                String date = resArray.get(0).getAsJsonObject().get("last_modified").toString();

                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_SERVER, Locale.getDefault());
                long time = dateFormat.parse(Utils.unquote(date)).getTime();

                return time > lastUpdateTime;

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Pair<Boolean, MapsActivity> booleanMapsActivityPair) {
            boolean updateAvailable = booleanMapsActivityPair.first;
            MapsActivity activity = booleanMapsActivityPair.second;
            if(updateAvailable) {
                activity.showUpdateDialog();
            } else {
                activity.getLocationPermission();
            }
        }
    }


    private void showUpdateDialog() {

        // setting up progressDialog here so we can have it call getLocationPermission() inside
        // the ASyncTask even though it won't have reference to this activity
        progressDialog = new ProgressDialog(this);
        final DataUpdater dataUpdater = new DataUpdater(progressDialog);

        progressDialog.setTitle(getString(R.string.processing));
        progressDialog.setMessage(getString(R.string.downloading_update));
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dataUpdater.cancel(true);
                        getLocationPermission();
                        progressDialog.dismiss();
                    }
                });

        AlertDialog.Builder updateDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.new_data_found))
                .setMessage(getString(R.string.would_you_like_to_download_update))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.download), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DataUpdater(progressDialog).execute(MapsActivity.this);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getLocationPermission();
                    }
                });

        updateDialog.show();
    }


    private static class DataUpdater extends AsyncTask<MapsActivity, String, MapsActivity> {

        // override constructor to hold progressDialog as a field so we can modify it in the
        // methods that don't have a reference to the Activity (such as onPreExecute)
        private ProgressDialog progressDialog;

        public DataUpdater(ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected MapsActivity doInBackground(MapsActivity... activities) {
            MapsActivity activity = activities[0];

            // getUpdateData returns the new CSV file from the server as a String and sets it to the
            // relevant field in MapsActivity to ensure the saving of the file is not cancellable
            if (activity.shouldUpdateRestaurantData()) {
                String restaurantData = getUpdateData(Constants.RESTAURANT_DATA_URL);
                activity.setRestaurantUpdateData(restaurantData);
            }
            if (activity.shouldUpdateInspectionData()) {
                String inspectionData = getUpdateData(Constants.INSPECTION_DATA_URL);
                activity.setInspectionUpdateData(inspectionData);
            }

            return activity;
        }

        private String getUpdateData(String url) {
            try {
                URLConnection connection = new URL(url).openConnection();
                connection.connect();

                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(new InputStreamReader((InputStream) connection.getContent()));
                JsonObject result = element.getAsJsonObject().get("result").getAsJsonObject();
                JsonArray resArray = result.getAsJsonObject().get("resources").getAsJsonArray();

                String dataUrl = resArray.get(0).getAsJsonObject().get("url").toString();
                dataUrl = Utils.unquote(dataUrl);

                return downloadData(dataUrl);

            } catch (IOException e) {
                return null;
            }
        }

        private String downloadData(String dataUrl) {

            // Later versions of Android complain about cleartext HTTP traffic
            dataUrl = dataUrl.replace("http://", "https://");

            InputStream inputStream;
            try {
                inputStream = new URL(dataUrl).openStream();
                Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                return s.next();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MapsActivity mapsActivity) {
            progressDialog.dismiss();
            mapsActivity.writeUpdatedDataToFile();
            mapsActivity.getLocationPermission();
        }
    }


    private void writeUpdatedDataToFile() {
        if (restaurantUpdateData != null) {
            writeToFile(Constants.RESTAURANTS_FILE_NAME, restaurantUpdateData, this);
            restaurantUpdateData = null;
            updateDateKey(Constants.RESTAURANTS_LAST_UPDATED_KEY);
        }
        if (inspectionUpdateData != null) {
            writeToFile(Constants.INSPECTIONS_FILE_NAME, inspectionUpdateData, this);
            inspectionUpdateData = null;
            updateDateKey(Constants.INSPECTIONS_LAST_UPDATED_KEY);
        }
    }


    private void updateDateKey(String key) {
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS_KEY, MODE_PRIVATE);
        long currentTime = new Date().getTime();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, currentTime);
        editor.apply();
    }


    private void initDataAndMaps() {
        restaurantManager = RestaurantManager.getInstance(this);
        ArrayList<Report> newReports = restaurantManager.getNewReportsForFavourites(this);
        if(!newReports.isEmpty()) {
            showNewFavouritesReportsDialog(newReports);
        }

        // obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private void showNewFavouritesReportsDialog(ArrayList<Report> newReports) {
        AlertDialog.Builder reportsDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.new_favourite_reports_dialog, null);
        LinearLayout reportList = dialogView.findViewById(R.id.favouriteReportsList);

        for(Report report : newReports) {
            View reportView = inflater.inflate(R.layout.new_favourite_report_item, null);

            TextView restaurantName = reportView.findViewById(R.id.favouriteRestaurantName);
            TextView reportDate = reportView.findViewById(R.id.favouriteRestaurantReportDate);
            TextView hazardLevel = reportView.findViewById(R.id.favouriteRestaurantHazardLevel);
            ImageView hazardIcon = reportView.findViewById(R.id.favouriteRestaurantHazardIcon);

            restaurantName.setText(restaurantManager.getRestaurant(report.getTrackingNum()).getName());
            reportDate.setText(Utils.userFriendlyDate(report.getDate(), this));

            int hazardIconResId = getResources().getIdentifier("hazard_" +
                    report.getHazardRating().toLowerCase(), "drawable", getPackageName());
            hazardIcon.setImageDrawable(getDrawable(hazardIconResId));

            int hazardColor = getResources().getIdentifier("hazard_" +
                    report.getHazardRating().toLowerCase(), "color", getPackageName());

            hazardLevel.setText(getString(R.string.restaurant_list_hazard_level, report.getHazardAbbr(this)));
            hazardLevel.setTextColor(getColor(hazardColor));

            reportList.addView(reportView);
        }

        reportsDialog.setView(dialogView);
        reportsDialog.show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }


    private void getDeviceLocation() {
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (mLocationPermissionsGranted) {
            final Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()) {
                        Location currentLocation = (Location) task.getResult();
                        moveCamera(currentLocation.getLatitude(), currentLocation.getLongitude(), Constants.DEFAULT_ZOOM);

                        clusterManager = new ClusterManager<>(getApplicationContext(), mMap);
                        setUpClusterManager();

                        if (restaurantTrackingNumToSelect != null) {
                            Restaurant restaurant = restaurantManager.getRestaurant(restaurantTrackingNumToSelect);
                            moveCamera(restaurant.getLatitude(), restaurant.getLongitude(), Constants.SELECTED_ZOOM);
                        }
                    }
                }
            });
        }
    }


    public void addClusterItems() {
        for (Restaurant restaurant : restaurantManager.getFilteredRestaurants()) {
            String trackingNum = restaurant.getTrackingNum();
            Report report = restaurantManager.getMostRecentReport(trackingNum);
            if (report == null) {
                continue;
            }

            LatLng position = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
            String snippet = getString(R.string.map_snippet, restaurant.getAddress(), report.getHazardAbbr(this));

            int hazardIconResId = getResources().getIdentifier("hazard_" +
                    report.getHazardRating().toLowerCase() + "_small", "drawable", getPackageName());
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(hazardIconResId);

            MarkerClusterItem item = new MarkerClusterItem(position, restaurant.getName(), snippet, trackingNum, icon);
            clusterManager.addItem(item);
        }
    }


    public void setRenderer() {
        MarkerClusterRenderer<MarkerClusterItem> clusterRenderer =
                new MarkerClusterRenderer<>(this, mMap, clusterManager, restaurantTrackingNumToSelect);
        clusterManager.setRenderer(clusterRenderer);
    }


    private void setUpClusterManager() {
        clusterManager.clearItems();
        addClusterItems();
        setRenderer();
        clusterManager.cluster();
        mMap.setOnCameraIdleListener(clusterManager);
        clusterManager.setOnClusterItemInfoWindowClickListener(
                new ClusterManager.OnClusterItemInfoWindowClickListener<MarkerClusterItem>() {
                    @Override
                    public void onClusterItemInfoWindowClick(MarkerClusterItem item) {
                        startActivity(RestaurantDetailsActivity.makeIntent(
                                MapsActivity.this, item.getRestaurantTrackingNum()));
                    }
                });
    }


    private void moveCamera(double latitude, double longitude, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }


    private void getLocationPermission() {

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionsGranted = true;
            initDataAndMaps();
        } else {
            ActivityCompat.requestPermissions(this, permissions, Constants.LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;
        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionsGranted = false;
                        return;
                    }
                }
                mLocationPermissionsGranted = true;
                initDataAndMaps();
            }
        }
    }


    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.RETURNING_TO_MAPS_KEY, 0);
        return intent;
    }


    public static Intent makeIntentSelectRestaurant(Context context, String trackingNum) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.RETURNING_TO_MAPS_KEY, 0);
        intent.putExtra(Constants.MAPS_RESTAURANT_SELECT_KEY, trackingNum);
        return intent;
    }


    // check if the user's phone is connected to the Internet
    // referenced from: https://stackoverflow.com/questions/9570237/android-check-internet-connection
    private boolean checkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    private void writeToFile(String fileName, String data, Context context) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(data.getBytes());
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_saving_data), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public boolean shouldUpdateRestaurantData() {
        return shouldUpdateRestaurantData;
    }

    public void setUpdateRestaurantData(boolean shouldUpdateRestaurantData) {
        this.shouldUpdateRestaurantData = shouldUpdateRestaurantData;
    }

    public boolean shouldUpdateInspectionData() {
        return shouldUpdateInspectionData;
    }

    public void setUpdateInspectionData(boolean shouldUpdateInspectionData) {
        this.shouldUpdateInspectionData = shouldUpdateInspectionData;
    }

    public void setRestaurantUpdateData(String restaurantUpdateData) {
        this.restaurantUpdateData = restaurantUpdateData;
    }

    public void setInspectionUpdateData(String inspectionUpdateData) {
        this.inspectionUpdateData = inspectionUpdateData;
    }
}
