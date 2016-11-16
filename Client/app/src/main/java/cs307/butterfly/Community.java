/*
 * Created by Travis on 10/7/2016.
 */

package cs307.butterfly;

import android.util.Log;

import java.util.ArrayList;

class Community {
    ArrayList<CommunityEvent> communityEvents;
    private String name;

    Community(String name) {
        this.communityEvents = new ArrayList<>();
        this.name = name;
        Log.d("createCommunity", name);
    }

    ArrayList<CommunityEvent> getCommunityEvents() {
        return this.communityEvents;
    }

    void addEvent(CommunityEvent event) {
        communityEvents.add(event);
        Log.d("addEvent", "added");
    }

    public String getName() {
        return this.name;
    }
    @SuppressWarnings("unused")
    public boolean deleteEvent(CommunityEvent event) {
        return communityEvents.remove(event);
    }
}
