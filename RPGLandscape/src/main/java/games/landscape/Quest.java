package games.landscape;

import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 23/06/13.
 */

class TargetChar
{
    public Vector2f spawnPos = new Vector2f(0.0f, 0.0f);
    public character character = null;
    public drawable spawnParams = null;
    public boolean kill = false;
    public boolean spawn = true;
}


public class Quest
{
    public static final String QUEST_FILENAME = "Quest6";

    public static void saveAll(OutputStream outputStream) throws IOException
    {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        Integer numQuests = s_root.size();
        objectOutputStream.write(numQuests);
        for (Quest quest : s_root)
        {
            quest.save(objectOutputStream);
        }
    }
    public static void loadAll(InputStream inputStream) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        int numQuests = objectInputStream.read();

        s_root.clear();
        for(int i=0; i<numQuests; i++)
        {
            Quest rootQuest = new Quest();
            rootQuest.load(objectInputStream);
            s_root.add(rootQuest);
        }
    }

    private void save(ObjectOutputStream outputStream) throws IOException
    {
        outputStream.write(m_condition.ordinal());
        outputStream.writeObject(m_name);
        outputStream.writeObject(m_blurb);
        int numSubQuests = (m_subQuests != null) ? m_subQuests.size() : 0;
        outputStream.write(numSubQuests);
        if (m_subQuests != null)
        {
            for(Quest quest : m_subQuests)
            {
                quest.save(outputStream);
            }
        }
        int numTargets = (m_targets != null) ? m_targets.size() : 0;
        outputStream.write(numTargets);
        if (m_targets != null)
        {
            for (TargetChar tChar : m_targets)
            {
                outputStream.write(drawable.s_items.indexOf(tChar.spawnParams));
                outputStream.writeObject(tChar.spawnPos);
                outputStream.writeBoolean(tChar.spawn);
                outputStream.writeBoolean(tChar.kill);
            }
        }
    }

    private void load(ObjectInputStream inputStream) throws IOException, ClassNotFoundException
    {
        m_condition = Condition.values()[inputStream.read()];
        m_name = (String) inputStream.readObject();
        m_blurb = (String) inputStream.readObject();
        int numSubQuests = inputStream.read();
        for(int i=0; i<numSubQuests; i++)
        {
            Quest subQuest = new Quest();
            subQuest.load(inputStream);
            subQuest.m_parent = this;
            m_subQuests.add(subQuest);
        }
        int numTargets = inputStream.read();
        for(int i=0; i<numTargets; i++)
        {
            TargetChar tChar = new TargetChar();
            int drawableIdx = inputStream.read();
            tChar.spawnParams = drawable.s_items.get(drawableIdx%drawable.s_items.size());
            tChar.spawnPos = (Vector2f)inputStream.readObject();
            tChar.spawn = inputStream.readBoolean();
            tChar.kill = inputStream.readBoolean();
            m_targets.add(tChar);
        }
    }

    public enum Condition
    {
        Kill,
        Converse,
        SubQuestAny,
        SubQuestAll,
        SubQuestSequence
    };

    public Vector2f m_pos;
    List<TargetChar> m_targets;
    Condition m_condition;
    List<Quest> m_subQuests;
    List<Quest> m_followOn;
    String m_name;
    String m_blurb;
    int m_activeSubQuest;
    boolean m_flaggedCompletion = false;
    Quest m_parent = null;

    static List<Quest> s_root = new ArrayList<Quest>();


    public Quest()
    {
        m_name = null;
        m_blurb = null;
        m_subQuests = null;
        m_condition = null;
        m_activeSubQuest = 0;
        m_subQuests = new ArrayList<Quest>();
        m_targets = new ArrayList<TargetChar>();
    }

    public Quest(String name, String blurb, Condition condition)
    {
        m_name = name;
        m_blurb = blurb;
        m_subQuests = null;
        m_condition = condition;
        m_activeSubQuest = 0;

        switch(m_condition)
        {
            case SubQuestAll:
            case SubQuestAny:
            case SubQuestSequence:
                m_subQuests = new ArrayList<Quest>();
                break;
            case Kill:
            case Converse:
                m_targets = new ArrayList<TargetChar>();
                break;
        }
    }

    public String GetName()
    {
        return m_name;
    }

    public void AddTarget(TargetChar tChar)
    {
        m_targets.add(tChar);
    }

    public void AddSubQuest(Quest subQuest)
    {
        m_subQuests.add(subQuest);
        subQuest.m_parent = this;
    }

    public void AddFollowOn(Quest followOnQuest)
    {
        m_followOn.add(followOnQuest);
        followOnQuest.m_parent = this;
    }

    public void Begin()
    {
        if (m_targets != null)
        {
            for (TargetChar tc : m_targets)
            {
                if (tc.spawn == true)
                {
                    tc.character = DoSpawn(tc.spawnParams, tc.spawnPos);
                    tc.character.CanBeDeleted(false);
                }
            }
        }

        switch (m_condition)
        {
            case SubQuestAny:
            case SubQuestAll:
                for (Quest subQ : m_subQuests)
            {
                subQ.Begin();
            }
            break;

            case SubQuestSequence:
                if (m_subQuests.size() > 0)
                {
                    m_subQuests.get(0).Begin();
                }
        }
    }

    public boolean Update()
    {
        boolean finished = false;
        switch (m_condition)
        {
            case SubQuestSequence:
                if (m_activeSubQuest < m_subQuests.size())
                {
                    if (m_subQuests.get(m_activeSubQuest).Update() == true)
                    {
                        m_subQuests.get(m_activeSubQuest).End();
                        m_activeSubQuest++;

                        if (m_activeSubQuest < m_subQuests.size())
                        {
                            m_subQuests.get(m_activeSubQuest).Begin();
                        }
                    }

                    //--- No new quests, this one is done!
                    finished = (m_activeSubQuest >= m_subQuests.size());
                    break;
                }
                break;
            case SubQuestAny:
            case SubQuestAll:
            {
                if (m_subQuests.size() > 0)
                {
                    int doneCount = 0;
                    for (Quest subQ : m_subQuests)
                    {
                        if (subQ.Update())
                        {
                            doneCount++;
                        }
                    }

                    finished = ((m_condition == Condition.SubQuestAny) && (doneCount > 0))
                            || ((m_condition == Condition.SubQuestAll) && (doneCount == m_subQuests.size()));
                }
                else
                {
                    finished = true;
                }

                break;
            }
            case Kill:
                finished = true;
                if (m_targets != null)
                {
                    for (TargetChar tc : m_targets)
                    {
                        if (!tc.character.IsDead())
                        {
                            finished = false;
                        }
                    }
                }
                break;
            case Converse:
                finished = m_flaggedCompletion;
                break;
        }

        return finished;
    }

    public void End()
    {
        if (m_targets != null)
        {
            for (TargetChar tc : m_targets)
            {
                if (tc.character != null)
                {
                    tc.character.CanBeDeleted(true);
                }
            }
        }

        switch (m_condition)
        {
            case SubQuestAny:
            case SubQuestAll:
                for (Quest subQ : m_subQuests)
            {
                subQ.End();
            }
            break;
        }
    }

    public void FlagComplete()
    {
        m_flaggedCompletion = true;
    }

    public character DoSpawn(drawable entParams, Vector2f pos)
    {
//        EntityParams eParams = Globals.Content.Load<EntityParams>("EntityParams/"+entParams);
        character spawned = new character();
        spawned.m_pos = pos;
        spawned.m_drawable = entParams;

        World.Get().m_entityManager.AddCharacter(spawned);

        return spawned;
    }

    public Quest Find(String qName)
    {
        if (m_name == qName)
        {
            return this;
        }
        else
        {
            switch (m_condition)
            {
                case SubQuestAll:
                case SubQuestAny:
                case SubQuestSequence:
                    for (Quest subQ : m_subQuests)
                {
                    Quest ret = subQ.Find(qName);
                    if (ret != null)
                    {
                        return ret;
                    }
                }
                break;
            }
        }

        return null;
    }

    public String BuildDescription()
    {
        return BuildDescription("");
    }

    String BuildDescription(String indent)
    {
        String eol = System.getProperty("line.separator");

        switch (m_condition)
        {
            case Converse:
                return indent + m_blurb + eol; //indent + "See " + m_targets[0].spawnParams + "\n";

            case Kill:
                return indent + m_blurb + eol; //indent + "Kill " + m_targets[0].spawnParams + "\n";

            case SubQuestAll:
            case SubQuestAny:
            {
                String ret = indent + m_blurb + eol; //indent + m_name + "\n";
                for (Quest subQ : m_subQuests)
                {
                    ret += subQ.BuildDescription(indent+"   ");
                }
                return ret;
            }

            case SubQuestSequence:
            {
                String ret = indent + m_name + eol;
                int questNum = 0;
                for (Quest subQ : m_subQuests)
                {
                    ret += subQ.BuildDescription(indent + "   ");
                    questNum++;
                    if (questNum > m_activeSubQuest)
                        break;
                }
                return ret;
            }
        }

        return "XXXX";
    }
}
/*
static class QuestDatabase
{
    static SortedList<string, Quest> sm_quests;

    public static void Init()
    {
        sm_quests = new SortedList<string, Quest>();
    }

    public static void AddQuest(Quest quest)
    {
        sm_quests.Add(quest.Name, quest);
    }
    public static Quest GetQuest(string questName)
    {
        return sm_quests[questName];
    }
}
*/