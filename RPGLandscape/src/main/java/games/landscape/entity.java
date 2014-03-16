package games.landscape;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Tom on 19/07/13.
 */
public class entity
{
    public Vector2f m_pos = new Vector2f();
    public drawable m_drawable;
    private float m_time = 0.0f;
    protected boolean m_deleteMe = false;
    public boolean m_canBeDeleted = true;
    public boolean m_isSpawned = false;

    void Update(float timePassed, World world)
    {
        m_time += timePassed;
    }

    void Render(Canvas canvas, Rect screenWindow, RectF worldWindow)
    {
        float ratio = (float)screenWindow.width() / worldWindow.width();
        float nx = (m_pos.x - worldWindow.left) * ratio;
        float ny = (m_pos.y - worldWindow.top) * ratio;
        float px = screenWindow.left + nx;
        float py = screenWindow.top + ny;

        drawable curDraw = m_drawable;

        curDraw.Render(m_time, canvas, 0.0f, px, py);
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