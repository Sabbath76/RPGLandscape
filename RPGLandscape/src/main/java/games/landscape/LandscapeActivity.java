package games.landscape;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.PopupMenu;
import games.landscape.LandscapeThread;
import games.landscape.RenderView;

import android.app.Activity;
import android.os.Bundle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class LandscapeActivity extends Activity {
	
	RenderView mRenderView;
	private LandscapeThread mThread;
    private PopupMenu mPopupMenu;

    final String FILENAME = "terrain.sav";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mRenderView = new RenderView(this);
        
        mThread = mRenderView.getThread();

//        mPopupMenu = new PopupMenu(this, mRenderView);
//        mPopupMenu.getMenu().
        
        setContentView(mRenderView);//R.layout.main);

        if (savedInstanceState != null)
        {
            byte buffer[] = savedInstanceState.getByteArray("terrain");
            if (buffer != null)
            {
                mThread.m_world.m_terrain.Load(buffer);
            }
        }
        else
        {
            try
            {
                int size = mThread.m_world.m_terrain.WORLD_SIZE_X*mThread.m_world.m_terrain.WORLD_SIZE_Y;
                byte buffer[] = new byte[size];
                FileInputStream fis = openFileInput(FILENAME);
                int read = fis.read(buffer, 0, size);
                fis.close();

                if (read == size)
                {
                    mThread.m_world.m_terrain.Load(buffer);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            try
            {
                FileInputStream fis = openFileInput(Quest.QUEST_FILENAME);
                Quest.loadAll(fis);
                fis.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }

            mThread.SetupQuests();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // The activity is about to be destroyed.
    }
    
    public void onUpdate()
    {
    }

    public void Save(byte buffer[])
    {
        try
        {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(buffer);
            fos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save the user's current game state
        byte buffer[] = mThread.m_world.m_terrain.Save();
        savedInstanceState.putByteArray("terrain", buffer);

        Save(buffer);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null)
        {
            byte buffer[] = savedInstanceState.getByteArray("terrain");
            if (buffer != null)
            {
                mThread.m_world.m_terrain.Load(buffer);
            }
        }
    }

    /**
     * Standard override to get key-press events.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, msg);
        } else {
            return mThread.doKeyDown(keyCode, msg);
        }
    }

    /**
     * Standard override for key-up.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyUp(keyCode, msg);
        } else {
            return mThread.doKeyUp(keyCode, msg);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.options, menu);

        menu.getItem(3/*R.id.Earth*/).setIcon(ETerrainType.Earth.textureID);
        menu.getItem(2/*R.id.Grass*/).setIcon(ETerrainType.Grass.textureID);
        menu.getItem(5/*R.id.Rocks*/).setIcon(ETerrainType.Rocks.textureID);
        menu.getItem(4/*R.id.Water*/).setIcon(ETerrainType.Water.textureID);
        menu.getItem(6/*R.id.Path*/).setIcon(ETerrainType.Path.textureID);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean ret = super.onOptionsItemSelected(item);

        switch (item.getItemId())
        {
            case R.id.play:
                mThread.SetPlay();
                return true;

            case R.id.pan:
                mThread.SetPan();
                return true;

            case R.id.Grass:
            {
                mThread.SetPaint(ETerrainType.Grass);
                return true;
            }

            case R.id.Earth:
            {
                mThread.SetPaint(ETerrainType.Earth);
                return true;
            }

            case R.id.Rocks:
            {
                mThread.SetPaint(ETerrainType.Rocks);
                return true;
            }

            case R.id.Water:
            {
                mThread.SetPaint(ETerrainType.Water);
                return true;
            }

            case R.id.Path:
            {
                mThread.SetPaint(ETerrainType.Path);
                return true;
            }

            case R.id.RenderBuffered:
            {
                mThread.ToggleRenderBuffered();
                return true;
            }

            case R.id.RenderSmoothed:
            {
                mThread.ToggleRenderSmoothed();
                return true;
            }

            case R.id.RenderPainted:
            {
                mThread.ToggleRenderPainted();
                return true;
            }

            case R.id.RenderDebug:
            {
                mThread.ToggleRenderDebug();
                return true;
            }

            case R.id.QuestEditor:
            {
                //define a new Intent for the second Activity
                Intent intent = new Intent(this,QuestEditor.class);

                //start the second Activity
                this.startActivity(intent);

                return true;
            }

            case R.id.Save:
            {
                byte buffer[] = mThread.m_world.m_terrain.Save();

                Save(buffer);

                return true;
            }
        }

        return ret;
    }
}