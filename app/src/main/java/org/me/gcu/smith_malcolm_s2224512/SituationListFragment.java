package org.me.gcu.smith_malcolm_s2224512;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

public class SituationListFragment extends Fragment implements AdapterView.OnItemClickListener
{

    public interface SituationListListener
    {
        void onSituationSelected(int position);
    }

    private SituationListListener listener;
    private Context myContext;
    private ListView listView;

    private String[] values;
    private int[] icons;
    private int[] colours;

    public SituationListFragment() {}

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        myContext = context;
        if (context instanceof SituationListListener)
        {
            listener = (SituationListListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement SituationListListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        listView = (ListView) v.findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        showRows();
        return v;
    }

    public void setRows(String[] newValues, int[] newIcons, int[] newColours)
    {
        values = newValues;
        icons = newIcons;
        colours = newColours;
        showRows();
    }

    private void showRows()
    {
        if (listView != null && values != null)
        {
            CustomAdapter customAdapter = new CustomAdapter(myContext.getApplicationContext(), values, icons, colours);
            listView.setAdapter(customAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        listener.onSituationSelected(position);
    }
}