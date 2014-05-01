package games.landscape;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Tom on 01/06/13.
 */

public class character extends entity
{
    public Vector2f m_vel = new Vector2f();
    public float    m_facing = 0.0f;
    public final float  TURN_SPEED = 6.0f;
    public int m_shadowColour = 0x80000000;

    public enum EFaction
    {
        Good,
        Bad,
        Neutral
    };

    EFaction m_faction = EFaction.Neutral;

    static public drawable s_defaultDeath;

    public drawable m_death = s_defaultDeath;

    public float m_shadowAngle = 135.0f;

    public boolean m_isDead = false;
    public boolean m_hasCollision = true;
    public float m_radius = 0.2f;
    public float m_health = 1.0f;

    boolean isEnemy(character target)
    {
        if ((target.m_faction == EFaction.Good) && (m_faction == EFaction.Bad))
            return true;
        if ((target.m_faction == EFaction.Bad) && (m_faction == EFaction.Good))
            return true;

        return false;
    }

    float wrapAngle(float angle)
    {
        if (angle > Math.PI)
        {
            return angle - ((float)Math.PI * 2.0f);
        }
        if (angle < -Math.PI)
        {
            return angle + ((float)Math.PI * 2.0f);
        }
        return angle;
    }

    boolean IsDead()
    {
        return m_isDead;
    }

    float stepTime = 0.0f;
    void Update(float timePassed, World world)
    {
        if (timePassed > 0.4f)
        {
            int n=0;
        }
        if (m_isDead)
        {
            stepTime += timePassed;
            if ((m_death == null) || (stepTime > (m_death.m_timePerFrame * m_death.m_numFrames)))
            {
                m_deleteMe = true;
                collectable collect = new collectable((Math.random() > 0.5) ? collectable.collectableType.Health : collectable.collectableType.Money, 1.0f);
                collect.m_pos.set(m_pos);
                world.m_entityManager.addCollectable(collect);
            }
        }
        else
        {
            Vector2f newPos = new Vector2f(m_pos, m_vel, timePassed);
            if (m_hasCollision)
            {
                world.m_terrain.CheckCollision(m_pos, newPos);
            }
            m_pos = newPos;

            if (m_pos.y < 0.0f)
            {
                int k=0;
            }

            stepTime += m_vel.length() * timePassed;

            if (m_vel.lengthSquared() > 0.001f)
            {
                float newAngle = m_vel.getAngle();
                float delta = wrapAngle(newAngle - m_facing);
                float step = TURN_SPEED * timePassed;
                if (Math.abs(delta) < step)
                {
                    m_facing = newAngle;
                }
                else if (delta > 0.0f)
                {
                    m_facing = wrapAngle(m_facing + step);
                }
                else
                {
                    m_facing = wrapAngle(m_facing - step);
                }
            }
        }
    }

    void Render(Canvas canvas, Rect screenWindow, RectF worldWindow)
    {
        float ratio = (float)screenWindow.width() / worldWindow.width();
        float nx = (m_pos.x - worldWindow.left) * ratio;
        float ny = (m_pos.y - worldWindow.top) * ratio;
        float px = screenWindow.left + nx;
        float py = screenWindow.top + ny;

        drawable curDraw = m_drawable;

        if (m_isDead && (m_death != null))
        {
            curDraw = m_death;
        }

        curDraw.Render(stepTime, canvas, m_facing, px, py);
    }

    public void Hit(float damage)
    {
        m_health -= damage;
        SoundManager.Get().PlaySound(SoundManager.ESoundType.Hit, m_pos);
        if (m_health <= 0.0f)
        {
            m_isDead = true;
            stepTime = 0.0f;
            SoundManager.Get().PlaySound(SoundManager.ESoundType.Death, m_pos);
        }
    }

    public void AddHealth(float add)
    {
        m_health += add;
    }
}
