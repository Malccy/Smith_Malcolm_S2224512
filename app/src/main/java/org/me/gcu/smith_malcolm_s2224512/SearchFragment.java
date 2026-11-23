package org.me.gcu.smith_malcolm_s2224512;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class SearchFragment extends Fragment implements View.OnClickListener
{

    public interface SearchFragmentListener
    {
        void onRoadSearch(String typedRoad);
        void onDistanceSearch(String typedKm);
        void onShowAll();
    }

    private SearchFragmentListener listener;
    private EditText etRoad;
    private EditText etDistance;
    private Button searchButton;
    private Button nearButton;
    private Button showAllButton;
    private TextView tvSearchInfo;

    public SearchFragment() {}

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof SearchFragmentListener)
        {
            listener = (SearchFragmentListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement SearchFragmentListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        etRoad = (EditText) v.findViewById(R.id.etRoad);
        etDistance = (EditText) v.findViewById(R.id.etDistance);
        searchButton = (Button) v.findViewById(R.id.searchButton);
        nearButton = (Button) v.findViewById(R.id.nearButton);
        showAllButton = (Button) v.findViewById(R.id.showAllButton);
        tvSearchInfo = (TextView) v.findViewById(R.id.tvSearchInfo);
        searchButton.setOnClickListener(this);
        nearButton.setOnClickListener(this);
        showAllButton.setOnClickListener(this);
        return v;
    }

    public void onClick(View v)
    {
        if (v == searchButton)
        {
            listener.onRoadSearch(etRoad.getText().toString());
        }
        else if (v == nearButton)
        {
            listener.onDistanceSearch(etDistance.getText().toString());
        }
        else if (v == showAllButton)
        {
            listener.onShowAll();
        }
    }

    public void clearRoadBox()
    {
        if (etRoad != null)
        {
            etRoad.setText("");
        }
    }

    public void clearDistanceBox()
    {
        if (etDistance != null)
        {
            etDistance.setText("");
        }
    }

    public void setInfo(String text)
    {
        if (tvSearchInfo != null)
        {
            tvSearchInfo.setText(text);
        }
    }
}