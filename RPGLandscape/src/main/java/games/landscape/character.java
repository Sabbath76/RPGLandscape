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
public class character
{
    public Vector2f m_pos = new Vector2f();
    public Vector2f m_vel = new Vector2f();
    public float    m_facing = 0.0f;
    public final float  TURN_SPEED = 6.0f;
    public int m_shadowColour = 0x80000000;

    static public drawable s_defaultDeath;

    public drawable m_drawable;
    public drawable m_death = s_defaultDeath;

    public float m_shadowAngle = 135.0f;

    public boolean m_isSpawned = false;
    public boolean m_isDead = false;
    protected boolean m_deleteMe = false;
    public boolean m_canBeDeleted = true;
    public boolean m_hasCollision = true;
    public float m_radius = 0.2f;

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
        if (m_isDead)
        {
            stepTime += timePassed;
            if ((m_death == null) || (stepTime > (m_death.m_timePerFrame * m_death.m_numFrames)))
            {
                m_deleteMe = true;
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
        int timeI = (int)(stepTime/curDraw.m_timePerFrame)%curDraw.m_numFrames;

        if (curDraw.m_pingPong)
        {
            final int effectiveFrames = (curDraw.m_numFrames*2)-2;
            timeI = (int)(stepTime/curDraw.m_timePerFrame)%effectiveFrames;
            if (timeI >= curDraw.m_numFrames)
                timeI = effectiveFrames - timeI;
        }
        int frameWidth = curDraw.m_bitmap.getWidth() / curDraw.m_numFrames;
        int frameHeight = curDraw.m_bitmap.getHeight() / curDraw.m_numDirections;
        int dirFrame = 0;

        int direction = (int)(((m_facing + Math.PI) * curDraw.m_numDirections / (2.0f * Math.PI)) + 0.5f);
        if (curDraw.m_numDirections == 8)
        {
            final int directions[] = {1, 6, 3, 7, 2, 5, 0, 4};
            dirFrame = directions[direction%8];
        }
        else if (curDraw.m_numDirections == 4)
        {
            final int directions[] = {1, 3, 2, 0};
            dirFrame = directions[direction%4];
        }

        Rect srcRect = new Rect(timeI*frameWidth, dirFrame*frameHeight, (timeI+1)*frameWidth, (dirFrame+1)*frameHeight);
        Matrix orig = canvas.getMatrix();
        Matrix renderTran = new Matrix();
        if (curDraw.m_rootAtBase)
            renderTran.setTranslate(-(frameWidth / 2), -frameHeight+20.0f);
        else
            renderTran.setTranslate(-(frameWidth / 2), -(frameHeight / 2));
        if (curDraw.m_rotateToFacing)
            renderTran.postRotate((float) Math.toDegrees(m_facing) + curDraw.m_renderAngle);

        Matrix renderShadow = new Matrix();
        renderShadow.set(renderTran);
        if (curDraw.m_rotateToFacing)
        {
            renderShadow.postTranslate(px+14, py+20);
        }
        else
        {
            renderShadow.postSkew(-0.5f, 0.0f);
            renderShadow.postScale(curDraw.m_shadowScale, -0.75f*curDraw.m_shadowScale);
            renderShadow.postTranslate(px, py);
        }
//        renderShadow.postRotate(m_shadowAngle);
        Paint shadow = new Paint();
        shadow.setColorFilter(new PorterDuffColorFilter(m_shadowColour, PorterDuff.Mode.MULTIPLY));
        canvas.setMatrix(renderShadow);
//        canvas.drawBitmap(m_bitmap, renderTran, shadow);
        canvas.drawBitmap(curDraw.m_bitmap, srcRect, new Rect(0, 0, frameWidth, frameHeight), shadow);

        renderTran.postTranslate(px, py);
        canvas.setMatrix(renderTran);
        canvas.drawBitmap(curDraw.m_bitmap, srcRect, new Rect(0, 0, frameWidth, frameHeight), null);
        canvas.setMatrix(orig);
//        canvas.drawBitmap(m_bitmap, renderTran, null);
    }

    public void Hit(float damage)
    {
        m_isDead = true;
        stepTime = 0.0f;
        SoundManager.Get().PlaySound(SoundManager.ESoundType.Hit, m_pos);
    }

    public void CanBeDeleted(boolean canBeDeleted)
    {
        m_canBeDeleted = canBeDeleted;
    }

    public boolean DeleteMe()
    {
        return m_canBeDeleted && m_deleteMe;
    }
}
