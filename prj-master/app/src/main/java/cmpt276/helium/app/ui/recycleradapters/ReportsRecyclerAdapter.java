package cmpt276.helium.app.ui.recycleradapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import cmpt276.helium.app.R;
import cmpt276.helium.app.Utils;
import cmpt276.helium.app.model.Report;
import cmpt276.helium.app.model.RestaurantManager;
import cmpt276.helium.app.ui.ReportDetailsActivity;

/*
    Adapter class for RecyclerView to populate with a list of Reports

    Referenced how to set up RecyclerViews from my (Navpreet's) old project in a previous course here
    https://github.com/nmatharu/Campus-Cannons
 */
public class ReportsRecyclerAdapter extends RecyclerView.Adapter<ReportsRecyclerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Report> reports;


    // Given the particular Restaurant, get its list to reports to use for the RecyclerView
    public ReportsRecyclerAdapter(Context context, String restaurantTrackingNum) {
        this.context = context;
        RestaurantManager restaurantManager = RestaurantManager.getInstance(context);
        reports = restaurantManager.getReportsFor(restaurantTrackingNum);
        Collections.sort(reports);
    }


    @NonNull
    @Override
    public ReportsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.reports_recycler_item, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull ReportsRecyclerAdapter.ViewHolder holder, int position) {

        final Report report = reports.get(position);

        // Init views
        ImageView hazardIcon = holder.layout.findViewById(R.id.reportListHazardIcon);
        TextView hazardLevelText = holder.layout.findViewById(R.id.reportListHazardLevel);
        TextView lastReportDate = holder.layout.findViewById(R.id.reportListDate);
        TextView numCriticalIssues = holder.layout.findViewById(R.id.reportListNumCritical);
        TextView numNonCriticalIssues = holder.layout.findViewById(R.id.reportListNumNonCritical);

        String hazardLevel = report.getHazardRating();
        String hazardAbbr = report.getHazardAbbr(context);

        // Based on the hazard level, get the appropriate image and
        // set the text view to the appropriate color
        int hazardIconResId = context.getResources().getIdentifier("hazard_" +
                hazardLevel.toLowerCase(), "drawable", context.getPackageName());
        hazardIcon.setImageDrawable(context.getDrawable(hazardIconResId));

        int hazardColor = context.getResources().getIdentifier("hazard_" +
                hazardLevel.toLowerCase(), "color", context.getPackageName());
        hazardLevelText.setText(context.getString(R.string.restaurant_list_hazard_level, hazardAbbr));
        hazardLevelText.setTextColor(context.getColor(hazardColor));

        lastReportDate.setText(Utils.userFriendlyDate(report.getDate(), context));
        numCriticalIssues.setText(String.valueOf(report.getNumCriticalIssues()));
        numNonCriticalIssues.setText(String.valueOf(report.getNumNonCriticalIssues()));

        // When a report is clicked on, start the report details activity, passing its unique key
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(ReportDetailsActivity.makeIntent(context, report.getUniqueKey()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = (ConstraintLayout) itemView;
        }
    }
}
