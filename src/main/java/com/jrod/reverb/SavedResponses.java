package com.jrod.reverb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class SavedResponses {
    private Preferences preferences;
    private List<String> responses;
    private Gson gson = new Gson();
    private Type typeToken = new TypeToken<List<String>>() {}.getType();

    public SavedResponses() {
        preferences = Preferences.userNodeForPackage(AppController.class);
        String srString = preferences.get("savedResponsesJSON", "[\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"]");
        responses = gson.fromJson(srString, typeToken);
    }

    public void saveResponse(int index, String response) {
        responses.remove(index);
        responses.add(index, response);
        preferences.put("savedResponsesJSON", gson.toJson(responses, typeToken));
    }

    public String get(int index) {
        return responses.get(index);
    }
}
