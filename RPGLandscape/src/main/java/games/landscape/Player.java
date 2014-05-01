package games.landscape;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Tom on 07/06/13.
 */
public class Player extends character
{
    private float m_weaponRange = 10.0f;
    private float m_money = 0.0f;
    private drawable m_heart = null;

    public Player(drawable heart)
    {
        m_faction = EFaction.Good;
        m_health = 10.0f;
        m_heart = heart;
    }

    void Update(float timePassed, World world)
    {
        super.Update(timePassed, world);
    }

    void Render(Canvas canvas, Rect screenWindow, RectF worldWindow)
    {
        int numHearts = (int)m_health;
        for (int i=0; i<numHearts; i++)
        {
            m_heart.Render(m_time, canvas, 0.0f, i*40.0f, 40.0f);
        }

        super.Render(canvas, screenWindow, worldWindow);
    }

    Projectile LaunchProjectile(character target, float aimTime, drawable draw, float scale)
    {
        Projectile proj = new Projectile(this, target, draw, scale);

        SoundManager.Get().PlaySound(SoundManager.ESoundType.FireArrow, m_pos);

        return proj;

     }

    public float getWeaponRange()
    {
        return m_weaponRange;
    }

    public void AddMoney(float amount)
    {
        m_money += amount;
    }
}
