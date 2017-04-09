package maria.ana.matei.com.publictransportationapp;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_FINE_LOCATION = 11;
    private GoogleMap mMap;
    private LatLng currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);


        if (checkPermissions()) {
            setMyLocationEnabled();
        }

        addMapMarkers();

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try {
                    mMap.clear();
                    addMapMarkers();
                    mMap.addMarker(new MarkerOptions().position(latLng).title("User Marker"));
                    Toast.makeText(getApplicationContext(), latLng.latitude + "," + latLng.longitude, Toast.LENGTH_SHORT).show();
                    String requestUrl = createRequestURL(mMap.getMyLocation(), latLng);
                    execAsyncTask(requestUrl);
                    currentMarker = latLng;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    mMap.clear();
                    addMapMarkers();
                    LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                    String requestUrl = createRequestURL(mMap.getMyLocation(), latLng);
                    mMap.addMarker(new MarkerOptions().position(latLng).title("User Marker"));
                    Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                    currentMarker = latLng;
                    execAsyncTask(requestUrl);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        if (checkPermissions()) {
            setMyLocationEnabled();
        }
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setMyLocationEnabled();
                } else {
                    Toast.makeText(getApplicationContext(), "t", Toast.LENGTH_SHORT);
                }
            }
        }
    }

    private void setMyLocationEnabled() {
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                try {
                    locationChange(location);
                } catch (Exception e) {

                }
            }
        });
    }

    public void locationChange(Location origin) throws Exception {
        mMap.clear();
        addMapMarkers();
        LatLng destination = new LatLng(currentMarker.latitude, currentMarker.longitude);
        String requestUrl = createRequestURL(origin, destination);
        execAsyncTask(requestUrl);
    }

    public void execAsyncTask(String requestUrl) {
        DirectionsJsonHelper directionsJsonHelper = new DirectionsJsonHelper() {
            @Override
            protected void onPostExecute(String jsonString) {
                try {
                    drawPath(jsonString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        directionsJsonHelper.execute(requestUrl);
    }

    public String createRequestURL(Location location, LatLng latLng) throws Exception {
        StringBuilder sb = new StringBuilder();
        String baseUrl = "https://maps.googleapis.com/maps/api/directions/";
        String outputFormat = "json?";
        String origin = "origin=" + location.getLatitude() + "," + location.getLongitude();
        String destination = "&destination=" + latLng.latitude + "," + latLng.longitude;
        String myKey = "&key=" + getResources().getString(R.string.google_maps_key);
        sb.append(baseUrl);
        sb.append(outputFormat);
        sb.append(origin);
        sb.append(destination);
        sb.append(myKey);
        Log.wtf("Request URL: ", sb.toString());
        return sb.toString();
    }

    public void addMapMarkers() {
        JsonHelper jsonHelper = new JsonHelper() {
            @Override
            protected void onPostExecute(List<Station> lstStations) {
                try {
                    addMapMarkers(lstStations);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        String jsonUrl = getResources().getString(R.string.jsonLocation);
        jsonHelper.execute(jsonUrl);
    }

    public void addMapMarkers(List<Station> lstStation) throws Exception {
        for (int i = 0; i < lstStation.size(); i++) {
            String name = lstStation.get(i).getNumeStatie();
            double lat = lstStation.get(i).getLatitudine();
            double longi = lstStation.get(i).getLobgitudine();
            LatLng draw = new LatLng(lat, longi);
            mMap.addMarker(new MarkerOptions().position(draw).title(name));
        }
    }

    private List<LatLng> decodePoly(String encoded) throws Exception {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public void drawPath(String jsonString) throws Exception {
        try {
            final JSONObject json = new JSONObject(jsonString);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))
                    .geodesic(true)
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}