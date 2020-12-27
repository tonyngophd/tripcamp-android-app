package us.tripcamp.ui.home;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import us.tripcamp.MainActivity;
import us.tripcamp.R;

import static android.service.controls.ControlsProviderService.TAG;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        final GridLayout spotGridLayout = root.findViewById(R.id.spotGridLayout);
        fetchSpots(spotGridLayout);
        return root;
    }

    private void fetchSpots(final GridLayout spotGridLayout) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()));
        String url = "https://tripcamptest.herokuapp.com/api/spots";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        ///https://stackoverflow.com/questions/9605913/how-do-i-parse-json-in-android
                        try {
                            JSONObject jObject = new JSONObject(response);
                            JSONArray jArray = jObject.getJSONArray("spots");
                            //Log.d(TAG, "onResponse: " + jArray);
                            for (int i = 0; i < jArray.length(); i++) {

                                LinearLayout layout = new LinearLayout(getActivity());
                                layout.setOrientation(LinearLayout.HORIZONTAL);
                                layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                layout.setPadding(100, 50, 20, 50);
                                spotGridLayout.addView(layout);
                                try {
                                    JSONObject oneObject = jArray.getJSONObject(i);
                                    // Pulling items from the array
                                    String id = oneObject.getString("id");
                                    final String name = oneObject.getString("name");
                                    String address = oneObject.getString("streetAddress");
                                    String city = oneObject.getString("city");
                                    String stateProvince = oneObject.getString("stateProvince");
                                    String country = oneObject.getString("country");
                                    String zipCode = oneObject.getString("zipCode");
                                    String description = oneObject.getString("description");
                                    ArrayList<String> stringArray = new ArrayList<String>();
                                    final JSONArray gpsCoordinates = oneObject.getJSONArray("gpsLocation");
                                    JSONArray urlArray = oneObject.getJSONArray("urls");
                                    //for (int ii = 0; ii < urlArray.length(); ii++) {
                                    LinearLayout linearLayout = new LinearLayout(getActivity());
                                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                                    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(650, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    linearLayout.setPadding(100, 20, 50, 10);
                                    layout.addView(linearLayout);

                                    ///https://stackoverflow.com/questions/18391830/how-to-programmatically-round-corners-and-set-random-background-colors
                                    layout.setBackgroundResource(R.drawable.tags_rounded_corners);
                                    GradientDrawable border = (GradientDrawable) layout.getBackground();;
                                    border.setColor(0xFFFFFFFF); //white background
                                    border.setStroke(1, 0xFF000000); //black border with full opacity
                                    //layout.setBackground(border);

                                    for (int ii = 0; ii < 1; ii++) {
                                        try {
                                            String url = urlArray.getString(ii);
                                            ImageView image = new ImageView(getActivity());
                                            image.setLayoutParams(new LinearLayout.LayoutParams(550, 400));
                                            //image.setMaxHeight(400);
                                            //image.setMaxWidth(300);
                                            Picasso.with(getActivity()).load(url).into(image);
                                            // Adds the view to the layout
                                            linearLayout.addView(image);

                                            TextView infoText = new TextView(getActivity());
                                            infoText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                            infoText.setPadding(50, 10, 50, 10);
                                            infoText.setText(address + " " + city + " " + stateProvince + ", " + zipCode + " " + country);
                                            linearLayout.addView(infoText);
                                        } catch (JSONException e) {
                                            Log.d(TAG, "onResponse: oops");
                                        }
                                        TextView descriptionText = new TextView(getActivity());
                                        descriptionText.setLayoutParams(new LinearLayout.LayoutParams(700, 600));
                                        descriptionText.setPadding(20, 10, 50, 20);
                                        descriptionText.setText(description.toString().replaceAll("      ", "").replaceAll("  ", ""));
                                        layout.addView(descriptionText);
                                        final Handler handler = new Handler(Looper.getMainLooper());
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(MainActivity.mMap != null) {
                                                    MainActivity.mMap.setMinZoomPreference(5.0f);
                                                    try {
                                                        MainActivity.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpsCoordinates.getDouble(0), gpsCoordinates.getDouble(1)), 5f));
                                                        MainActivity.mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng(gpsCoordinates.getDouble(0), gpsCoordinates.getDouble(1)))
                                                                .title(name));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }, 100);

                                    }
                                } catch (JSONException e) {
                                    // Oops
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + "That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}