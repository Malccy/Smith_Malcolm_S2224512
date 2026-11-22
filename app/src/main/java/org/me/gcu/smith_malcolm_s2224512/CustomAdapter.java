package org.me.gcu.smith_malcolm_s2224512;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends BaseAdapter {
    Context context;
    String[] brdList;
    int[] pics;
    int[] colours;
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, String[] birdList, int[] images, int[] rowColours) {
        this.context = context;
        this.brdList = birdList;
        this.pics = images;
        this.colours = rowColours;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return brdList.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        CustomAdapter.WidgetsHolder holder = null;
        if(view == null) {
            view = inflter.inflate(R.layout.layout_listview, null);
            holder = new CustomAdapter.WidgetsHolder();
            holder.bird = (TextView) view.findViewById(R.id.textView);
            holder.pic = (ImageView) view.findViewById(R.id.icon);
            view.setTag(holder);
        }
        else{
            holder = (CustomAdapter.WidgetsHolder)view.getTag();
        }
        holder.bird.setText(brdList[i]);
        holder.pic.setImageResource(pics[i]);
        view.setBackgroundColor(colours[i]);
        return view;
    }
    static class WidgetsHolder {
        TextView bird;
        ImageView pic;
    }
}
