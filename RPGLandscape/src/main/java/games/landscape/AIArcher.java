package games.landscape;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 17/03/14.
 */
public class AIArcher extends AICharacter
{
    character m_target = null;
    float m_targetRange = 4.0f;
    float m_shootRangeMin = 1.0f;
    float m_shootRangeMax = 2.0f;
    float m_nextShootTime = 0.0f;

    void Update(float timePassed, World world)
    {
        if ((m_target != null) && m_target.IsDead())
        {
            m_target = null;
        }
        PickTarget(world);

        m_wander = m_target == null;
        if (!m_wander)
        {
            Vector2f offset = new Vector2f();
            offset.subtract(m_target.m_pos, m_pos);
            float distSqr = offset.lengthSquared();
            if (distSqr < (m_shootRangeMin * m_shootRangeMin))
            {
                //--- Back away
                float dist = (float)Math.sqrt(distSqr);
                m_targetPos.set(m_pos);
                m_targetPos.addScaled(offset, (dist - m_shootRangeMin)/dist);
                m_vel.subtract(m_targetPos, m_pos);
                m_vel.scale(m_wanderSpeed / Math.max(m_vel.length(), 1.0f));
            }
            else if (distSqr > (m_shootRangeMax * m_shootRangeMax))
            {
                //--- Close up
                float dist = (float)Math.sqrt(distSqr);
                m_targetPos.set(m_pos);
                m_targetPos.addScaled(offset, (dist - ((m_shootRangeMin+m_shootRangeMax) * 0.5f))/dist);
                m_vel.subtract(m_targetPos, m_pos);
                m_vel.scale(m_wanderSpeed / Math.max(m_vel.length(), 1.0f));
            }
            else
            {
                //--- Attack!
                m_nextShootTime -= timePassed;

                if (m_nextShootTime < 0.0f)
                {
                    m_nextShootTime += 1.0f + (Math.random() * 2.0f);

                    LaunchProjectile(m_target, 1.0f, drawable.s_items.get(0), 0.005f);
                }
            }
        }

        super.Update(timePassed, world);
    }

    void PickTarget(World world)
    {
        if ((m_target == null) && ((m_faction == EFaction.Good) || (m_faction == EFaction.Bad)))
        {
            List<character> foundTargets = new ArrayList<character>();
            world.m_entityManager.findCharsInRange(m_pos, 10.0f, (m_faction == EFaction.Good) ? EFaction.Bad : EFaction.Good, foundTargets);

            if (foundTargets.size() > 0)
            {
                m_target = foundTargets.get(0);
            }
        }
    }


    Projectile LaunchProjectile(character target, float aimTime, drawable draw, float scale)
    {
        Projectile proj = new Projectile(this, target, draw, scale);

        SoundManager.Get().PlaySound(SoundManager.ESoundType.FireArrow, m_pos);

        World.Get().m_entityManager.addEntity(proj);

        return proj;

    }

}
