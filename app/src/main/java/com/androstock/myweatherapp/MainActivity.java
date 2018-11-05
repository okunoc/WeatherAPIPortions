package com.androstock.myweatherapp;

import android.content.Intent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;




public class MainActivity extends AppCompatActivity implements LocationListener {

    // Project Created by Ferdousur Rahman Shajib
    // www.androstock.com

    TextView cityField, detailsField, currentTemperatureField, humidity_field, pressure_field, weatherIcon, updatedField, locationText;
    ProgressBar loader;
    Typeface weatherFont;
    String zipcode = "00000";
    String OPEN_WEATHER_MAP_API = "3d58a04d89afa4a0dab92c4e6490991c";
    double latitude, longitude;
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        // location finder stuff first

        locationText = (TextView)findViewById(R.id.locationText);



        /* Permission for fine access location*/
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }



        loader = (ProgressBar) findViewById(R.id.loader);
        cityField = (TextView) findViewById(R.id.city_field);
        updatedField = (TextView) findViewById(R.id.updated_field);
        detailsField = (TextView) findViewById(R.id.details_field);
        currentTemperatureField = (TextView) findViewById(R.id.current_temperature_field);
        humidity_field = (TextView) findViewById(R.id.humidity_field);
        pressure_field = (TextView) findViewById(R.id.pressure_field);
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);






        /* When clicking start button, do following */
        Button Startbtn = findViewById(R.id.Start);
        Startbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //Temp disable task to prevent overuse of queries
                getLocation();


            }
        });


        Button Stopbtn = (Button) findViewById(R.id.Stop);
        Stopbtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

                //locationText.setText("Stopped");
               taskLoadUp(zipcode);

                startActivity(new Intent(MainActivity.this, Additional_info.class));



            }
        });
    }



    /* Begin Location getlocation and overrides shenanigans */

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, this);

        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationText.setText("Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());
        //latitude = location.getLatitude();
        //longitude = location.getLongitude();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            locationText.setText(locationText.getText() + "\n"+addresses.get(0).getAddressLine(0)+", "+addresses.get(0).getAddressLine(1)+", "+addresses.get(0).getAddressLine(2));
            zipcode = addresses.get(0).getPostalCode();
            locationText.setText(locationText.getText()+ "\n Zipcode:" + zipcode);
        }catch(Exception e)
        {

        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }


    /* Begin WeatherAPI loading shenanigans */

    public void taskLoadUp(String query) {
        if (Function.isNetworkAvailable(getApplicationContext())) {
            DownloadWeather task = new DownloadWeather();
            task.execute(query);

        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }



    class DownloadWeather extends AsyncTask < String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //loader.setVisibility(View.VISIBLE);

        }
        protected String doInBackground(String...args) {

            //String xml = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?zip=" + args[0] +
            //        ",us&units=imperial&appid=" + OPEN_WEATHER_MAP_API);
            String xml = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?zip=" + args[0] +
                    ",us&units=imperial&appid=" + OPEN_WEATHER_MAP_API);
            return xml;
        }
        @Override
        protected void onPostExecute(String xml) {

            try {
                JSONObject json = new JSONObject(xml);
                if (json != null) {
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    JSONObject wind = json.getJSONObject("wind");

                    //DateFormat df = DateFormat.getDateTimeInstance();

                   // cityField.setText(json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country"));
                   // detailsField.setText(details.getString("description").toUpperCase(Locale.US));
                    currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + "°");
                  //  humidity_field.setText("Humidity: " + main.getString("humidity") + "%");
                    pressure_field.setText("Windspeed: " + wind.getString("speed") + " mph");

                    //loader.setVisibility(View.GONE);

                }
            } catch (JSONException e) {
               // Toast.makeText(getApplicationContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
            }


        }



    }



}