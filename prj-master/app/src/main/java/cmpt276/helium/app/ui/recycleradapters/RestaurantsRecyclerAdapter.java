package cmpt276.helium.app.ui.recycleradapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import cmpt276.helium.app.R;
import cmpt276.helium.app.Utils;
import cmpt276.helium.app.model.Report;
import cmpt276.helium.app.model.Restaurant;
import cmpt276.helium.app.model.RestaurantManager;
import cmpt276.helium.app.ui.RestaurantDetailsActivity;
import cmpt276.helium.app.ui.RestaurantListActivity;

/*
    Adapter class for RecyclerView to populate with a list of Restaurants

    Referenced how to set up RecyclerViews from my (Navpreet's) old project in a previous course here
    https://github.com/nmatharu/Campus-Cannons
 */
public class RestaurantsRecyclerAdapter extends RecyclerView.Adapter<RestaurantsRecyclerAdapter.ViewHolder> {

    private Context context;
    private RestaurantManager restaurantManager;


    public RestaurantsRecyclerAdapter(Context context) {
        this.context = context;
        restaurantManager = RestaurantManager.getInstance(context);
    }


    @NonNull
    @Override
    public RestaurantsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.restaurant_recycler_item, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull final RestaurantsRecyclerAdapter.ViewHolder holder, int position) {

        Restaurant restaurant = restaurantManager.getFilteredRestaurants().get(position);
        final String trackingNum = restaurant.getTrackingNum();

        // Init views
        TextView name = holder.layout.findViewById(R.id.restaurantName);
        TextView address = holder.layout.findViewById(R.id.restaurantAddress);
        ImageView restaurantIcon = holder.layout.findViewById(R.id.restaurantIcon);
        ImageView hazardIcon = holder.layout.findViewById(R.id.restaurantHazardIcon);
        final ImageButton favouriteButton = holder.layout.findViewById(R.id.favouriteButton);

        // Call utility function which will guess what the most appropriate icon is from the name
        restaurantIcon.setImageDrawable(context.getDrawable(Utils.getRestaurantIcon(restaurant.getName())));

        // We will be hiding one of these and showing the other (showing most recent report data
        // or the bar that has a message that says "No inspection reports.")
        ConstraintLayout reportBar = holder.layout.findViewById(R.id.restaurantReportBar);
        LinearLayout noReportBar = holder.layout.findViewById(R.id.restaurantNoReportBar);

        // mostRecentReport may be null if the restaurant has no reports
        Report mostRecentReport = restaurantManager.getMostRecentReport(trackingNum);
        if (mostRecentReport != null) {

            reportBar.setVisibility(View.VISIBLE);
            noReportBar.setVisibility(View.GONE);

            String hazardLevel = mostRecentReport.getHazardRating();
            String hazardAbbr = mostRecentReport.getHazardAbbr(context);

            // Based on the hazard level, get the appropriate image and
            // set the text view to the appropriate color
            int hazardIconResId = context.getResources().getIdentifier("hazard_" +
                    hazardLevel.toLowerCase(), "drawable", context.getPackageName());
            hazardIcon.setImageDrawable(context.getDrawable(hazardIconResId));

            int hazardColor = context.getResources().getIdentifier("hazard_" +
                    hazardLevel.toLowerCase(), "color", context.getPackageName());
            TextView hazardLevelText = holder.layout.findViewById(R.id.restaurantHazardLevel);
            hazardLevelText.setText(context.getString(R.string.restaurant_list_hazard_level, hazardAbbr));
            hazardLevelText.setTextColor(context.getColor(hazardColor));

            TextView lastReportDate = holder.layout.findViewById(R.id.restaurantReportDate);
            TextView issuesFound = holder.layout.findViewById(R.id.restaurantIssuesFound);

            lastReportDate.setText(Utils.userFriendlyDate(mostRecentReport.getDate(), context));
            issuesFound.setText(String.valueOf(mostRecentReport.getNumIssues()));

        } else {
            reportBar.setVisibility(View.GONE);
            noReportBar.setVisibility(View.VISIBLE);
        }

        name.setText(restaurant.getName());
        address.setText(restaurant.getAddress());

        if(restaurantManager.restaurantIsFavourite(trackingNum)) {
            holder.layout.setBackground(context.getDrawable(R.drawable.rounded_favourite_bg));
            favouriteButton.setImageDrawable(context.getDrawable(R.drawable.favourite));
        } else {
            holder.layout.setBackground(context.getDrawable(R.drawable.rounded_white_bg));
            favouriteButton.setImageDrawable(context.getDrawable(R.drawable.not_favourite));
        }

        // Set up favourite button
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(restaurantManager.restaurantIsFavourite(trackingNum)) {
                    restaurantManager.removeFromFavourites(trackingNum);
                } else {
                    restaurantManager.addToFavourites(trackingNum);
                }
                restaurantManager.saveFavourites(context);
                if(context instanceof RestaurantListActivity) {
                    ((RestaurantListActivity) context).refreshRecyclerView();
                }
            }
        });

        // When a Restaurant is clicked on, go to restaurant details activity, passing in trackingNum
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(RestaurantDetailsActivity.makeIntent(context, trackingNum));
            }
        });
    }


    @Override
    public int getItemCount() {
        return restaurantManager.getFilteredRestaurants().size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = (LinearLayout) itemView;
        }
    }
}
