package games.landscape;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * Created by Tom on 08/06/13.
 */
public class World
{
    public Rect  m_screenWindow;
    public RectF m_worldWindow;
    public Terrain m_terrain;
    public EntityManager m_entityManager;
    public Player m_player;

    static World s_world;

    static World Get()
    {
        return s_world;
    }
    static void Set(World world)
    {
        s_world = world;
    }

    void ConvertScreen2World(Vector2f screen, Vector2f world)
    {
        float screen2world = m_worldWindow.width() / m_screenWindow.width();
        world.x = screen.x - (float)m_screenWindow.left;
        world.y = screen.y - (float)m_screenWindow.top;
        world.x *= screen2world;
        world.y *= screen2world;
        world.x += m_worldWindow.left;
        world.y += m_worldWindow.top;
    }


    void ConvertWorld2Screen(Vector2f screen, Vector2f world)
    {
        float world2screen = m_screenWindow.width() / m_worldWindow.width();
        screen.x = world.x - m_worldWindow.left;
        screen.y = world.y - m_worldWindow.top;
        screen.x *= world2screen;
        screen.y *= world2screen;
        screen.x += m_screenWindow.left;
        screen.y += m_screenWindow.top;
    }

    void ConvertScreen2World(MotionEvent.PointerCoords screen, Vector2f world)
    {
        float world2screen = (m_worldWindow.width() / m_screenWindow.width());
        world.x = screen.x - (float)m_screenWindow.left;
        world.y = screen.y - (float)m_screenWindow.top;
        world.x *= world2screen;
        world.y *= world2screen;
        world.x += m_worldWindow.left;
        world.y += m_worldWindow.top;
    }

}
