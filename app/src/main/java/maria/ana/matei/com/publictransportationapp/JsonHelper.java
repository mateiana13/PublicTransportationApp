package maria.ana.matei.com.publictransportationapp;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JsonHelper extends AsyncTask<String, Void, List<Station>> {

    @Override
    protected List<Station> doInBackground(String... params) {
        List<Station> lstStations = new ArrayList<Station>();
        try {
            URL url = new URL(params[0]);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            JSONObject json = jsonObject.getJSONObject("Stations");
            JSONArray jsonArray[] = new JSONArray[4];
            jsonArray[0] = json.getJSONArray("M1");
            jsonArray[1] = json.getJSONArray("M2");
            jsonArray[2] = json.getJSONArray("M3");
            jsonArray[3] = json.getJSONArray("M4");

            for (int i = 0; i < jsonArray.length; i++) {
                for (int j = 0; j < jsonArray[i].length(); j++) {
                    JSONObject el = jsonArray[i].getJSONObject(j);
                    Station s = new Station(el.getString("-name"), el.getDouble("-lat"), el.getDouble("-long"));
                    lstStations.add(s);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lstStations;
    }
}
