package cs307.butterfly;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Travis on 10/7/2016.
 */

public class Community {
    private ArrayList<CommunityEvent> communityEvents;
    private String name;

    public Community(String name) {
        this.communityEvents = new ArrayList<>();
        this.name = name;
        Log.d("createCommunity", name);
    }

    public ArrayList<CommunityEvent> getCommunityEvents() {
        return this.communityEvents;
    }

    public void addEvent(CommunityEvent event) {
        communityEvents.add(event);
        Log.d("addEvent", "added");
    }

    public String getName() {
        return this.name;
    }
    public boolean deleteEvent(CommunityEvent event) {
        return communityEvents.remove(event);
    }
}
