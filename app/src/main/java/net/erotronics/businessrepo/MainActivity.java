package net.erotronics.businessrepo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    ArrayList<HashMap<String, String>> bizList;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lv =  findViewById(R.id.list);

      bizList = new ArrayList<>();

        GetBusinesses getBusinesses = new GetBusinesses();
        getBusinesses.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String makeServiceCall(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("church", "globe");
            conn.setRequestProperty("town", "reading");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    private String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the
         * BufferedReader.readLine() method. We iterate until the
         * BufferedReader return null which means there's no more data to
         * read. Each line will appended to a StringBuilder and returned as
         * String.
         */
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Get message from the cloud DB
     * @author sentaca
     *
     */
    private class GetBusinesses extends AsyncTask<Void, Void, String> {
        String output;
        @Override
        protected String doInBackground(Void... arg0) {
            Log.d("PRE", "SUCCESS");
            String url = "http://ec2-18-222-233-158.us-east-2.compute.amazonaws.com:8080/business-repository-service/list-businesses";

             output = makeServiceCall(url);
            try {
                Log.d(TAG, output);

            }
            catch (Exception e) {
                e.getCause();
            }

            return null;
            }

        protected void onPostExecute(String result) {
            JSONArray business;
            try {
                JSONArray jBiz = new JSONArray(output);

                Log.d(TAG, "STUFFFF");
                for(int i=0;i<jBiz.length();i++){
                    HashMap<String,String> biz = new HashMap<>();
                    JSONObject obj = jBiz.getJSONObject(i);
                    biz.put("businessName", obj.getString("businessName"));
                    biz.put("businessLocation", obj.getString("businessLocation"));
                    biz.put("phoneNumber", obj.getString("phoneNumber"));

                    bizList.add(biz);
                    Log.d("name", obj.getString("businessName"));
                }
            }
            catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                e.getStackTrace();
            }

            super.onPostExecute(result);
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, bizList,
                    R.layout.list_item, new String[]{ "businessName","businessLocation", "phoneNumber" },
                    new int[]{R.id.biz_name, R.id.address, R.id.biz_phone});
            lv.setAdapter(adapter);

        }



    }
}
