package cmpt276.helium.app.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import cmpt276.helium.app.Constants;
import cmpt276.helium.app.R;
import cmpt276.helium.app.model.Report;
import cmpt276.helium.app.model.Restaurant;
import cmpt276.helium.app.model.RestaurantManager;
import cmpt276.helium.app.ui.recycleradapters.ReportsRecyclerAdapter;

/*
    Activity that displays the detail of a particular restaurant and the list of
    inspection reports for that restaurant
 */
public class RestaurantDetailsActivity extends AppCompatActivity {

    private String restaurantTrackingNum;
    private Restaurant restaurant;
    private ArrayList<Report> reports = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        setupToolbar();

        initRestaurantDetails();
        initViews();
        initRecyclerView();
        initMapButton();
    }


    private void initRestaurantDetails() {
        restaurantTrackingNum = getIntent().getStringExtra(Constants.RESTAURANT_TRACKINGNUM_INTENT_KEY);
        RestaurantManager restaurantManager = RestaurantManager.getInstance(this);
        restaurant = restaurantManager.getRestaurant(restaurantTrackingNum);
        reports = restaurantManager.getReportsFor(restaurantTrackingNum);
    }


    private void initViews() {

        TextView name = findViewById(R.id.restaurantDetailsName);
        TextView address = findViewById(R.id.restaurantDetailsAddress);
        TextView coords = findViewById(R.id.restaurantDetailsCoords);

        name.setText(restaurant.getName());
        address.setText(restaurant.getAddress());
        coords.setText(restaurant.getCoords());

        coords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MapsActivity.makeIntentSelectRestaurant(
                        RestaurantDetailsActivity.this, restaurantTrackingNum));
            }
        });

        // Display alternative "no reports" text if there aren't any
        if (reports.isEmpty()) {
            TextView noReportsText = findViewById(R.id.noReportsText);
            noReportsText.setVisibility(View.VISIBLE);
        }
    }


    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.reportRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ReportsRecyclerAdapter recyclerAdapter = new ReportsRecyclerAdapter(this, restaurantTrackingNum);
        recyclerView.setAdapter(recyclerAdapter);
    }


    private void initMapButton() {
        ImageButton mapButton = findViewById(R.id.openMaps);
        mapButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                // Start google maps
                // From https://developers.google.com/maps/documentation/urls/android-intents
                Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" +
                        restaurant.getAddress() + " " + restaurant.getName());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    }


    // Short snippet to add back button to actionBar and finish below in onOptionsItemSelected
    // From https://stackoverflow.com/questions/26651602/display-back-arrow-on-toolbar
    private void setupToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.restaurant_details));
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }


    public static Intent makeIntent(Context context, String trackingNum) {
        Intent intent = new Intent(context, RestaurantDetailsActivity.class);
        intent.putExtra(Constants.RESTAURANT_TRACKINGNUM_INTENT_KEY, trackingNum);
        return intent;
    }
}
