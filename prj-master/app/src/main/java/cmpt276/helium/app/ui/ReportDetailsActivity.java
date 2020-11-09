package cmpt276.helium.app.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import cmpt276.helium.app.Constants;
import cmpt276.helium.app.R;
import cmpt276.helium.app.Utils;
import cmpt276.helium.app.model.Report;
import cmpt276.helium.app.model.Restaurant;
import cmpt276.helium.app.model.RestaurantManager;
import cmpt276.helium.app.ui.recycleradapters.ViolationsRecyclerAdapter;

/*
    Activity that displays the details of a particular report and the list of violations
 */
public class ReportDetailsActivity extends AppCompatActivity {

    private int reportKey;
    private Restaurant restaurant;
    private Report report;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);
        setupToolbar();

        initReportDetails();
        initViews();
        initRecyclerView();
    }


    private void initReportDetails() {
        RestaurantManager restaurantManager = RestaurantManager.getInstance(this);

        // Get report's primary key that was passed in from Intent
        reportKey = getIntent().getIntExtra(Constants.UNIQUE_REPORT_INTENT_KEY, 0);
        report = restaurantManager.getReport(reportKey);
        restaurant = restaurantManager.getRestaurant(report.getTrackingNum());
    }


    private void initViews() {
        TextView reportDate = findViewById(R.id.reportDetailsDate);
        TextView reportRestaurantName = findViewById(R.id.reportDetailsRestaurant);
        TextView reportType = findViewById(R.id.reportDetailsType);
        TextView numCriticalIssues = findViewById(R.id.reportDetailsNumCritical);
        TextView numNonCriticalIssues = findViewById(R.id.reportDetailsNumNonCritical);
        ImageView hazardIcon = findViewById(R.id.reportDetailsHazardIcon);
        TextView hazardLevelText = findViewById(R.id.reportDetailsHazardLevel);

        reportDate.setText(getString(R.string.date_inspection_report, Utils.makeFullDate(report.getDate())));
        reportRestaurantName.setText(restaurant.getName());
        reportType.setText(report.getType());
        numCriticalIssues.setText(String.valueOf(report.getNumCriticalIssues()));
        numNonCriticalIssues.setText(String.valueOf(report.getNumNonCriticalIssues()));

        String hazardLevel = report.getHazardRating();
        String hazardAbbr = report.getHazardAbbr(this);

        // Based on the hazard level, get the appropriate image and
        // set the text view to the appropriate color
        int hazardIconResId = getResources().getIdentifier("hazard_" +
                hazardLevel.toLowerCase(), "drawable", getPackageName());
        hazardIcon.setImageDrawable(getDrawable(hazardIconResId));

        int hazardColor = getResources().getIdentifier("hazard_" +
                hazardLevel.toLowerCase(), "color", getPackageName());
        hazardLevelText.setText(getString(R.string.restaurant_list_hazard_level, hazardAbbr));
        hazardLevelText.setTextColor(getColor(hazardColor));
    }


    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.violationRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ViolationsRecyclerAdapter recyclerAdapter = new ViolationsRecyclerAdapter(this, reportKey);
        recyclerView.setAdapter(recyclerAdapter);
    }


    // Short snippet to add back button to actionBar and finish below in onOptionsItemSelected
    // From https://stackoverflow.com/questions/26651602/display-back-arrow-on-toolbar
    private void setupToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.report_details));
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }


    public static Intent makeIntent(Context context, int reportKey) {
        Intent intent = new Intent(context, ReportDetailsActivity.class);
        intent.putExtra(Constants.UNIQUE_REPORT_INTENT_KEY, reportKey);
        return intent;
    }
}
