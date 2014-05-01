package games.landscape;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.SurfaceHolder;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

enum EMode
{
    Move,
    Pan,
    Paint,
    Edit,
};

class AimAction
{
    character target;
    float aimTime;
    int pointerId;
    public boolean fire = false;
}

public class LandscapeThread extends Thread
{
    private final drawable human;
    private final drawable platypus;
    /** Indicate whether the surface has been created & is ready to draw */
    private boolean m_run = false;
    private Object mPauseLock = new Object();
    private boolean mPaused;

    /** Message handler used by thread to interact with TextView */
//        private Handler mHandler;

    /** Handle to the surface manager object we interact with */
    private SurfaceHolder mSurfaceHolder;

    /** Handle to the application context, used to e.g. fetch Drawables. */
    private Context mContext;
    Resources mRes;

    public World m_world = new World();

    private Bitmap mSword;
    private Bitmap mPlatypus;
    public final drawable mArrow;
//    private Bitmap mBackgroundImageFar;
//    private Bitmap mBackgroundImageNear;

    private int mCanvasWidth = 1;
    private int mCanvasHeight = 1;

    private long mLastTimeMS = 0;

    public float mPosX = 3.0f;
    public float mPosY = 4.0f;
    public float mGridSize = 100.0f;
    public float mSpeedX = 0.0f;
    public float mSpeedY = 0.0f;
    public float m_zoomSpeed = 0.0f;
    
    public float m_zoomLevel = 0.5f;

    public boolean m_renderBuffered = true;
    public boolean m_renderSmoothed = true;

    public float m_holdTime = 0.0f;
    public EMode m_mode = EMode.Move;
    public boolean m_panning = false;
    public boolean m_zoom = false;
    public boolean m_painting = false;
    public ETerrainType m_paintType = ETerrainType.Grass;
    PointerCoords m_dragStart = new PointerCoords();

    Vector2f m_moveSpeed = new Vector2f();
    private boolean m_moving = false;

    private List<AimAction> m_aims = new ArrayList<AimAction>();

    private float m_minSpeed = 0.5f;
    private float m_maxSpeed = 3.0f;
    private float m_innerRadius = 0.15f;
    private float m_outerRadius = 0.90f;

    private float m_lastUpdateTime = 0.0f;
    private float m_lockOnTime = 0.25f;
    private float m_chargeTime = 1.0f;
    private float m_dispTargetRadius = 65.0f;
    private float m_dispTargetLW = 6.0f;
    private int m_movePointerId = -1;
    private Quest m_ActiveQuest = null;
    private boolean m_renderBrushed = false;

    public LandscapeThread(SurfaceHolder surfaceHolder, Context context) 
    {
    	mSurfaceHolder = surfaceHolder;
        mContext = context;
        mRes = context.getResources();

        m_world.m_entityManager = new EntityManager();
        m_world.m_terrain = new Terrain(context);

        World.Set(m_world);

        SoundManager.Get().Init(context, m_world);

        m_world.m_screenWindow = new Rect(50, 50, 600, 1000);
        float width  = (float)m_world.m_screenWindow.width() / mGridSize;
        float height = (float)m_world.m_screenWindow.height() / mGridSize;
        m_world.m_worldWindow  = new RectF(mPosX-width*0.5f, mPosY-height*0.5f, mPosX+width*0.5f, mPosY+height*0.5f);

        Bitmap sword = BitmapFactory.decodeResource(mRes, R.drawable.sword_render);
        mSword = Bitmap.createScaledBitmap(sword, sword.getWidth() / 4, sword.getHeight() / 4, true);

        Bitmap arrow = BitmapFactory.decodeResource(mRes, R.drawable.arrow);
        mArrow = new drawable("arrow");
        mArrow.m_bitmap = Bitmap.createScaledBitmap(arrow, arrow.getWidth() / 2, arrow.getHeight()*2, true);
        mArrow.m_rotateToFacing = true;

        Bitmap platypusBMP = BitmapFactory.decodeResource(mRes, R.drawable.platmain2);
        mPlatypus = Bitmap.createScaledBitmap(platypusBMP, platypusBMP.getWidth() / 3, platypusBMP.getHeight() / 3, true);

        human = new drawable("human");
        human.m_bitmap = BitmapFactory.decodeResource(mRes, R.drawable.actor1m);
        human.m_numFrames = 4;
        human.m_numDirections = 8;
        human.m_timePerFrame = 0.4f;
        human.m_rootAtBase = true;

        Bitmap bloodBMP = BitmapFactory.decodeResource(mRes, R.drawable.blood);
        drawable blood = new drawable("blood");
        blood.m_bitmap = Bitmap.createScaledBitmap(bloodBMP, bloodBMP.getWidth()/2, bloodBMP.getHeight()/2, true);
        blood.m_numFrames = 6;
        blood.m_timePerFrame = 0.04f;

        character.s_defaultDeath = blood;


        Bitmap healthBMP = BitmapFactory.decodeResource(mRes, R.drawable.heart);
        drawable health = new drawable("health");
        health.m_bitmap = healthBMP;//Bitmap.createScaledBitmap(healthBMP, healthBMP.getWidth()/2, healthBMP.getHeight()/2, true);
        health.m_rotateToFacing = true;
        health.m_shadowOffsX = 8;
        health.m_shadowOffsY = 8;

        drawable.s_health = health;

        Bitmap coinsBMP = BitmapFactory.decodeResource(mRes, R.drawable.coinpile);
        drawable coins = new drawable("coins");
        coins.m_bitmap = Bitmap.createScaledBitmap(coinsBMP, coinsBMP.getWidth()/2, coinsBMP.getHeight()/2, true);
        coins.m_rotateToFacing = true;
        drawable.s_money = coins;

        Bitmap koboldBMP = BitmapFactory.decodeResource(mRes, R.drawable.kobold);
        drawable kobold = new drawable("kobold");
        kobold.m_bitmap = Bitmap.createScaledBitmap(koboldBMP, koboldBMP.getWidth()*2, koboldBMP.getHeight()*2, true);
        kobold.m_numFrames = 3;
        kobold.m_pingPong = true;
        kobold.m_numDirections = 4;
        kobold.m_timePerFrame = 0.07f;
        kobold.m_rootAtBase = true;
        kobold.m_shadowScale = 1.2f;

        platypus = new drawable("platypus");
        platypus.m_bitmap = mPlatypus;
        platypus.m_numFrames = 1;
        platypus.m_numDirections = 1;
        platypus.m_renderAngle = 200.0f;
        platypus.m_rotateToFacing = true;

        m_world.m_player = new Player(health);
        m_world.m_player.m_pos.x = mPosX;
        m_world.m_player.m_pos.y = mPosY;

        m_world.m_player.m_drawable = human;
        m_world.m_entityManager.AddCharacter(m_world.m_player);

        character mob = new character();
        mob.m_drawable = platypus;
        mob.m_pos.x = mPosX + 6.0f;
        mob.m_pos.y = mPosY - 1.0f;
        m_world.m_entityManager.AddCharacter(mob);

        spawnParams koboldParams = new spawnParams();
        koboldParams.gfx = kobold;
        koboldParams.chance = 8;
        koboldParams.charType = spawnParams.ECharacterType.Normal;
        m_world.m_entityManager.addSpawnable(koboldParams);

        spawnParams archerParams = new spawnParams();
        archerParams.gfx = human;
        archerParams.chance = 1;
        archerParams.charType = spawnParams.ECharacterType.Archer;
        m_world.m_entityManager.addSpawnable(archerParams);
    }

    public void SetupQuests()
    {
        if (Quest.s_root.size() == 0)
        {
            Quest getWeapon = new Quest("GetWeapon", "Arm yourself", Quest.Condition.Kill);
            TargetChar tgt = new TargetChar();
            tgt.spawnParams = human;
            tgt.spawn = true;
            tgt.spawnPos = new Vector2f(3.0f, 3.0f);
            getWeapon.AddTarget(tgt);
            Quest killATotemAnimal = new Quest("KillTotem", "Kill a totem animal", Quest.Condition.SubQuestAny);
            Quest killBird = new Quest("KillBird", "Kill a bird", Quest.Condition.Kill);
            tgt = new TargetChar();
            tgt.spawn = true;
            tgt.spawnParams = platypus;
            tgt.spawnPos = new Vector2f(5.0f, 4.0f);
            killBird.AddTarget(tgt);
            Quest killDeer = new Quest("KillDeer", "Kill a deer", Quest.Condition.Kill);
            tgt = new TargetChar();
            tgt.spawn = true;
            tgt.spawnParams = platypus;
            tgt.spawnPos = new Vector2f(6.5f, 2.0f);
            killDeer.AddTarget(tgt);
            Quest killBear = new Quest("KillBear", "Kill a bear", Quest.Condition.Kill);
            tgt = new TargetChar();
            tgt.spawn = true;
            tgt.spawnParams = platypus;
            tgt.spawnPos = new Vector2f(7.2f, 3.0f);
            killBear.AddTarget(tgt);

            killATotemAnimal.AddSubQuest(killBird);
            killATotemAnimal.AddSubQuest(killDeer);
            killATotemAnimal.AddSubQuest(killBear);
            Quest tellGeoff = new Quest("TellGeoff", "Return To Geoff", Quest.Condition.Converse);

            Quest questInit = new Quest("Initiation", "Beginnings", Quest.Condition.SubQuestSequence);
            questInit.AddSubQuest(getWeapon);
            questInit.AddSubQuest(killATotemAnimal);

            questInit.AddSubQuest(tellGeoff);

            Quest.s_root.add(questInit);
        }

        m_ActiveQuest = Quest.s_root.get(0);
        m_ActiveQuest.Begin();
    }

    public void run() {

//        SoundManager.Get().PlaySound(SoundManager.ESoundType.Music);

        while (m_run) {
            Canvas c = null;

            updateGameState();

            try {
                c = mSurfaceHolder.lockCanvas(null);
                // synchronized (mSurfaceHolder) {
                doDraw(c);
                // }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }// end finally block
        }// end while m_run block
        synchronized (mPauseLock) {
            while (mPaused) {
                try {
                    mPauseLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
    
    /**
     * Add key press input to the GameEvent queue
     */
    public boolean doKeyDown(int keyCode, KeyEvent msg) 
    {
    	if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
    	{
    		if (m_zoom)
    		{
    			m_zoomSpeed = 1;
    		}
    		else
    		{
	    		mSpeedY = 1;
    		}
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
    	{
    		if (m_zoom)
    		{
    			m_zoomSpeed = -1;
    		}
    		else
    		{
	    		mSpeedY = -1;
    		}
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
    	{
    		mSpeedX = 1;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
    	{
    		mSpeedX = -1;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_ENTER)
    	{
    		m_zoom = !m_zoom;
    		mSpeedX = 0;
    		mSpeedY = 0;
    	}
        return true;
    }

    /**
     * Add key press input to the GameEvent queue
     */
    public boolean doKeyUp(int keyCode, KeyEvent msg) 
    {
    	if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
    	{
    		mSpeedY = 0;
    		m_zoomSpeed = 0.0f;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
    	{
    		mSpeedY = 0;
    		m_zoomSpeed = 0.0f;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
    	{
    		mSpeedX = 0;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
    	{
    		mSpeedX = 0;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_ENTER)
    	{
    		m_zoomSpeed = 0;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_0)
    	{
            m_world.m_terrain.doClipping = !m_world.m_terrain.doClipping;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_1)
    	{
            if (m_mode == EMode.Edit)
            {
                m_mode = EMode.Pan;
            }
            else
            {
                m_mode = EMode.Edit;
            }
    	}
    	else if (keyCode == KeyEvent.KEYCODE_2)
    	{
            if (m_mode == EMode.Paint)
            {
                m_mode = EMode.Pan;
            }
            else
            {
                m_mode = EMode.Paint;
            }
    		m_paintType = ETerrainType.Grass;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_9)
    	{
    		m_renderBuffered = !m_renderBuffered;
    	}

    	return true;
    }

    public void onGameTouchEvent(MotionEvent event)
    {
        Rect rc = mSurfaceHolder.getSurfaceFrame();
        PointerCoords pointCoords = new PointerCoords();
        Vector2f worldPos = new Vector2f();
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_POINTER_UP:
            {
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int newPointerID = event.getPointerId(pointerIndex);
                if (m_moving && (newPointerID == m_movePointerId))
                {
                    m_moving = false;
                }
                else
                {
                    for (AimAction aimAction : m_aims)
                    {
                        if (aimAction.pointerId == newPointerID)
                        {
                            aimAction.fire = true;
                            break;
                        }
                    }
                }
            }
                break;
            case MotionEvent.ACTION_UP:
                for (int pointerIdx = 0; pointerIdx < event.getPointerCount(); pointerIdx++)
                {
                    int newPointerID = event.getPointerId(pointerIdx);

                    if (m_moving && (newPointerID == m_movePointerId))
                    {
                        m_moving = false;
                    }
                    else
                    {
                        for (AimAction aimAction : m_aims)
                        {
                            if (aimAction.pointerId == newPointerID)
                            {
                                aimAction.fire = true;
                                break;
                            }
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
            {
                final int pointerIdx = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int newPointerID = event.getPointerId(pointerIdx);
                event.getPointerCoords(pointerIdx, pointCoords);
                m_world.ConvertScreen2World(pointCoords, worldPos);

                float selectSqrDist = m_world.m_player.getWeaponRange();
                selectSqrDist = selectSqrDist * selectSqrDist;

                final float PICK_DIST = 0.3f;
                character hitChar = m_world.m_entityManager.GetHit(worldPos, PICK_DIST);

                if ((hitChar != null) && (hitChar != m_world.m_player)
                        && (hitChar.m_pos.distanceSquared(m_world.m_player.m_pos) < selectSqrDist))
                {
                    AimAction newAim = new AimAction();
                    newAim.aimTime = 0.0f;
                    newAim.target = hitChar;
                    newAim.pointerId = newPointerID;
                    m_aims.add(newAim);
                }
                else if (!m_moving)
                {
                    m_moveSpeed.x = pointCoords.x - rc.centerX();
                    m_moveSpeed.y = pointCoords.y - rc.centerY();
                    m_movePointerId = newPointerID;
                    float len = m_moveSpeed.length();
                    if (len > 0.0f)
                    {
                        float velFactor = (((2.0f * len) / rc.width()) - m_innerRadius) * (m_outerRadius - m_innerRadius);
                        velFactor = StrictMath.min(velFactor, 1.0f);
                        velFactor = StrictMath.max(velFactor, 0.0f);
                        float newSpeed = (velFactor * (m_maxSpeed - m_minSpeed)) + m_minSpeed;
                        m_moveSpeed.scale(newSpeed / len);
                    }
                    m_moving = true;
                }
            }
            break;
            case MotionEvent.ACTION_DOWN:
                for (int pointerIdx = 0; pointerIdx < event.getPointerCount(); pointerIdx++)
                {
                    int newPointerID = event.getPointerId(pointerIdx);
//                    Log.d("Touch", "Down "+Integer.toString(newPointerID));
                    event.getPointerCoords(pointerIdx, pointCoords);
                    m_world.ConvertScreen2World(pointCoords, worldPos);

                    final float PICK_DIST = 0.3f;
                    character hitChar = m_world.m_entityManager.GetHit(worldPos, PICK_DIST);

                    if ((hitChar != null) && (hitChar != m_world.m_player))
                    {
                        AimAction newAim = new AimAction();
                        newAim.aimTime = 0.0f;
                        newAim.target = hitChar;
                        newAim.pointerId = newPointerID;
                        m_aims.add(newAim);
                    }
                    else if (!m_moving)
                    {
                        m_moveSpeed.x = pointCoords.x - rc.centerX();
                        m_moveSpeed.y = pointCoords.y - rc.centerY();
                        m_movePointerId = newPointerID;
                        float len = m_moveSpeed.length();
                        if (len > 0.0f)
                        {
                            float velFactor = (((2.0f * len) / rc.width()) - m_innerRadius) * (m_outerRadius - m_innerRadius);
                            velFactor = StrictMath.min(velFactor, 1.0f);
                            velFactor = StrictMath.max(velFactor, 0.0f);
                            float newSpeed = (velFactor * (m_maxSpeed - m_minSpeed)) + m_minSpeed;
                            m_moveSpeed.scale(newSpeed / len);
                        }
                        m_moving = true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (int pointerIdx = 0; pointerIdx < event.getPointerCount(); pointerIdx++)
                {
                    int newPointerID = event.getPointerId(pointerIdx);
  //                  Log.d("Touch", "Move "+Integer.toString(newPointerID));

                    if (m_moving && (newPointerID == m_movePointerId))
                    {
                        event.getPointerCoords(pointerIdx, pointCoords);

                        m_moveSpeed.x = pointCoords.x - m_world.m_screenWindow.centerX();
                        m_moveSpeed.y = pointCoords.y - m_world.m_screenWindow.centerY();
                        float len = m_moveSpeed.length();
                        if (len > 0.0f)
                        {
                            float velFactor = (((2.0f * len) / rc.width()) - m_innerRadius) * (m_outerRadius - m_innerRadius);
                            velFactor = StrictMath.min(velFactor, 1.0f);
                            velFactor = StrictMath.max(velFactor, 0.0f);
                            float newSpeed = (velFactor * (m_maxSpeed - m_minSpeed)) + m_minSpeed;
                            m_moveSpeed.scale(newSpeed / len);
                        }
                    }
                }
                break;
        }
    }

	public void onTouchEvent(MotionEvent event) 
	{
        if (m_mode == EMode.Move)
        {
            onGameTouchEvent(event);
        }
        else
        {
            Rect rc = mSurfaceHolder.getSurfaceFrame();
            switch (event.getAction()) {
                 case MotionEvent.ACTION_UP:
                     m_painting = false;
                     m_panning = false;
                     m_moving = false;
                     break;

                 case MotionEvent.ACTION_DOWN:
                     // process the mouse hover movement...
                     switch (m_mode)
                     {
                         case Paint:
                             m_painting = true;
                         case Edit:
                             PointerCoords clickPos = new PointerCoords();
                             event.getPointerCoords(0, clickPos);
                             if (m_world.m_screenWindow.contains((int)clickPos.x, (int)clickPos.y))
                             {
                                 Vector2f worldPos = new Vector2f();
                                 m_world.ConvertScreen2World(clickPos, worldPos);
                                 worldPos.x /= m_world.m_terrain.QUAD_SIZE;
                                 worldPos.y /= m_world.m_terrain.QUAD_SIZE;

                                 if (m_painting)
                                 {
                                     m_world.m_terrain.SetQuad((int)worldPos.x, (int)worldPos.y, m_paintType);
                                 }
                                 else
                                 {
                                     m_world.m_terrain.ToggleQuad((int)worldPos.x, (int)worldPos.y);
                                 }
                             }
                             break;
                         case Pan:
                             m_panning = true;
                             event.getPointerCoords(0, m_dragStart);
                             break;
                         case Move:
                             break;
                     }

                     break;

                 case MotionEvent.ACTION_MOVE:
                     // process the mouse hover movement...
                     if (m_panning)
                     {
                         PointerCoords clickPos = new PointerCoords();
                         event.getPointerCoords(0, clickPos);

                         float moveX = clickPos.x - m_dragStart.x;
                         float moveY = clickPos.y - m_dragStart.y;

                         if ((moveX != 0.0f)
                             || (moveY != 0.0f))
                         {
                             m_dragStart = clickPos;
                             mPosX += -moveX * (m_world.m_worldWindow.width() / m_world.m_screenWindow.width());
                             mPosY += -moveY * (m_world.m_worldWindow.height() / m_world.m_screenWindow.height());
                         }
                     }
                     else if (m_painting)
                     {
                         PointerCoords clickPos = new PointerCoords();
                         event.getPointerCoords(0, clickPos);
                         if (m_world.m_screenWindow.contains((int)clickPos.x, (int)clickPos.y))
                         {
                             Vector2f worldPos = new Vector2f();
                             m_world.ConvertScreen2World(clickPos, worldPos);
                             worldPos.x /= m_world.m_terrain.QUAD_SIZE;
                             worldPos.y /= m_world.m_terrain.QUAD_SIZE;

                             m_world.m_terrain.SetQuad((int)worldPos.x, (int)worldPos.y, m_paintType);
                         }
                     }
                     break;
                 }
        }
	 }

    private void updateGameState()
    {
        long currentTimeMS = System.nanoTime();
//    	long currentTimeMS = System.currentTimeMillis();
    	
//    	if (mLastTimeMS != 0)
    	{
    		long timePassedMS = (mLastTimeMS == 0) ? 0 : currentTimeMS - mLastTimeMS;
    		float timeInSecs = (float)((double)timePassedMS / 1000000000.0);

            m_lastUpdateTime = timeInSecs;

            timeInSecs = Math.min(timeInSecs, 0.2f);

            List<AimAction> delAims = new ArrayList<AimAction>();
            for (AimAction aimAction : m_aims)
            {
                aimAction.aimTime += timeInSecs;
                if (aimAction.fire)
                {
                    float scale = m_world.m_worldWindow.width() / m_world.m_screenWindow.width();
                    Projectile projectile = m_world.m_player.LaunchProjectile(aimAction.target, aimAction.aimTime, mArrow, scale);
                    m_world.m_entityManager.AddCharacter(projectile);
                    delAims.add(aimAction);
                }
            }
            for (AimAction aimAction : delAims)
            {
                m_aims.remove(aimAction);
            }
            if (m_moving)
            {
                mPosX += m_moveSpeed.x * timeInSecs;
                mPosY += m_moveSpeed.y * timeInSecs;
            }
    		float xInc = (mSpeedX * timeInSecs);
    		float yInc = (mSpeedY * timeInSecs);
	    	mPosX += xInc;
	    	mPosY += yInc;

            if (m_moving)
            {
                m_world.m_player.m_vel = m_moveSpeed;
            }
            else
            {
//                m_player.m_pos.x = mPosX;
//                m_player.m_pos.y = mPosY;
                m_world.m_player.m_vel.x = 0.0f;
                m_world.m_player.m_vel.y = 0.0f;
            }
            m_ActiveQuest.Update();
            m_world.m_entityManager.Update(timeInSecs, m_world);
            if (m_mode == EMode.Move)
            {
                mPosX = m_world.m_player.m_pos.x;
                mPosY = m_world.m_player.m_pos.y;
            }

            if (m_zoom)
	    	{
		    	float zoomInc = m_zoomSpeed * timeInSecs;

		    	m_zoomLevel += zoomInc;
		    	m_zoomLevel = Math.max(m_zoomLevel, 0.1f);
	    	}

            final float halfX  = m_zoomLevel * 0.5f * (float)m_world.m_screenWindow.width() / mGridSize;
            final float halfY = m_zoomLevel * 0.5f * (float)m_world.m_screenWindow.height() / mGridSize;
            m_world.m_worldWindow.left   = mPosX - halfX;
            m_world.m_worldWindow.right  = mPosX + halfX;
            m_world.m_worldWindow.top 	 = mPosY - halfY;
            m_world.m_worldWindow.bottom = mPosY + halfY;

    	}
    	mLastTimeMS = currentTimeMS;
    }
    private void doDraw(Canvas canvas) 
    {
        if (canvas != null)
        {
            if (m_renderBrushed)
            {
                m_world.m_terrain.RenderPainted(canvas, m_world.m_screenWindow, m_world.m_worldWindow);
            }
            else if (m_renderBuffered && !m_painting)
            {
                m_world.m_terrain.RenderBuffered(canvas, m_world.m_screenWindow, m_world.m_worldWindow, m_renderSmoothed);
            }
            else
            {
                m_world.m_terrain.Render(canvas, m_world.m_screenWindow, m_world.m_worldWindow, m_renderSmoothed);
            }

            m_world.m_entityManager.Render(canvas, m_world.m_screenWindow, m_world.m_worldWindow);

            for (AimAction aimAction : m_aims)
            {
                boolean lockedOn = (aimAction.aimTime > m_lockOnTime);
                Vector2f screenPos = new Vector2f();
                m_world.ConvertWorld2Screen(screenPos, aimAction.target.m_pos);
                int targetColour = lockedOn ? Color.RED : Color.LTGRAY;

                final float targetPts[] = {-1.5f,  0.0f, -1.0f,  0.0f, 1.5f,  0.0f, 1.0f,  0.0f,
                                            0.0f, -1.5f,  0.0f, -1.0f, 0.0f,  1.5f, 0.0f,  1.0f};
                float newPts[] = new float[16];
                float range = m_dispTargetRadius * (2.0f - Math.min(aimAction.aimTime / m_lockOnTime, 1.0f));
                for (int i =0; i<8; i++)
                {
                    newPts[2*i]     = (targetPts[2*i] * range) + screenPos.x;
                    newPts[2*i+1]   = (targetPts[2*i+1] * range) + screenPos.y;
                }

                Paint p = new Paint();
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(m_dispTargetLW);
                p.setColor(targetColour);
                canvas.drawLines(newPts, p);

                if (lockedOn)
                {
                    canvas.drawCircle(screenPos.x, screenPos.y, m_dispTargetRadius, p);
                }
            }

            float fps = 0.0f;
            if (m_lastUpdateTime > 0.0f)
            {
                fps = 1.0f / m_lastUpdateTime;
            }
            String fpsStr   = String.format("%.02f", fps);
//            String lastTime = String.format("%.02f", m_lastUpdateTime);
            String position = String.format("%.02f, %.02f", mPosX, mPosY);
            Paint paint = new Paint();
            paint.setTextSize(40.0f);
            paint.setColor(Color.GREEN);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(fpsStr, m_world.m_screenWindow.right-50, 50, paint);
//            canvas.drawText(lastTime, m_world.m_screenWindow.right-50, 80, paint);
            canvas.drawText(position, m_world.m_screenWindow.right-50, 110, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            drawString(canvas, paint, m_ActiveQuest.BuildDescription(), m_world.m_screenWindow.left+100, 200);
//            canvas.drawText(m_ActiveQuest.BuildDescription(), m_world.m_screenWindow.left+100, 200, paint);


//    	canvas.drawBitmap(mSword, m_screenWindow.centerX()-(mSword.getWidth()/2), m_screenWindow.centerY()-(mSword.getHeight()/2), null);

//        canvas.drawBitmap(mSword, m_screenWindow.centerX(), m_screenWindow.centerY(), null);

//    	canvas.drawBitmap(mBackgroundImageFar, 0, 0, null);
    	
//    	int radX = mBeanish.getWidth() / 2;
//    	int radY = mBeanish.getHeight() / 2;
//    	int posX = (int)mPosX;
//    	int posY = (int)mPosY;
//    	canvas.drawBitmap(mBeanish, posX-radX, posY-radY, null);
        }
    }

    Rect bounds = new Rect();
    void drawString(Canvas canvas, Paint paint, String str, int x, int y)
    {
        String[] lines = str.split("\n");

        int yoff = 0;
        for (int i = 0; i < lines.length; ++i) {
            canvas.drawText(lines[i], x, y + yoff, paint);
            paint.getTextBounds(lines[i], 0, lines[i].length(), bounds);
            yoff += bounds.height();
        }
    }

    /* Callback invoked when the surface dimensions change. */
    public void setSurfaceSize(int width, int height) {
        // synchronized to make sure these all change atomically
        synchronized (mSurfaceHolder) {
            mCanvasWidth = width;
            mCanvasHeight = height;

            m_world.m_screenWindow.top = 0;
            m_world.m_screenWindow.left = 0;
            m_world.m_screenWindow.bottom = height;
            m_world.m_screenWindow.right = width;

//            // don't forget to resize the background image
//            mBackgroundImageFar = Bitmap.createScaledBitmap(mBackgroundImageFar, width,
//                    height, true);
//
//            // don't forget to resize the background image
//            mBackgroundImageNear = Bitmap.createScaledBitmap(mBackgroundImageNear, width,
//                    height, true);
        }
    }

    /**
     * Used to signal the thread whether it should be running or not.
     * Passing true allows the thread to run; passing false will shut it
     * down if it's already running. Calling start() after this was most
     * recently called with false will result in an immediate shutdown.
     * 
     * @param b true to run, false to shut down
     */
    public void setRunning(boolean b) {
        m_run = b;
    }

    public void SetPlay()
    {
        m_mode = EMode.Move;
    }

    public void SetPaint(ETerrainType terrainType)
    {
        m_mode = EMode.Paint;
        m_paintType = terrainType;
    }

    public void SetPan()
    {
        m_mode = EMode.Pan;
    }


    // Two methods for your Runnable/Thread class to manage the thread properly.
    public void onPause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }
    public void onResume()
    {
        synchronized (mPauseLock)
        {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

    public void ToggleRenderPainted()
    {
        m_renderBrushed = !m_renderBrushed;
    }

    public void ToggleRenderBuffered()
    {
        m_renderBuffered = !m_renderBuffered;
    }

    public void ToggleRenderSmoothed()
    {
        m_renderSmoothed = !m_renderSmoothed;
    }

    public void ToggleRenderDebug()
    {
        m_world.m_terrain.ToggleDebug();
    }
}
