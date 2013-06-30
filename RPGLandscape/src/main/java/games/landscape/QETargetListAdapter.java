package games.landscape;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 30/06/13.
 */
public class QETargetListAdapter extends ArrayAdapter<TargetChar>
{
    List<TargetChar> m_targetChars;

    public QETargetListAdapter(Context context, int viewid, List<TargetChar> targetChars)
    {
        super(context, viewid, targetChars);
        m_targetChars = targetChars;
    }

}
