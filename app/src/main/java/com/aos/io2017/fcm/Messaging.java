package com.aos.io2017.fcm;

import android.util.Log;

import com.aos.io2017.Application;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by aortuno on 11/25/2016.
 */
public class Messaging {


    private static HttpURLConnection prepareConnection(HttpURLConnection connection) throws UnsupportedEncodingException {
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-type", "application/json");
        connection.setRequestProperty("Authorization", "key=AAAAEdDwvBU:APA91bGwDZOPob7STO0FO_XObhGc4N6jwDlbHfXu_9Cg8tW5YnfthxqHb4fUr1EKiLPiPr2T2YU4f3I4vIMj1OEaGSD_sl2cuGg4jh8BnWdw5hEc_u3eFg6M7jOAdZUNGzwIWNhxk2CR00qUsWwU8ZGjsRRSlsFzPg");
        return connection;
    }

    public static boolean post(String title, String message, String token) throws Exception {
        URL url = new URL("https://fcm.googleapis.com/fcm/send");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {

            JSONObject root = new JSONObject();
            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", title);

            JSONObject data = new JSONObject();
            data.put("message", message);
            root.put("notification", notification);
            root.put("data", data);
            root.put("registration_ids", new JSONArray().put(token));

            Log.d(Application.tag, root.toString());

            prepareConnection(connection);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            DataOutputStream osw = new DataOutputStream(connection.getOutputStream());
            osw.write(root.toString().getBytes());
            osw.flush();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
            throw new Exception(String.format("Error->POST Metodo: %s, Code: %s", connection.getRequestMethod(), connection.getResponseCode()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(Application.tag, e.getMessage());
            throw e;
        } finally {
            connection.disconnect();
        }
    }

    public static boolean post(String title, String message) throws Exception {
        URL url = new URL("https://fcm.googleapis.com/fcm/send");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {

            JSONObject root = new JSONObject();
            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", title);
            notification.put("sound", "default");


            JSONObject data = new JSONObject();
            data.put("message", message);
            root.put("notification", notification);
            root.put("data", data);
            root.put("to", "/topics/chats");

            Log.d(Application.tag, root.toString());

            prepareConnection(connection);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            DataOutputStream osw = new DataOutputStream(connection.getOutputStream());
            osw.write(root.toString().getBytes());
            osw.flush();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
            throw new Exception(String.format("Error->POST Metodo: %s, Code: %s", connection.getRequestMethod(), connection.getResponseCode()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(Application.tag, e.getMessage());
            throw e;
        } finally {
            connection.disconnect();
        }
    }

}
