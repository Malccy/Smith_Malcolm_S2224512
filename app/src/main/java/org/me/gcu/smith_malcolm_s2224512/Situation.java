package org.me.gcu.smith_malcolm_s2224512;

import android.util.Log;

import java.util.ArrayList;

/**
 * Data class for one "situation" (an incident or a roadworks entry) of the Traffic Scotland feeds.
 * Same idea as the "Thing" class of lecture 2c / Lab 3: private attributes, default constructor,
 * getters, setters and toString(). As in Thing.setBolt(), a setter can process the text it is given.
 */
public class Situation
{
    public static final int TYPE_UNPLANNED = 0;  // came from the unplanned events feed
    public static final int TYPE_ROADWORKS = 1;  // came from the roadworks feed

    private int feedType;
    private String situationId = "";        // id attribute of <situationRecord>
    private String recordType = "";         // xsi:type attribute of <situationRecord>
    private String subType = "";            // e.g. roadClosed, queuingTraffic, roadworks
    private String comment = "";            // generalPublicComment
    private String creationTime = "";
    private String versionTime = "";
    private String startTime = "";          // overallStartTime
    private String endTime = "";            // overallEndTime (roadworks only)
    private String probability = "";
    private String delaysType = "";         // delays, longDelays, veryLongDelays ("" if none given)
    private int operationalLanes = -1;      // -1 = not given
    private int lanesRestricted = -1;       // -1 = not given
    private boolean urgentRoadworks = false;
    private String direction = "";
    private String fromDescription = "";
    private String toDescription = "";
    private float fromLatitude;
    private float fromLongitude;
    private float toLatitude;
    private float toLongitude;
    private boolean hasFromLatitude = false;
    private boolean hasFromLongitude = false;
    private boolean hasToLatitude = false;
    private boolean hasToLongitude = false;
    private ArrayList<String> roads = new ArrayList<String>(); // road names found in the location data
    //getter, setter and toString() methods

    /**
     * Default constructor
     */
    public Situation() {
    }

    public int getFeedType() {
        return feedType;
    }

    public void setFeedType(int feedType) {
        this.feedType = feedType;
    }

    public String getSituationId() {
        return situationId;
    }

    public void setSituationId(String situationId) {
        this.situationId = situationId;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
        Log.d("Situation", String.format("Sub type set: %s", this.subType));
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        Log.d("Situation", String.format("Comment set: %s", this.comment));
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getVersionTime() {
        return versionTime;
    }

    public void setVersionTime(String versionTime) {
        this.versionTime = versionTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getProbability() {
        return probability;
    }

    public void setProbability(String probability) {
        this.probability = probability;
    }

    public String getDelaysType() {
        return delaysType;
    }

    public void setDelaysType(String delaysType) {
        this.delaysType = delaysType;
        Log.d("Situation", String.format("Delays type set: %s", this.delaysType));
    }

    public int getOperationalLanes() {
        return operationalLanes;
    }

    /**
     * Stores the number of operational lanes. The text is converted to an int here, as setBolt() does in Thing.
     * @param lanes  String with the number, e.g. "2"
     */
    public void setOperationalLanes(String lanes) {
        try {
            this.operationalLanes = Integer.parseInt(lanes);
        } catch (NumberFormatException e) {
            Log.e("Situation", "Error parsing operational lanes");
        }
    }

    public int getLanesRestricted() {
        return lanesRestricted;
    }

    public void setLanesRestricted(String lanes) {
        try {
            this.lanesRestricted = Integer.parseInt(lanes);
        } catch (NumberFormatException e) {
            Log.e("Situation", "Error parsing restricted lanes");
        }
    }

    public boolean isUrgentRoadworks() {
        return urgentRoadworks;
    }

    public void setUrgentRoadworks(boolean urgentRoadworks) {
        this.urgentRoadworks = urgentRoadworks;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getFromDescription() {
        return fromDescription;
    }

    public void setFromDescription(String fromDescription) {
        this.fromDescription = fromDescription;
    }

    public String getToDescription() {
        return toDescription;
    }

    public void setToDescription(String toDescription) {
        this.toDescription = toDescription;
    }

    public float getFromLatitude() {
        return fromLatitude;
    }

    public void setFromLatitude(String latitude) {
        try {
            this.fromLatitude = Float.parseFloat(latitude);
            this.hasFromLatitude = true;
        } catch (NumberFormatException e) {
            Log.e("Situation", "Error parsing from latitude");
        }
    }

    public float getFromLongitude() {
        return fromLongitude;
    }

    public void setFromLongitude(String longitude) {
        try {
            this.fromLongitude = Float.parseFloat(longitude);
            this.hasFromLongitude = true;
        } catch (NumberFormatException e) {
            Log.e("Situation", "Error parsing from longitude");
        }
    }

    public float getToLatitude() {
        return toLatitude;
    }

    public void setToLatitude(String latitude) {
        try {
            this.toLatitude = Float.parseFloat(latitude);
            this.hasToLatitude = true;
        } catch (NumberFormatException e) {
            Log.e("Situation", "Error parsing to latitude");
        }
    }

    public float getToLongitude() {
        return toLongitude;
    }

    public void setToLongitude(String longitude) {
        try {
            this.toLongitude = Float.parseFloat(longitude);
            this.hasToLongitude = true;
        } catch (NumberFormatException e) {
            Log.e("Situation", "Error parsing to longitude");
        }
    }

    /** True if the feed gave a usable latitude and longitude for the 'from' point. */
    public boolean hasFromPoint() {
        return hasFromLatitude && hasFromLongitude;
    }

    /** True if the feed gave a usable latitude and longitude for the 'to' point. */
    public boolean hasToPoint() {
        return hasToLatitude && hasToLongitude;
    }

    public ArrayList<String> getRoads() {
        return roads;
    }

    /** Adds a road name to the list, unless it is empty or already there. */
    public void addRoad(String road) {
        if (road == null || road.equals("")) {
            return;
        }
        for (String r : roads) {
            if (r.equals(road)) {
                return;
            }
        }
        roads.add(road);
    }

    @Override
    public String toString() {
        return "Situation{" +
                "feedType=" + feedType +
                ", situationId='" + situationId + '\'' +
                ", recordType='" + recordType + '\'' +
                ", subType='" + subType + '\'' +
                ", comment='" + comment + '\'' +
                ", roads=" + roads +
                ", direction='" + direction + '\'' +
                ", from='" + fromDescription + "' (" + fromLatitude + "," + fromLongitude + ")" +
                ", to='" + toDescription + "' (" + toLatitude + "," + toLongitude + ")" +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", delaysType='" + delaysType + '\'' +
                ", operationalLanes=" + operationalLanes +
                ", lanesRestricted=" + lanesRestricted +
                '}';
    }
}// End of class
