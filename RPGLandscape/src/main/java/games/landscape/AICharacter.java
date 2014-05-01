package games.landscape;

/**
 * Created by Tom on 08/06/13.
 */

public class AICharacter extends character
{
    float m_nextTargetTime = 0.0f;
    Vector2f m_targetPos = new Vector2f();
    float m_wanderMax = 2.0f;
    float m_wanderMinTime = 2.0f;
    float m_wanderMaxTime = 5.0f;
    float m_wanderSpeed = 1.0f;
    boolean m_wander = true;

    void Update(float timePassed, World world)
    {
        if (m_wander)
        {
            m_nextTargetTime -= timePassed;

            if (m_nextTargetTime < 0.0f)
            {
                m_targetPos.x = m_pos.x + (((float)Math.random() * 2.0f) - 1.0f) * m_wanderMax;
                m_targetPos.y = m_pos.y + (((float)Math.random() * 2.0f) - 1.0f) * m_wanderMax;

                m_nextTargetTime = ((float)Math.random() * (m_wanderMaxTime - m_wanderMinTime)) + m_wanderMinTime;
            }

            m_vel.subtract(m_targetPos, m_pos);
            m_vel.scale(m_wanderSpeed / Math.max(m_vel.length(), 1.0f));
        }

        super.Update(timePassed, world);
    }
}
