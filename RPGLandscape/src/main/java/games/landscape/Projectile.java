package games.landscape;

import android.graphics.Bitmap;

/**
 * Created by Tom on 07/06/13.
 */
public class Projectile extends character
{
    character m_target = null;
    character m_from = null;
    float m_speed = 4.0f;
    float m_hitDist = 0.1f;
    float m_damage = 1.0f;

    Projectile(character from, character target, drawable draw, float scale)
    {
        m_target = target;
        m_from = from;

        m_drawable = draw;
        m_hasCollision = false;

        m_pos.set(m_from.m_pos);
        m_pos.y -= 0.1f;
        m_vel.subtract(target.m_pos, m_pos);
        m_facing = m_vel.getAngle();
        m_pos.addScaled(m_vel, draw.m_bitmap.getWidth() * scale / m_vel.length());
    }

    void Update(float timePassed, World world)
    {
        m_vel.subtract(m_target.m_pos, m_pos);
        float dist = m_vel.length();

        Vector2f oldPos = new Vector2f();
        oldPos.set(m_pos);

        if (dist > 0.01f)
        {
            m_vel.scale(m_speed / dist);
        }

        super.Update(timePassed, world);

        if (m_target.m_pos.distFromLine(oldPos, m_pos) < m_target.m_radius)
        {
            m_target.Hit(m_damage);
            m_deleteMe = true;
        }
    }

}
