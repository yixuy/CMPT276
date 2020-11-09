package cmpt276.helium.app.model;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import cmpt276.helium.app.R;

/*
    AlertDialog.Builder subclass that inflates a custom view that allows the user to search/filter
    restaurants in either the MapsActivity or the RestaurantActivityList and abstracts away
    much of the View handling
 */
public class SearchDialogBuilder extends AlertDialog.Builder {

    private EditText restaurantName;
    private EditText minCritical;
    private EditText maxCritical;

    private RadioButton lowHazard;
    private RadioButton medHazard;
    private RadioButton highHazard;

    private CheckBox favouritesOnly;

    private Button clearButton;
    private Button filterButton;

    public SearchDialogBuilder(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.search_dialog, null);

        restaurantName = view.findViewById(R.id.searchDialogRestaurantName);
        minCritical = view.findViewById(R.id.searchDialogMinCritical);
        maxCritical = view.findViewById(R.id.searchDialogMaxCritical);

        lowHazard = view.findViewById(R.id.searchHazardLowRadio);
        medHazard = view.findViewById(R.id.searchHazardMedRadio);
        highHazard = view.findViewById(R.id.searchHazardHighRadio);

        // Clumsy but there's no better way to do this for radio button groups with even remotely
        // complex layouts that include other views alongside the radio buttons, see:
        // https://stackoverflow.com/questions/10461005/how-to-group-radiobutton-from-different-linearlayouts/13273890
        lowHazard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                medHazard.setChecked(false);
                highHazard.setChecked(false);
            }
        });
        medHazard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lowHazard.setChecked(false);
                highHazard.setChecked(false);
            }
        });
        highHazard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lowHazard.setChecked(false);
                medHazard.setChecked(false);
            }
        });

        favouritesOnly = view.findViewById(R.id.searchDialogFavouritesOnly);

        clearButton = view.findViewById(R.id.searchDialogClear);
        filterButton = view.findViewById(R.id.searchDialogFilter);

        setView(view);
    }

    public String getRestaurantName() {
        return restaurantName.getText().toString();
    }

    public Integer getMinViolations() {
        String text = minCritical.getText().toString();
        if(text.isEmpty()) {
            return null;
        }
        return Integer.parseInt(text);
    }

    public Integer getMaxViolations() {
        String text = maxCritical.getText().toString();
        if(text.isEmpty()) {
            return null;
        }
        return Integer.parseInt(text);
    }

    public boolean shouldOnlyShowFavourites() {
        return favouritesOnly.isChecked();
    }

    public String getSelectedHazardLevel() {
        if(lowHazard.isChecked()) {
            return "Low";
        }
        if(medHazard.isChecked()) {
            return "Moderate";
        }
        if(highHazard.isChecked()) {
            return "High";
        }
        return null;
    }

    public void setClearButtonListener(View.OnClickListener listener) {
        clearButton.setOnClickListener(listener);
    }

    public void setFilterButtonListener(View.OnClickListener listener) {
        filterButton.setOnClickListener(listener);
    }
}
