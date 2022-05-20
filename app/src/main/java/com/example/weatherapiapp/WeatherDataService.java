package com.example.weatherapiapp;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeatherDataService {

    public static final String QUERY_FOR_CITY_ID = "https://www.metaweather.com/api/location/search/?query=";
    public static final String QUERY_FOR_CITY_WEATHER_BY_ID = "https://www.metaweather.com/api/location/";

    Context context;
    String cityID;

    public WeatherDataService(Context context) {
        this.context = context;
    }

    public interface VolleyResponseListener{
        void onError(String message);
        void onResponse(String cityID);
    }

    public interface ForeCastByIdResponse{
        void onError(String message);
        void onResponse(List<WeatherReportModel> weatherReportModels);
    }
    public interface GetCityForecastByNameCallback{
        void onError(String message);
        void onResponse(List<WeatherReportModel> weatherReportModels);
    }

    public void getCityID(String cityName, VolleyResponseListener volleyResponseListener){
        String url = QUERY_FOR_CITY_ID + cityName;


        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                cityID = "";
                try {
                    JSONObject cityInfo = response.getJSONObject(0);
                    cityID = cityInfo.getString("woeid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(context, "City ID = " + cityID, Toast.LENGTH_SHORT).show();
                volleyResponseListener.onResponse(cityID);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError("Something wrong");
            }
        });
        MySingleton.getInstance(context).addToRequestQueue(request);

        //return cityID;

    }

    public void getCityForecastByID(String cityID, ForeCastByIdResponse foreCastByIdResponse){
        List<WeatherReportModel> weatherReportModels = new ArrayList<>();
        String url = QUERY_FOR_CITY_WEATHER_BY_ID + cityID;
        //get the json object
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                try {
                    JSONArray consolodated_weather_list = response.getJSONArray("consolidated_weather");

                    //firstItem



                    for(int i = 0; i< consolodated_weather_list.length(); i++) {

                        WeatherReportModel oneDay = new WeatherReportModel();
                        JSONObject firstDayFromapi = (JSONObject) consolodated_weather_list.get(i);
                        oneDay.setId(firstDayFromapi.getInt("id"));
                        oneDay.setWeatherStateAbbr(firstDayFromapi.getString("weather_state_abbr"));
                        oneDay.setWeatherStateName(firstDayFromapi.getString("weather_state_name"));
                        oneDay.setWindDirectionCompass(firstDayFromapi.getString("wind_direction_compass"));
                        oneDay.setCreated(firstDayFromapi.getString("created"));
                        oneDay.setApplicableDate(firstDayFromapi.getString("applicable_date"));
                        oneDay.setMinTemp(firstDayFromapi.getLong("min_temp"));
                        oneDay.setMaxTemp(firstDayFromapi.getLong("max_temp"));
                        oneDay.setTheTemp(firstDayFromapi.getLong("the_temp"));
                        oneDay.setWindSpeed(firstDayFromapi.getLong("wind_speed"));
                        oneDay.setWindDirection(firstDayFromapi.getLong("wind_direction"));
                        oneDay.setAirPressure(firstDayFromapi.getInt("air_pressure"));
                        oneDay.setHumidity(firstDayFromapi.getInt("Humidity"));
                        oneDay.setVisibility(firstDayFromapi.getLong("visibility"));
                        oneDay.setPredictability(firstDayFromapi.getInt("predictability"));
                        weatherReportModels.add(oneDay);
                    }

                    foreCastByIdResponse.onResponse(weatherReportModels);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },  new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();


            }
        });
        //get the property called "consolodated weather" which is an array
        MySingleton.getInstance(context).addToRequestQueue(request);
    }
//
    public void getCityForecastByName(String cityName, GetCityForecastByNameCallback getCityForecastByNameCallback){
        //fetch the cityid given name

        getCityID(cityName, new VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(String cityID) {
                getCityForecastByID(cityID, new ForeCastByIdResponse() {
                    @Override
                    public void onError(String message) {

                    }

                    @Override
                    public void onResponse(List<WeatherReportModel> weatherReportModels) {
                        //we have the weather report
                        getCityForecastByNameCallback.onResponse(weatherReportModels);
                    }
                });
            }
        });
        //fetch the city forecase given city id
    }


}
