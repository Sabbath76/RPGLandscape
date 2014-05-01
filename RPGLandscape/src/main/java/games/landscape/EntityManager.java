package games.landscape;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

/**
 * Created by Tom on 07/06/13.
 */
public class EntityManager
{
    private float m_spawnRange = 2.0f;
    private float m_despawnDistSqr = 3.0f * 3.0f;

    public void AddCharacter(character newChar)
    {
        m_entities.add(newChar);
    }

    public character GetHit(Vector2f pos, float range)
    {
        final float rangeSqr = range*range;
        for (entity entTest : m_entities)
        {
            if (entTest instanceof character)
            {
                character charTest = (character )entTest;
                if (charTest.m_pos.distanceSquared(pos) < rangeSqr)
                    return charTest;
            }
        }

        return null;
    }

    List<entity> m_spawnEntities = new ArrayList<entity>();
    List<entity> m_entities = new ArrayList<entity>();
    List<collectable> m_collectables = new ArrayList<collectable>();
    int m_numSpawned = 0;
    int m_maxSpawn = 5;
    List<spawnParams> m_spawnables = new ArrayList<spawnParams>();
    int m_spawnTotal = 0;
//    drawable m_spawnTemplate = null;

    public void Render(Canvas canvas, Rect screenWindow, RectF worldWindow)
    {
        try
        {
        for (entity charRender : m_entities)
        {
            charRender.Render(canvas, screenWindow, worldWindow);
        }
        }
        catch (ConcurrentModificationException exception)
        {

        }
    }

    public void findCharsInRange(Vector2f pos, float range, character.EFaction faction, List<character> foundChars)
    {
        float sqrRange = range*range;
        for (entity ent : m_entities)
        {
            if (character.class.isAssignableFrom(ent.getClass()))
            {
                character charCur = (character)ent;
                if ((ent.m_pos.distanceSquared(pos) < sqrRange)
                    && !charCur.IsDead()
                    && (charCur.m_faction == faction))
                {
                    foundChars.add(charCur);
                }
            }
        }
    }


    public void Update(float timeInSecs, World world)
    {
        List<entity> deleteList = null;
        for (entity charUpdate : m_entities)
        {
            charUpdate.Update(timeInSecs, world);
            boolean delete = charUpdate.DeleteMe();
            if (!delete && charUpdate.m_isSpawned)
            {
                delete = (charUpdate.m_pos.distanceSquared(world.m_worldWindow) > m_despawnDistSqr);
            }
            if (delete)
            {
                if (deleteList == null)
                {
                    deleteList = new ArrayList<entity>();
                }
                deleteList.add(charUpdate);
            }
        }

        for (entity charSpawn : m_spawnEntities)
        {
            m_entities.add(0, charSpawn);
        }
        m_spawnEntities.clear();

        if (m_numSpawned < m_maxSpawn)
        {
            RectF spawnRegion = new RectF();
            if (Math.abs(world.m_player.m_vel.x) > Math.abs(world.m_player.m_vel.y))
            {
                spawnRegion.top = world.m_worldWindow.top;
                spawnRegion.bottom = world.m_worldWindow.bottom;
                if (world.m_player.m_vel.x > 0.0f)
                {
                    spawnRegion.left = world.m_worldWindow.right;
                    spawnRegion.right = spawnRegion.left + m_spawnRange;
                }
                else
                {
                    spawnRegion.right = world.m_worldWindow.left;
                    spawnRegion.left = spawnRegion.right - m_spawnRange;
                }
            }
            else
            {
                spawnRegion.left = world.m_worldWindow.left;
                spawnRegion.right = world.m_worldWindow.right;
                if (world.m_player.m_vel.y > 0.0f)
                {
                    spawnRegion.top = world.m_worldWindow.bottom;
                    spawnRegion.bottom = spawnRegion.top + m_spawnRange;
                }
                else
                {
                    spawnRegion.bottom = world.m_worldWindow.top;
                    spawnRegion.top = spawnRegion.bottom - m_spawnRange;
                }
            }

            Vector2f newPos = new Vector2f();
            newPos.setRandom(spawnRegion);

            if (!world.m_terrain.IsBlocked(newPos))
            {
                float spawnNum = (float)Math.random()*(float)m_spawnTotal;
                for (spawnParams params : m_spawnables)
                {
                    spawnNum -= params.chance;
                    if (spawnNum <= 0)
                    {
                        character newChar = null;
                        if (params.charType == spawnParams.ECharacterType.Archer)
                        {
                            newChar = new AIArcher();
                            newChar.m_faction = character.EFaction.Bad;
                        }
                        else
                        {
                            newChar = new AICharacter();
                        }
                        newChar.m_drawable = params.gfx;
                        newChar.m_pos.set(newPos);
                        newChar.m_isSpawned = true;
                        m_entities.add(newChar);
                        m_numSpawned++;

                        break;
                    }
                }

/*                AICharacter newAI = new AICharacter();
                newAI.m_drawable = m_spawnTemplate;
                newAI.m_pos.set(newPos);
                newAI.m_isSpawned = true;
                m_entities.add(newAI);
                m_numSpawned++;
                */
            }
        }

        if (deleteList != null)
        {
            for (entity charDelete : deleteList)
            {
                if (charDelete.m_isSpawned)
                    m_numSpawned--;
                m_entities.remove(charDelete);
            }
        }
    }

    public void addSpawnable(spawnParams newSpawner)
    {
        m_spawnables.add(newSpawner);
        m_spawnTotal += newSpawner.chance;
    }

    public void addCollectable(collectable collect)
    {
        m_spawnEntities.add(collect);
    }

    public void addEntity(entity ent)
    {
        m_spawnEntities.add(ent);
    }

}
