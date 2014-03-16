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

import java.util.List;

/**
 * Created by Tom on 30/06/13.
 */
public class QEQuestListAdapter extends ArrayAdapter<Quest> implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    List<Quest> m_subQuests;
    QuestEditor m_questEditor;
    Quest m_quest;

    public QEQuestListAdapter(QuestEditor context, int viewid, List<Quest> subQuests, Quest quest)
    {
        super(context, viewid, subQuests);
        m_quest = quest;
        m_questEditor = context;
        m_subQuests = subQuests;
    }

    public View getView (int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        SViewHolder viewHolder = view == null ? null : (SViewHolder)view.getTag();
        if (viewHolder == null)
        {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.questitem, parent, false);

            viewHolder = new SViewHolder();
            viewHolder.title = (TextView) view.findViewById(R.id.questTitle);
            viewHolder.edit = (Button) view.findViewById(R.id.editquest);
            viewHolder.remove = (Button) view.findViewById(R.id.removechar);
            view.setTag(viewHolder);
        }

        Quest subQuest = m_subQuests.get(position);
        viewHolder.title.setText(subQuest.m_name);
        viewHolder.edit.setTag(subQuest);
        viewHolder.remove.setTag(subQuest);
        final QEQuestListAdapter adapter = this;
        viewHolder.remove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                adapter.remove((Quest)view.getTag());
//                this.onRemoveTarget(view.getTag());
            }
        });
        viewHolder.edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                adapter.edit((Quest)view.getTag());
//                this.onRemoveTarget(view.getTag());
            }
        });

        return view;
    }

    @Override
    public void onClick(View view)
    {
        Quest quest = new Quest("Title", "Blurb", Quest.Condition.Kill);
        quest.m_parent = m_quest;
        this.add(quest);
    }

    public void edit(Quest quest)
    {
        m_questEditor.SetQuest(quest);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        Quest quest = (Quest)adapterView.getTag();
//        quest.spawnParams = drawable.s_items.get(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {
    }

    class SViewHolder
    {
        TextView title;
        Button edit;
        Button remove;
    }

}

