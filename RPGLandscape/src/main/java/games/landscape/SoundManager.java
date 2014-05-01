package games.landscape;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by Tom on 09/06/13.
 */
public class SoundManager
{
    enum ESoundType
    {
        Music,
        BirdCalls,
        Hit,
        Death,
        FireArrow,
        GetCoins,
        GetHealth
    };

    static SoundManager s_soundManager = new SoundManager();
    static SoundManager Get()
    {
        return s_soundManager;
    }

    private SoundPool m_soundPool = null;
    private int m_soundMap[][] = new int[ESoundType.values().length][];
    private World m_world = null;
    private MediaPlayer m_mediaPlayer = null;
    private Context m_context = null;

    long startTime = 0;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run()
        {
            int nextTime = 1000+(World.Get().m_random.nextInt()%8000);
            SoundManager.Get().PlaySound(ESoundType.BirdCalls);
            timerHandler.postDelayed(this, nextTime);
        }
    };

    void Init(Context context, World world)
    {
        m_soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 100);
        m_world = world;
        m_context = context;

        int musicSfx[] = new int[1];
        musicSfx[0] = R.raw.goldenporsche;

        int birdCallSfx[] = new int[3];
        birdCallSfx[0] = m_soundPool.load(context, R.raw.birds_01, 1);
        birdCallSfx[1] = m_soundPool.load(context, R.raw.bird_chirps5, 1);
        birdCallSfx[2] = m_soundPool.load(context, R.raw.house_wren, 1);

        int hitSfx[] = new int[3];
        hitSfx[0] = m_soundPool.load(context, R.raw.body_hit_1, 1);
        hitSfx[1] = m_soundPool.load(context, R.raw.body_hit_2, 1);
        hitSfx[2] = m_soundPool.load(context, R.raw.body_hit_3, 1);

        int deathSfx[] = new int[2];
        deathSfx[0] = m_soundPool.load(context, R.raw.deathcry, 1);
        deathSfx[1] = m_soundPool.load(context, R.raw.man_die, 1);

        int arrowSfx[] = new int[1];
        arrowSfx[0] = m_soundPool.load(context, R.raw.bow_fire, 1);

        int getCoin[] = new int[3];
        getCoin[0] = m_soundPool.load(context, R.raw.money_pickup, 1);
        getCoin[1] = m_soundPool.load(context, R.raw.money_pickup_2, 1);
        getCoin[2] = m_soundPool.load(context, R.raw.money_drop, 1);

        int getHealth[] = new int[2];
        getHealth[0] = m_soundPool.load(context, R.raw.heartbeat_speeding_up_01, 1);
        getHealth[1] = m_soundPool.load(context, R.raw.heartbeat_speeding_up_02, 1);

        m_soundMap[ESoundType.Music.ordinal()] = musicSfx;
        m_soundMap[ESoundType.BirdCalls.ordinal()] = birdCallSfx;
        m_soundMap[ESoundType.Hit.ordinal()] = hitSfx;
        m_soundMap[ESoundType.Death.ordinal()] = deathSfx;
        m_soundMap[ESoundType.FireArrow.ordinal()] = arrowSfx;
        m_soundMap[ESoundType.GetCoins.ordinal()] = getCoin;
        m_soundMap[ESoundType.GetHealth.ordinal()] = getHealth;

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    void PlaySound(ESoundType soundType, Vector2f pos)
    {
        final int type = soundType.ordinal();
        if (m_soundMap[type] != null)
        {
        final int numOptions = m_soundMap[type].length;
        int option = (int) (Math.random() * numOptions);

        float t = (pos.x - m_world.m_worldWindow.left) / m_world.m_worldWindow.width();
        t = Math.min(Math.max(t, 0.0f), 1.0f);
        float left = 1.0f - t;
        float right = t;

        m_soundPool.play(m_soundMap[type][option], left, right, 1, 0, 1.0f);
        }
    }

    void PlaySound(ESoundType soundType)
    {

        final int type = soundType.ordinal();

        if (m_soundMap[type] != null)
        {
            final int numOptions = m_soundMap[type].length;
            int option = (int) (Math.random() * numOptions);

            if (soundType == ESoundType.Music)
            {
                m_mediaPlayer = MediaPlayer.create(m_context, m_soundMap[type][option]);
                if (m_mediaPlayer != null)
                {
                    m_mediaPlayer.setLooping(true);
                    m_mediaPlayer.start();
                }
            }
            else
            {
                float left = 1.0f;
                float right = 1.0f;

                m_soundPool.play(m_soundMap[type][option], left, right, 1, 0, 1.0f);
            }
        }
    }
}
