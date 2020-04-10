package com.changhong.testwifi.been;

import android.content.Context;
import android.util.Log;

import com.changhong.testwifi.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HttpInterfaceDate {

    private List<Item> array = new ArrayList<>();

    public HttpInterfaceDate(Context context) {
        String fileContent = null;
        try {
            InputStream is = context.getAssets().open("interface.json");
            fileContent = FileUtils.readFile(is);
            Log.d(getClass().getSimpleName(), "====~ read file  = " + fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fileContent != null) {
            try {
                JSONArray jsonArray = new JSONArray(fileContent);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jobj = jsonArray.getJSONObject(i);
                    Item item = new Item();
                    if (jobj.has("name")) {
                        item.setName(jobj.getString("name"));
                    }
                    if (jobj.has("path"))
                        item.setPath(jobj.getString("path"));
                    if (jobj.has("params"))
                        item.params = jobj.getString("params");
                    array.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(getClass().getSimpleName(), "====~ mArray = " + array);
    }

    public List<Item> getArray() {
        return array;
    }

    public List<String> getNameList() {
        if (array == null)
            return null;

        ArrayList<String> result = new ArrayList<>();
        for (Item item : array) {
            result.add(item.getName());
        }
        return result;
    }


    public static final class Item {
        String name;

        String path;
        String params;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getParams() {
            return params;
        }
    }
}
