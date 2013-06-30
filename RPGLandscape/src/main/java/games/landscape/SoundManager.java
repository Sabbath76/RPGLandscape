package games.landscape;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

/**
 * Created by Tom on 09/06/13.
 */
public class SoundManager
{
    enum ESoundType
    {
        Hit,
        FireArrow
    };

    static SoundManager s_soundManager = new SoundManager();
    static SoundManager Get()
    {
        return s_soundManager;
    }

    private SoundPool m_soundPool = null;
    private int m_soundMap[][] = new int[2][];
    private World m_world = null;

    void Init(Context context, World world)
    {
        m_soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
        m_world = world;

        int hitSfx[] = new int[3];
        hitSfx[0] = m_soundPool.load(context, R.raw.body_hit_1, 1);
        hitSfx[1] = m_soundPool.load(context, R.raw.body_hit_2, 1);
        hitSfx[2] = m_soundPool.load(context, R.raw.body_hit_3, 1);

        int arrowSfx[] = new int[1];
        arrowSfx[0] = m_soundPool.load(context, R.raw.bow_fire, 1);

        m_soundMap[ESoundType.Hit.ordinal()] = hitSfx;
        m_soundMap[ESoundType.FireArrow.ordinal()] = arrowSfx;
    }

    void PlaySound(ESoundType soundType, Vector2f pos)
    {
        final int type = soundType.ordinal();
        final int numOptions = m_soundMap[type].length;
        int option = (int) (Math.random() * numOptions);

        float t = (pos.x - m_world.m_worldWindow.left) / m_world.m_worldWindow.width();
        t = Math.min(Math.max(t, 0.0f), 1.0f);
        float left = 1.0f - t;
        float right = t;

        m_soundPool.play(m_soundMap[type][option], left, right, 1, 0, 1.0f);
    }
}
