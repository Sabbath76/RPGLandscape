package games.landscape;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 30/06/13.
 */
public class QuestEditor extends Activity {

    Quest mQuest;
    EditText mTextTitle;
    EditText mTextBlurb;
    Spinner mSpinner;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.questeditor);

        mSpinner = (Spinner) findViewById(R.id.questtype);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.condition_array, android.R.layout.simple_spinner_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                if (mQuest != null)
                {
                    mQuest.m_condition = Quest.Condition.values()[i];

                    if ((mQuest.m_condition == Quest.Condition.Kill)
                            || (mQuest.m_condition == Quest.Condition.Converse))
                    {
                        if (mQuest.m_targets == null)
                        {
                            mQuest.m_targets = new ArrayList<TargetChar>();
                        }
                    }
                    else
                    {
                        if (mQuest.m_subQuests == null)
                        {
                            mQuest.m_subQuests = new ArrayList<Quest>();
                        }
                    }

                    mQuest.m_name  = mTextTitle.getText().toString();
                    mQuest.m_blurb = mTextBlurb.getText().toString();

                    SetQuest(mQuest);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
            }
        });

        SetQuest(null);
    }

    void SetQuest(Quest quest)
    {
        mQuest = quest;

        mTextTitle = (EditText) findViewById(R.id.editTitle);
        mTextBlurb = (EditText) findViewById(R.id.editblurb);

        final ListView targetList = (ListView)findViewById(R.id.targetList);
        final Button button = (Button) findViewById(R.id.addtarget);

        if (quest == null)
        {
            mTextTitle.setText("Quest");
            mTextBlurb.setText("Here are all the quests available");
            mSpinner.setVisibility(View.GONE);

            button.setText("Add Root Quest");
            List<Quest> subQuests = Quest.s_root;
            QEQuestListAdapter adapter = new QEQuestListAdapter(this, R.layout.targetcharitem, subQuests, mQuest);
            targetList.setAdapter(adapter);
            button.setOnClickListener(adapter);
        }
        else
        {
            mTextTitle.setText(mQuest.m_name);
            mTextBlurb.setText(mQuest.m_blurb);

            if ((quest.m_condition == Quest.Condition.Kill)
                    || (quest.m_condition == Quest.Condition.Converse))
            {
                button.setText("Add Target");
                List<TargetChar> targetChars = mQuest.m_targets;
                QETargetListAdapter adapter = new QETargetListAdapter(this, R.layout.targetcharitem, targetChars);
                targetList.setAdapter(adapter);
                button.setOnClickListener(adapter);
            }
            else
            {
                button.setText("Add Quest");
                List<Quest> subQuests = mQuest.m_subQuests;
                QEQuestListAdapter adapter = new QEQuestListAdapter(this, R.layout.targetcharitem, subQuests, mQuest);
                targetList.setAdapter(adapter);
                button.setOnClickListener(adapter);
            }

            mSpinner.setVisibility(View.VISIBLE);
            mSpinner.setSelection(mQuest.m_condition.ordinal());
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // The activity is about to be destroyed.

        if (mQuest != null)
        {
            mQuest.m_name  = mTextTitle.getText().toString();
            mQuest.m_blurb = mTextBlurb.getText().toString();
        }
    }

    @Override
    public void onBackPressed()
    {
        if (mQuest != null)
        {
            mQuest.m_name  = mTextTitle.getText().toString();
            mQuest.m_blurb = mTextBlurb.getText().toString();

            SetQuest(mQuest.m_parent);
        }
        else
        {
            try
            {
                FileOutputStream fos = openFileOutput(Quest.QUEST_FILENAME, Context.MODE_PRIVATE);
                Quest.saveAll(fos);
                fos.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            super.onBackPressed();
        }
    }


}