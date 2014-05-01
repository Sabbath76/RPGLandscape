package games.landscape;

import android.graphics.drawable.Drawable;

/**
 * Created by Tom on 17/03/14.
 */
public class spawnParams
{
    enum ECharacterType
    {
        Archer,
        Normal
    };

    drawable gfx = null;
    ECharacterType charType = ECharacterType.Normal;
    int chance = 1;
}
