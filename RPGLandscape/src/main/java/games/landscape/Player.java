package games.landscape;

import android.graphics.Bitmap;

/**
 * Created by Tom on 07/06/13.
 */
public class Player extends character
{
    void Update(float timePassed, World world)
    {
        super.Update(timePassed, world);
    }

    Projectile LaunchProjectile(character target, float aimTime, drawable draw, float scale)
    {
        Projectile proj = new Projectile(this, target, draw, scale);

        SoundManager.Get().PlaySound(SoundManager.ESoundType.FireArrow, m_pos);

        return proj;

     }
}
