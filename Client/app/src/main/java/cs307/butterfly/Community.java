package cs307.butterfly;

import java.util.ArrayList;

/**
 * Created by Travis on 10/7/2016.
 */

public class Community {
    private ArrayList<CommunityEvent> communityEvents;

    public Community() {
        this.communityEvents = new ArrayList<>();
    }

    public ArrayList<CommunityEvent> getCommunityEvents() {
        return this.communityEvents;
    }

    public void addEvent(CommunityEvent event) {
        communityEvents.add(event);
    }

    public boolean deleteEvent(CommunityEvent event) {
        return communityEvents.remove(event);
    }
}
