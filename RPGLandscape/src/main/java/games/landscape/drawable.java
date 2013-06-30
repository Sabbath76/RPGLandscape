package games.landscape;

import android.graphics.Bitmap;

/**
 * Created by Tom on 08/06/13.
 */
public class drawable
{
    public Bitmap   m_bitmap;
    public int      m_numFrames = 1;
    public int      m_numDirections = 1;
    public float    m_renderAngle = 0.0f;
    public boolean  m_rootAtBase = false;
    public boolean  m_rotateToFacing = false;
    public boolean  m_pingPong = false;
    public float    m_timePerFrame = 1.0f;
    public float    m_shadowScale = 1.0f;
}
