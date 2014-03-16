package games.landscape;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 30/06/13.
 */
public class QETargetListAdapter extends ArrayAdapter<TargetChar> implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    List<TargetChar> m_targetChars;

    public QETargetListAdapter(Context context, int viewid, List<TargetChar> targetChars)
    {
        super(context, viewid, targetChars);
        m_targetChars = targetChars;
    }

    public View getView (int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        SViewHolder viewHolder = view == null ? null : (SViewHolder)view.getTag();
        if (viewHolder == null)
        {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.targetcharitem, parent, false);

            viewHolder = new SViewHolder();
//            viewHolder.title = (TextView) view.findViewById(R.id.targetname);
            viewHolder.spinner = (Spinner) view.findViewById(R.id.seldrawable);
            viewHolder.remove = (Button) view.findViewById(R.id.removechar);
            view.setTag(viewHolder);
        }

        TargetChar targetChar = m_targetChars.get(position);
//        viewHolder.title.setText(m_targetChars.get(position).spawnParams.m_name);
        ArrayAdapter<drawable> spinAdapter = new ArrayAdapter<drawable>(this.getContext(), android.R.layout.simple_spinner_item, drawable.s_items);
        viewHolder.spinner.setAdapter(spinAdapter);
        viewHolder.spinner.setSelection(drawable.s_items.indexOf(targetChar.spawnParams));
        viewHolder.spinner.setOnItemSelectedListener(this);
        viewHolder.spinner.setTag(targetChar);
        viewHolder.remove.setTag(targetChar);
        final QETargetListAdapter adapter = this;
        viewHolder.remove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                adapter.remove((TargetChar)view.getTag());
//                this.onRemoveTarget(view.getTag());
            }
        });

        return view;
    }

    @Override
    public void onClick(View view)
    {
        TargetChar targetChar = new TargetChar();
        targetChar.spawnParams = drawable.s_items.get(0);
        this.add(targetChar);
 //     notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        TargetChar targetChar = (TargetChar)adapterView.getTag();
        targetChar.spawnParams = drawable.s_items.get(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    class SViewHolder
    {
        TextView title;
        Spinner spinner;
        Button remove;
    }

}

