package cmpt276.helium.app.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import cmpt276.helium.app.R;
import cmpt276.helium.app.model.RestaurantManager;
import cmpt276.helium.app.model.SearchDialogBuilder;
import cmpt276.helium.app.ui.recycleradapters.RestaurantsRecyclerAdapter;

/*
    Main Activity that displays the list of restaurants in a RecyclerView (most of the
    relevant code is within the RestaurantsRecyclerAdapter)
 */
public class RestaurantListActivity extends AppCompatActivity {

    private RestaurantsRecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        initToMapsButton();
        initSearchButton();
        initRecyclerView();
    }

    private void initToMapsButton() {
        FloatingActionButton toListButton = findViewById(R.id.toMapsButton);
        toListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MapsActivity.makeIntent(RestaurantListActivity.this));
            }
        });
    }

    private void initSearchButton() {
        FloatingActionButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SearchDialogBuilder searchDialog = new SearchDialogBuilder(RestaurantListActivity.this);
                final AlertDialog show = searchDialog.show();
                searchDialog.setClearButtonListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RestaurantManager restaurantManager = RestaurantManager.getInstance(RestaurantListActivity.this);
                        restaurantManager.clearFilter();
                        recyclerAdapter.notifyDataSetChanged();
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
                        RestaurantManager restaurantManager = RestaurantManager.getInstance(RestaurantListActivity.this);
                        restaurantManager.filter(name, hazardLevel, min, max, favourites);
                        recyclerAdapter.notifyDataSetChanged();
                        show.dismiss();
                    }
                });
            }
        });
    }


    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.restaurantRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter = new RestaurantsRecyclerAdapter(this);
        recyclerView.setAdapter(recyclerAdapter);
    }


    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, RestaurantListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


    public void refreshRecyclerView() {
        recyclerAdapter.notifyDataSetChanged();
    }
}
