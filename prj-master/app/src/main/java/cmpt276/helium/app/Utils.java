package cmpt276.helium.app;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/*
    Class that provides utility functions for removes quote on either end of a String for use
    while parsing a .csv, getting restaurant icons based on name, and helpers for generating
    nicely formatted dates
 */
public class Utils {


    public static String unquote(String s) {
        int length = 0;
        if (s != null) {
            length = s.length();
        }
        if (length > 1 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, length - 1);
        }
        return s;
    }


    // We have to user .contains rather than exact string matching (which would avoid all these
    // conditionals) because many of the restaurant names are not just their "name"
    // e.g. 7-Eleven in the data set appears as 7-Eleven #26365, 7-Eleven #26517, etc.
    public static int getRestaurantIcon(String name) {
        if (name.contains("7-Eleven")) {
            return R.drawable.restaurant_7_eleven;
        }
        if (name.contains("A&W") || name.contains("A & W")) {
            return R.drawable.restaurant_a_and_w;
        }
        if (name.contains("Blenz Coffee")) {
            return R.drawable.restaurant_blenz_coffee;
        }
        if (name.contains("Booster Juice")) {
            return R.drawable.restaurant_booster_juice;
        }
        if (name.contains("Boston Pizza")) {
            return R.drawable.restaurant_boston_pizza;
        }
        if (name.contains("Browns Socialhouse")) {
            return R.drawable.restaurant_browns_socialhouse;
        }
        if (name.contains("Burger King")) {
            return R.drawable.restaurant_burger_king;
        }
        if (name.contains("Church's Chicken")) {
            return R.drawable.restaurant_churchs_chicken;
        }
        if (name.contains("Dairy Queen")) {
            return R.drawable.restaurant_dairy_queen;
        }
        if (name.contains("Domino's Pizza")) {
            return R.drawable.restaurant_dominos_pizza;
        }
        if (name.contains("Freshslice Pizza")) {
            return R.drawable.restaurant_freshslice_pizza;
        }
        if (name.contains("KFC")) {
            return R.drawable.restaurant_kfc;
        }
        if (name.contains("Little Caesars")) {
            return R.drawable.restaurant_little_caesars;
        }
        if (name.contains("McDonald's")) {
            return R.drawable.restaurant_mcdonalds;
        }
        if (name.contains("Pizza Hut")) {
            return R.drawable.restaurant_pizza_hut;
        }
        if (name.contains("Safeway")) {
            return R.drawable.restaurant_safeway;
        }
        if (name.contains("Save On Foods")) {
            return R.drawable.restaurant_save_on_foods;
        }
        if (name.contains("Starbucks")) {
            return R.drawable.restaurant_starbucks;
        }
        if (name.contains("Subway")) {
            return R.drawable.restaurant_subway;
        }
        if (name.contains("Tim Hortons")) {
            return R.drawable.restaurant_tim_hortons;
        }
        if (name.contains("Wendy's")) {
            return R.drawable.restaurant_wendys;
        }
        if (name.contains("White Spot")) {
            return R.drawable.restaurant_white_spot;
        }
        if (name.contains("Sushi")) {
            return R.drawable.restaurant_sushi;
        }
        if (name.contains("Pizza")) {
            return R.drawable.restaurant_pizza;
        }
        if (name.contains("Seafood")) {
            return R.drawable.restaurant_seafood;
        }
        if (name.contains("Bar")) {
            return R.drawable.restaurant_bar;
        }
        return R.drawable.restaurant_default;
    }


    public static String userFriendlyDate(String date, Context context) {
        try {
            Date now = new Date();
            Date dateNum = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).parse(date);
            long difference = now.getTime() - Objects.requireNonNull(dateNum).getTime();

            // Converting number of milliseconds to number of days with standard library, from
            // https://stackoverflow.com/questions/20165564/calculating-days-between-two-dates-with-java
            long days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
            if (days <= 30) {
                return context.getString(R.string.n_days_ago, days);
            }
            if (days <= 365) {
                SimpleDateFormat monthDay = new SimpleDateFormat(Constants.DATE_FORMAT_MONTH_DAY, Locale.getDefault());
                return monthDay.format(dateNum);
            }

            SimpleDateFormat monthYear = new SimpleDateFormat(Constants.DATE_FORMAT_MONTH_YEAR, Locale.getDefault());
            return monthYear.format(dateNum);

        } catch (ParseException e) {
            return date;
        }
    }


    public static String makeFullDate(String date) {
        try {
            Date dateNum = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).parse(date);
            SimpleDateFormat fullFormat = new SimpleDateFormat(Constants.DATE_FORMAT_FULL, Locale.getDefault());
            return fullFormat.format(Objects.requireNonNull(dateNum));

        } catch (ParseException e) {
            return date;
        }
    }
}
