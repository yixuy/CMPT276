package cmpt276.helium.app.ui.recycleradapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import cmpt276.helium.app.Constants;
import cmpt276.helium.app.R;
import cmpt276.helium.app.model.RestaurantManager;
import cmpt276.helium.app.model.Violation;

/*
    Adapter class for RecyclerView to populate with a list of Violations

    Referenced how to set up RecyclerViews from my (Navpreet's) old project in a previous course here
    https://github.com/nmatharu/Campus-Cannons
 */
public class ViolationsRecyclerAdapter extends RecyclerView.Adapter<ViolationsRecyclerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Integer> violationsIDs;
    private RestaurantManager restaurantManager;


    // Get the list of violations of a particular report given its primary key
    public ViolationsRecyclerAdapter(Context context, int reportKey) {
        this.context = context;
        restaurantManager = RestaurantManager.getInstance(context);
        this.violationsIDs = restaurantManager.getReport(reportKey).getViolationsIDs();
    }


    @NonNull
    @Override
    public ViolationsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.violations_recycler_item, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull ViolationsRecyclerAdapter.ViewHolder holder, int position) {

        int violationID = violationsIDs.get(position);
        Violation violation = restaurantManager.getViolation(violationID);

        // Init views
        TextView type = holder.layout.findViewById(R.id.violationType);
        TextView description = holder.layout.findViewById(R.id.violationDescription);
        ImageView icon = holder.layout.findViewById(R.id.violationIcon);
        ImageView typeIcon = holder.layout.findViewById(R.id.violationTypeIcon);
        final TextView descriptionFull = holder.layout.findViewById(R.id.violationDescriptionFull);

        if (violation.isCritical()) {
            type.setText(context.getString(R.string.critical));
            type.setTextColor(context.getColor(R.color.hazard_high));
            typeIcon.setImageDrawable(context.getDrawable(R.drawable.violation_critical));
        } else {
            type.setText(context.getString(R.string.not_critical));
            type.setTextColor(context.getColor(R.color.hazard_low));
            typeIcon.setImageDrawable(context.getDrawable(R.drawable.violation_not_critical));
        }

        // Utility function call to get the appropriate icon for this violation ID
        icon.setImageDrawable(context.getDrawable(Objects.requireNonNull(
                Constants.VIOLATION_ID_ICONS.get(violationID))));

        // Get the short version of the violation description via strings.xml from its ID
        int descriptionID = context.getResources().getIdentifier(
                "short_desc_" + violationID, "string", context.getPackageName());
        description.setText(context.getString(descriptionID));

        descriptionFull.setText(violation.getDescription());

        // Pressing on a violation will toggle showing the full description
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (descriptionFull.getVisibility() == View.GONE) {
                    descriptionFull.setVisibility(View.VISIBLE);
                } else {
                    descriptionFull.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return violationsIDs.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = (LinearLayout) itemView;
        }
    }
}
