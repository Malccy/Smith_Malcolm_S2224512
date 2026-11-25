package org.me.gcu.smith_malcolm_s2224512;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class TrafficViewModel extends ViewModel
{

    private ArrayList<Situation> unplannedList = new ArrayList<Situation>();
    private ArrayList<Situation> roadworksList = new ArrayList<Situation>();
    private ArrayList<Situation> allSituations = new ArrayList<Situation>();
    private boolean unplannedLoaded = false;
    private boolean roadworksLoaded = false;
    private boolean unplannedOffline = false;
    private boolean roadworksOffline = false;

    private boolean started = false;

    private String searchRoad = "";
    private float searchKm = 0;
    private String searchKmText = "";

    private boolean mapShown = false;

    private final MutableLiveData<String> observedStatus;

    public TrafficViewModel()
    {
        observedStatus = new MutableLiveData<>();
    }

    public LiveData<String> getObservedStatus()
    {
        return observedStatus;
    }

    public boolean isStarted()
    {
        return started;
    }

    public void setStarted(boolean value)
    {
        started = value;
    }

    public void startLoading()
    {
        unplannedLoaded = false;
        roadworksLoaded = false;
    }

    public void setResult(int feedType, ArrayList<Situation> list, boolean offline)
    {
        if (feedType == Situation.TYPE_UNPLANNED)
        {
            unplannedList = list;
            unplannedLoaded = true;
            unplannedOffline = offline;
        }
        else
        {
            roadworksList = list;
            roadworksLoaded = true;
            roadworksOffline = offline;
        }
        allSituations = new ArrayList<Situation>();
        for (Situation u : unplannedList)
        {
            allSituations.add(u);
        }
        for (Situation r : roadworksList)
        {
            allSituations.add(r);
        }

        String unplannedText = "...";
        String roadworksText = "...";
        if (unplannedLoaded) { unplannedText = String.valueOf(unplannedList.size()); }
        if (roadworksLoaded) { roadworksText = String.valueOf(roadworksList.size()); }
        String status = "Incidents: " + unplannedText + "   Roadworks: " + roadworksText;
        if (unplannedOffline || roadworksOffline)
        {
            status = status + "   (OFFLINE - saved copy used)";
        }
        if (unplannedLoaded && roadworksLoaded && allSituations.size() == 0)
        {
            status = "No data: could not download it and there is no saved copy.";
        }
        observedStatus.setValue(status);
    }

    public boolean isMapShown()
    {
        return mapShown;
    }

    public void setMapShown(boolean value)
    {
        mapShown = value;
    }

    public ArrayList<Situation> getAllSituations()
    {
        return allSituations;
    }

    public void setSearch(String road, float km, String kmText)
    {
        searchRoad = road;
        searchKm = km;
        searchKmText = kmText;
    }

    public String getSearchRoad()
    {
        return searchRoad;
    }

    public float getSearchKm()
    {
        return searchKm;
    }

    public String getSearchKmText()
    {
        return searchKmText;
    }
}