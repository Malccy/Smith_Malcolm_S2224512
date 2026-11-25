package org.me.gcu.smith_malcolm_s2224512;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class SituationMapFragment extends SupportMapFragment implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private ArrayList<Situation> situations = new ArrayList<Situation>();

    public SituationMapFragment() {}

    public void setSituations(ArrayList<Situation> newSituations)
    {
        situations = newSituations;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        getMapAsync(this);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        LatLng first = null;
        for (Situation s : situations)
        {
            String type = "Incident";
            if (s.getFeedType() == Situation.TYPE_ROADWORKS)
            {
                type = "Roadworks";
            }
            if (s.hasFromPoint())
            {
                LatLng from = new LatLng(s.getFromLatitude(), s.getFromLongitude());
                mMap.addMarker(new MarkerOptions().position(from).title(type + ": " + s.getComment()));
                if (first == null)
                {
                    first = from;
                }
            }
            if (s.hasToPoint())
            {
                LatLng to = new LatLng(s.getToLatitude(), s.getToLongitude());
                mMap.addMarker(new MarkerOptions().position(to).title(type + " (end): " + s.getComment()));
                if (first == null)
                {
                    first = to;
                }
            }
        }

        LatLng gcu = new LatLng(55.8673, -4.2504);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gcu, 9f));
    }
}