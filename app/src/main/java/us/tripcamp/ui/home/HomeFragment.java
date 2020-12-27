package us.tripcamp.ui.home;

import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

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
        final ImageView pic1 = root.findViewById(R.id.pic1);
        final ImageView pic2 = root.findViewById(R.id.pic2);
        final GridLayout spotGridLayout = root.findViewById(R.id.spotGridLayout);
        final LinearLayout linearlayout1 = root.findViewById(R.id.linearlayout1);
        testFetch(pic1, pic2, spotGridLayout, linearlayout1);
        return root;
    }

    private void testFetch(final ImageView pic1, final ImageView pic2, final GridLayout spotGridLayout, final LinearLayout linearlayout1) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()));
        String url = "https://tripcamptest.herokuapp.com/api/spots";
        Log.d(TAG, "testFetch: Response fetching");

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
//                        textView.setText("Response is: "+ response.substring(0,500));
//                        Log.d(TAG, "onResponse: " + "Response is: "+ response.substring(0,500));
                        try {
                            JSONObject jObject = new JSONObject(response);
//                            Log.d(TAG, "onResponse: jObject" + jObject.toString());
                            JSONArray jArray = jObject.getJSONArray("spots");
                            Log.d(TAG, "onResponse: " + jArray);
                            for (int i=0; i < jArray.length(); i++)
                            {

                                LinearLayout layout = new LinearLayout(getActivity());
                                layout.setOrientation(LinearLayout.HORIZONTAL);
                                layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300));
                                spotGridLayout.addView(layout);
                                try {
                                    JSONObject oneObject = jArray.getJSONObject(i);
                                    // Pulling items from the array
                                    String id = oneObject.getString("id");
                                    String name = oneObject.getString("name");
                                    String description = oneObject.getString("description");
                                    ArrayList<String> stringArray = new ArrayList<String>();
                                    JSONArray urlArray = oneObject.getJSONArray("urls");
                                    for (int ii=0; ii < urlArray.length(); ii++)
                                    {
                                        try {
                                            String url = urlArray.getString(ii);
//                                            Log.d(TAG, "onResponse: url " + url);
//                                            if(ii == 0)
//                                                Picasso.with(getActivity()).load(url).into(pic1);
//                                            else if(ii == 1)
//                                                Picasso.with(getActivity()).load(url).into(pic2);
                                            ImageView image = new ImageView(getActivity());
                                            image.setLayoutParams(new android.view.ViewGroup.LayoutParams(400,300));
                                            image.setMaxHeight(400);
                                            image.setMaxWidth(300);
                                            Picasso.with(getActivity()).load(url).into(image);
                                            // Adds the view to the layout
                                            layout.addView(image);

                                        } catch (JSONException e) {
                                            Log.d(TAG, "onResponse: oops");
                                        }
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
//                textView.setText("That didn't work!");
                Log.d(TAG, "onErrorResponse: " + "That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}