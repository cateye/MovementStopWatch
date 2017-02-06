package com.kataysantos.movementstopwatch;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by katay on 2/2/17.
 */
class LapAdapter extends ArrayAdapter<StopWatch.Time> {

    private static Logger LOG = Logger.getLogger("LapAdapter");
    LapAdapter(Context context) {
        super(context, 0, new ArrayList<StopWatch.Time>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LOG.info("getView(position="+position+")");

        StopWatch.Time lap = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timelapsed_list_item, parent, false);
        }
        TextView timeLapsed = (TextView) convertView.findViewById(R.id.timelapsed_list_lapsedTime_textView);
        timeLapsed.setText((CharSequence) lap.toString());

        return convertView;
    }

    @Override
    public void add(StopWatch.Time object) {
        super.add(object);
        notifyDataSetChanged();
        LOG.info("added object, new count -> " + getCount());
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        observer.onChanged();
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
       LOG.info("unresigerDataSetObserver");
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}
