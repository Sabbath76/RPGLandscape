package games.landscape;

/**
 * Created by Tom on 19/07/13.
 */


public class collectable extends entity
{
    float m_collectRange = 0.2f;
    collectableType m_collectType = collectableType.Money;
    float m_amount = 1.0f;

    public enum collectableType
    {
        Health,
        Money
    };

    collectable(collectableType collectType, float value)
    {
        m_collectType = collectType;
        m_amount = value;

        switch (m_collectType)
        {
            case Health:
                m_drawable = drawable.s_health;
                break;
            case Money:
                m_drawable = drawable.s_money;
                break;
        }
    }

    void Update(float timePassed, World world)
    {
        float distSqr = world.m_player.m_pos.distanceSquared(m_pos);
        if (distSqr < (m_collectRange*m_collectRange))
        {
            if (m_collectType == collectableType.Health)
            {
                world.m_player.AddHealth(m_amount);
                SoundManager.Get().PlaySound(SoundManager.ESoundType.GetCoins, m_pos);
            }
            else
            {
                world.m_player.AddMoney(m_amount);
                SoundManager.Get().PlaySound(SoundManager.ESoundType.GetHealth, m_pos);
            }
            m_deleteMe = true;
        }
    }
}
