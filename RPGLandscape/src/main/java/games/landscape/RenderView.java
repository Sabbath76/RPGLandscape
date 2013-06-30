package games.landscape;

//import java.util.TimerTask;

import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
//import android.media.JetPlayer.OnJetEventListener;
//import android.os.Handler;
//import android.util.Log;
//import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
//import android.view.View;

public class RenderView extends SurfaceView implements SurfaceHolder.Callback {
    private ShapeDrawable mDrawable;
    private LandscapeThread mThread;
    private boolean mThreadIsRunning = false;

    public RenderView(Context context) {
        super(context);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        int x = 10;
        int y = 10;
        int width = 50;
        int height = 50;

        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(0xff74AC23);
        mDrawable.setBounds(x, y, x + width, y + height);
        
        mThread = new LandscapeThread(holder, context);
        	 
    }

    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }
    public void surfaceCreated(SurfaceHolder arg0) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        if (!mThreadIsRunning)
        {
            mThreadIsRunning = true;
            mThread.setRunning(true);
            if (!mThread.isAlive())
            {
                mThread.start();
            }
        }
        else
        {
            mThread.onResume();
        }
    }

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mThread.setSurfaceSize(width, height);
	}

	public boolean onTouchEvent(MotionEvent event) 
	{
		mThread.onTouchEvent(event);

        return true;
		
/*        switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 // process the mouse hover movement...
            	 
                 return true;
             case MotionEvent.ACTION_MOVE:
                 // process the scroll wheel movement...
                 return true;
	         }
	     return super.onTouchEvent(event);
*/	 }
	public void surfaceDestroyed(SurfaceHolder holder) {
        mThread.onPause();
	}

	public LandscapeThread getThread() {
		return mThread;
	}
}
