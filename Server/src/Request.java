import org.json.simple.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by nick on 10/11/16.
 */
public class Request {
    private RequestTypes requestType;
    private String requesterId;
    private Date timestamp;
    private DataTypes primaryObjType;
    private DataTypes secondaryObjType;
    private Hashtable<String, String> primaryObjProperties;
    private Hashtable<String, String> secondaryObjProperties;

    public Request(JSONObject jObj) {
        this.requestType = RequestTypes.valueOf(jObj.get("command").toString());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM'T'HH:mm:ssZ");
        try {
            this.timestamp = formatter.parse(jObj.get("timestamp").toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (requestType) {
            case ADD:
                JSONObject secondaryJObj = (JSONObject) jObj.get("primary-object");
                this.secondaryObjType = DataTypes.valueOf(secondaryJObj.get("type").toString());
                secondaryObjProperties.putAll((Map) secondaryJObj.get("properties"));
            default:
                JSONObject primaryJObj = (JSONObject) jObj.get("primary-object");
                this.primaryObjType = DataTypes.valueOf(primaryJObj.get("type").toString());
                primaryObjProperties.putAll((Map) primaryJObj.get("properties"));
        }
    }

    public DataTypes getPrimaryObjType() {
        return primaryObjType;
    }

    public List<String> getPrimaryColumns() {
        return new ArrayList<String>(primaryObjProperties.keySet());
    }

    public List<String> getPrimaryValues() {
        return new ArrayList<String>(primaryObjProperties.values());
    }

    public String getPrimaryValueFor(String column) {
        return primaryObjProperties.get(column);
    }

    public Map<String, String> getPrimaryProperties() {
        return primaryObjProperties;
    }

    public RequestTypes getRequestType() {
        return requestType;
    }



}
