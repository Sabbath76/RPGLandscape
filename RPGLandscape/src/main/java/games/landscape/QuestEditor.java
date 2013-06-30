package games.landscape;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 30/06/13.
 */
public class QuestEditor extends Activity {

    QETargetListAdapter mTargetList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.questeditor);

        List<TargetChar> targetChars = new ArrayList<TargetChar>();

        mTargetList = new QETargetListAdapter(this, R.id.targetList, targetChars);
    }
}