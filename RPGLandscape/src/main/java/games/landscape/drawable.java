package games.landscape;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 08/06/13.
 */
public class drawable
{
    public String   m_name = "Drawable";
    public Bitmap   m_bitmap;
    public int      m_numFrames = 1;
    public int      m_numDirections = 1;
    public float    m_renderAngle = 0.0f;
    public boolean  m_rootAtBase = false;
    public boolean  m_rotateToFacing = false;
    public boolean  m_pingPong = false;
    public float    m_timePerFrame = 1.0f;
    public float    m_shadowScale = 1.0f;
    public int      m_shadowColour = 0x80000000;

    drawable(String name)
    {
        m_name = name;
        Register(this);
    }

    public String toString()
    {
        return m_name;
    }

    public void Render(float time, Canvas canvas, float facing, float px, float py)
    {
        int timeI = (int)(time/m_timePerFrame)%m_numFrames;

        if (m_pingPong)
        {
            final int effectiveFrames = (m_numFrames*2)-2;
            timeI = (int)(time/m_timePerFrame)%effectiveFrames;
            if (timeI >= m_numFrames)
                timeI = effectiveFrames - timeI;
        }
        int frameWidth = m_bitmap.getWidth() / m_numFrames;
        int frameHeight = m_bitmap.getHeight() / m_numDirections;
        int dirFrame = 0;

        int direction = (int)(((facing + Math.PI) * m_numDirections / (2.0f * Math.PI)) + 0.5f);
        if (m_numDirections == 8)
        {
            final int directions[] = {1, 6, 3, 7, 2, 5, 0, 4};
            dirFrame = directions[direction%8];
        }
        else if (m_numDirections == 4)
        {
            final int directions[] = {1, 3, 2, 0};
            dirFrame = directions[direction%4];
        }

        Rect srcRect = new Rect(timeI*frameWidth, dirFrame*frameHeight, (timeI+1)*frameWidth, (dirFrame+1)*frameHeight);
        Matrix orig = canvas.getMatrix();
        Matrix renderTran = new Matrix();
        if (m_rootAtBase)
            renderTran.setTranslate(-(frameWidth / 2), -frameHeight+20.0f);
        else
            renderTran.setTranslate(-(frameWidth / 2), -(frameHeight / 2));
        if (m_rotateToFacing)
            renderTran.postRotate((float) Math.toDegrees(facing) + m_renderAngle);

        Matrix renderShadow = new Matrix();
        renderShadow.set(renderTran);
        if (m_rotateToFacing)
        {
            renderShadow.postTranslate(px+14, py+20);
        }
        else
        {
            renderShadow.postSkew(-0.5f, 0.0f);
            renderShadow.postScale(m_shadowScale, -0.75f*m_shadowScale);
            renderShadow.postTranslate(px, py);
        }
//        renderShadow.postRotate(m_shadowAngle);
        Paint shadow = new Paint();
        shadow.setColorFilter(new PorterDuffColorFilter(m_shadowColour, PorterDuff.Mode.MULTIPLY));
        canvas.setMatrix(renderShadow);
//        canvas.drawBitmap(m_bitmap, renderTran, shadow);
        canvas.drawBitmap(m_bitmap, srcRect, new Rect(0, 0, frameWidth, frameHeight), shadow);

        renderTran.postTranslate(px, py);
        canvas.setMatrix(renderTran);
        canvas.drawBitmap(m_bitmap, srcRect, new Rect(0, 0, frameWidth, frameHeight), null);
        canvas.setMatrix(orig);
//        canvas.drawBitmap(m_bitmap, renderTran, null);
    }

    static List<drawable> s_items = new ArrayList<drawable>();
    static void Register(drawable item)
    {
        s_items.add(item);
    }
    public static drawable s_health = null;
    public static drawable s_money = null;
}
