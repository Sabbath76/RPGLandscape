package games.landscape;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PointF;
import android.graphics.Point;

enum ETerrainType
{
    Earth (R.drawable.dirt, 0, 0.0f, false),
    Water (R.drawable.water, 1, 0.1f, true),
	Grass (R.drawable.grassclover, 2, 0.0f, false),
	Path (R.drawable.stonewall, 3, 0.0f, false),
    Rocks (R.drawable.groundtiles, 4, 0.0f, false);
	
	public final int textureID;
	public final int priority;
    public final float scrollSpeed;
    public boolean blocked = false;

    ETerrainType(int _textureID, int _priority, float _scrollSpeed, boolean _blocked)
	{
		textureID = _textureID;
        priority = _priority;
        scrollSpeed = _scrollSpeed;
        blocked = _blocked;
	}
}

enum EDirection
{
	TopLeft		(-1, -1, 1),
	Top			(0, -1, 2),
	TopRight	(1, -1, 4),
	Right		(1, 0, 8),
	BottomRight	(1, 1, 16),
	Bottom		(0, 1, 32),
	BottomLeft	(-1, 1, 64),
	Left		(-1, 0, 128);
	
	public final int offX;
	public final int offY;
    public final int mask;
	
	EDirection(int _offX, int _offY, int _mask)
	{
		offX = _offX;
        offY = _offY;
        mask = _mask;
	}
}

enum EAlphaMap
{
	FourSided			(R.drawable.side4_alpha, -1),
	ThreeSided			(R.drawable.side3_alpha, -1),
	TwoSidedCorner		(R.drawable.corner2sides_alpha, R.drawable.corner2sides_fringe),
	TwoSidedParallel	(R.drawable.side2parallel_alpha, R.drawable.side2parallel_fringe),
	OneSided			(R.drawable.side_alpha, R.drawable.side_fringe),
	Corner				(R.drawable.corner_alpha, -1);

	public final int textureID;
	public final int fringeTextureID;
	EAlphaMap(int _textureID, int _fringeTexture)
	{
		textureID = _textureID;
		fringeTextureID = _fringeTexture;
	}
}

public class Terrain {
	ETerrainType m_terrain[][];

	EnumMap<ETerrainType, Bitmap> m_bitmaps;
	EnumMap<EAlphaMap, Bitmap> m_alphaBitmaps;
	EnumMap<EAlphaMap, Bitmap> m_fringeBitmaps;

    Bitmap m_brushStroke;

    Random m_random;

    Bitmap m_buffer[];
	int m_currentBuffer = 0;
	Canvas m_canvasBuffer;
	Rect m_lastWindow;
	Rect m_lastQuads;
	
	Bitmap m_quadBuffer;
	Canvas m_quadCanvas;

    boolean mDrawSmoothed = true;
    boolean mDrawEdging = false;
    boolean mDrawDebug = false;

    final float QUAD_SIZE = 0.5f;
	
	final int WORLD_SIZE_X = 50;
	final int WORLD_SIZE_Y = 50;

    float m_totalTime = 0.0f;

	EnumMap<ETerrainType, EnumSet<ETerrainType>> m_higherPriority;

	public boolean doClipping = true;
	private int m_clipCounter = 0;
	
	public Terrain(Context context)
	{
		m_terrain = new ETerrainType[WORLD_SIZE_X][WORLD_SIZE_Y];
		
		for (int x=0; x<WORLD_SIZE_X; x++)
		{
			for (int y=0; y<WORLD_SIZE_Y; y++)
			{
				m_terrain[x][y] = ETerrainType.Water;
			}
		}
		
		m_terrain[1][0] = ETerrainType.Earth;
		m_terrain[2][0] = ETerrainType.Rocks;
		m_terrain[3][0] = ETerrainType.Rocks;
		m_terrain[1][1] = ETerrainType.Earth;
		m_terrain[2][1] = ETerrainType.Earth;
		m_terrain[1][2] = ETerrainType.Earth;
		m_terrain[2][2] = ETerrainType.Earth;
		
		m_buffer = new Bitmap[2];
		
		m_lastWindow = new Rect();
		m_lastQuads = new Rect();

        m_random = new Random();

        m_higherPriority = new EnumMap<ETerrainType, EnumSet<ETerrainType>>(ETerrainType.class);
		m_bitmaps = new EnumMap<ETerrainType, Bitmap>(ETerrainType.class);
		Resources res = context.getResources();
        m_brushStroke = BitmapFactory.decodeResource(res, R.drawable.paint_stroke);
		for (ETerrainType t : ETerrainType.values())
		{
			Bitmap bitmap = BitmapFactory.decodeResource(res, t.textureID);
			m_bitmaps.put(t, bitmap);

			EnumSet<ETerrainType> higherPriority = EnumSet.noneOf(ETerrainType.class);

			for (ETerrainType t2 : ETerrainType.values())
			{
				if (t2.priority > t.priority)
				{
					higherPriority.add(t2);
				}
			}
			
			m_higherPriority.put(t, higherPriority);
		}

		m_alphaBitmaps = new EnumMap<EAlphaMap, Bitmap>(EAlphaMap.class);
		for (EAlphaMap a : EAlphaMap.values())
		{
			Bitmap bitmap = BitmapFactory.decodeResource(res, a.textureID);
			m_alphaBitmaps.put(a, bitmap);
		}
		
		m_fringeBitmaps = new EnumMap<EAlphaMap, Bitmap>(EAlphaMap.class);
		for (EAlphaMap a : EAlphaMap.values())
		{
			if (a.fringeTextureID >= 0)
			{
				Bitmap bitmap = BitmapFactory.decodeResource(res, a.fringeTextureID);
				m_fringeBitmaps.put(a, bitmap);
			}
		}
	}

    public byte[] Save()
    {
        byte[] saveBuffer = new byte[WORLD_SIZE_X*WORLD_SIZE_Y];
        int pos=0;

        for (int x=0; x<WORLD_SIZE_X; x++)
        {
            for (int y=0; y<WORLD_SIZE_Y; y++)
            {
                saveBuffer[pos] = (byte)(m_terrain[x][y].ordinal());
                pos++;
            }
        }

        return saveBuffer;
    }
    public void Load(byte[] buffer)
    {
        int pos=0;

        for (int x=0; x<WORLD_SIZE_X; x++)
        {
            for (int y=0; y<WORLD_SIZE_Y; y++)
            {
                m_terrain[x][y] = ETerrainType.values()[buffer[pos]];
                pos++;
            }
        }

    }

	public boolean Query(PointF posF, PointF offset, ETerrainType section[][])
	{
		Point pos = new Point((int)posF.x, (int)posF.y);
		offset = posF;
		offset.x -= pos.x;
		offset.y -= pos.y;
		
		return true;
	}
	
	public void ToggleQuad(int x, int y)
	{
		switch(m_terrain[x][y])
		{
		case Grass:
			m_terrain[x][y] = ETerrainType.Earth;
			break;
		case Earth:
			m_terrain[x][y] = ETerrainType.Rocks;
			break;
		case Rocks:
			m_terrain[x][y] = ETerrainType.Water;
			break;
		case Water:
			m_terrain[x][y] = ETerrainType.Grass;
			break;
		}
	}
	
	public void SetQuad(int x, int y, ETerrainType terrainType)
	{
        if ((x >= 0) && (x < WORLD_SIZE_X)
            && (y >= 0) && (y < WORLD_SIZE_Y))
        {
            m_terrain[x][y] = terrainType;
        }
	}
	
//	public EnumSet<EDirection> QuerySurround(ETerrainType terrain, int X, int Y)
    public int QuerySurround(ETerrainType terrain, int X, int Y)
	{
        int dirs = 0;
//		EnumSet<EDirection> dirs = EnumSet.noneOf(EDirection.class);

//        int dirFlag = 1;
		for (EDirection d : EDirection.values())
		{
			int newX = X + d.offX;
			int newY = Y + d.offY;
			
			if (mDrawSmoothed && (newX >= 0) && (newX < WORLD_SIZE_X) && (newY >= 0) && (newY < WORLD_SIZE_X))
			{
				if (m_terrain[newX][newY] == terrain)
				{
                    dirs |= d.mask;
//					dirs.add(d);
				}
			}

//            dirFlag = dirFlag << 1;
		}

		return dirs;
	}

	public boolean Render(Canvas canvas, Rect window, RectF world, boolean drawSmoothed)
	{
        mDrawSmoothed = drawSmoothed;
        if (canvas == null)
            return false;

        m_lastQuads.set(0, 0, 0, 0);

        m_totalTime += 0.1f;

		float world2Screen = (0.1f+window.width()) / world.width();
		RectF quadRectF = new RectF(world.left/QUAD_SIZE, world.top/QUAD_SIZE, world.right/QUAD_SIZE, world.bottom/QUAD_SIZE);
		Rect  quadRect  = new Rect((int)quadRectF.left, (int)quadRectF.top, (int)(quadRectF.right), (int)(quadRectF.bottom));
		int grid2Screen = (int)(world2Screen * QUAD_SIZE);
		
		Point ssPos = new Point(window.left, window.top);
		ssPos.x -= (int)((quadRectF.left - (float)quadRect.left) * grid2Screen);
		ssPos.y -= (int)((quadRectF.top - (float)quadRect.top) * grid2Screen);
		
		int worldInc = (int)(QUAD_SIZE * world2Screen);
		
		if (doClipping)
		{
			if (m_clipCounter > 0)
			{
				canvas.drawRGB(0, 0, 0);
				m_clipCounter--;
			}
			canvas.clipRect(window);
		}
		else
		{
			m_clipCounter = 2;
		}
//		canvas.drawRGB(100, 0, 0);
		
		m_quadBuffer = Bitmap.createBitmap(grid2Screen, grid2Screen, Bitmap.Config.ARGB_8888);
		m_quadCanvas = new Canvas(m_quadBuffer);
		Rect dstRect = new Rect();
		dstRect.left = ssPos.x;
		dstRect.right = dstRect.left + worldInc;
				
		for (int x=quadRect.left; x<=quadRect.right; x++)
		{
			dstRect.top    = ssPos.y;
			dstRect.bottom = dstRect.top + worldInc;

			if ((x >= 0) && (x < WORLD_SIZE_X))
			{
				for (int y=quadRect.top; y<=quadRect.bottom; y++)
				{
					if ((y >= 0) && (y<WORLD_SIZE_Y))
					{
						RenderQuad(canvas, dstRect, x, y, grid2Screen);
					}
			    	
					dstRect.top    += worldInc;
					dstRect.bottom += worldInc;
				}
			}
			
			dstRect.left  += worldInc;
			dstRect.right += worldInc;
		}
		
		Paint unfilledPaint = new Paint();
		unfilledPaint.setStyle(Style.STROKE);
		unfilledPaint.setARGB(0xff, 0xff, 0x40, 0xff);
		canvas.drawRect(window, unfilledPaint);
		
		String text = String.format("w2s = %f, gridSize = %d xcnt = %d ycnt=%d", world2Screen, grid2Screen, 0, 0);//xCount, yCount);
		canvas.drawText(text, 10, 10, unfilledPaint);
		
		return true;
	}
	
	public boolean RenderBuffered(Canvas canvas, Rect window, RectF world, boolean drawSmoothed)
	{
        m_totalTime += 0.1f;
        mDrawSmoothed = drawSmoothed;
		float world2Screen = (0.1f+window.width()) / world.width();
		RectF quadRectF = new RectF(world.left/QUAD_SIZE, world.top/QUAD_SIZE, world.right/QUAD_SIZE, world.bottom/QUAD_SIZE);
		Rect  quadRect  = new Rect((int)quadRectF.left, (int)quadRectF.top, (int)(quadRectF.right)+1, (int)(quadRectF.bottom)+1);
		int quadPixels = (int)(world2Screen * QUAD_SIZE);
		
		final int xQuads = quadRect.width()+1;
		final int yQuads = quadRect.height()+1;
		
		boolean sameWindow = (m_lastWindow.equals(window)); 
		if (!sameWindow)
		{
			m_lastWindow.set(window);
			m_buffer[0] = Bitmap.createBitmap(xQuads * quadPixels, yQuads * quadPixels, Bitmap.Config.RGB_565);
			m_buffer[1] = Bitmap.createBitmap(xQuads * quadPixels, yQuads * quadPixels, Bitmap.Config.RGB_565);
			m_currentBuffer = 0;
			
			m_canvasBuffer = new Canvas(m_buffer[m_currentBuffer]);

            m_quadBuffer = Bitmap.createBitmap(quadPixels, quadPixels, Bitmap.Config.ARGB_8888);
            m_quadCanvas = new Canvas(m_quadBuffer);
		}

		boolean noChanges = false;//(m_lastQuads.equals(quadRect));
		
		if (!noChanges)
		{
			Rect ints = new Rect(Math.max(quadRect.left, m_lastQuads.left), Math.max(quadRect.top, m_lastQuads.top),
							  Math.min(quadRect.right, m_lastQuads.right), Math.min(quadRect.bottom, m_lastQuads.bottom));
			
			m_currentBuffer = 1-m_currentBuffer;
			m_canvasBuffer.setBitmap(m_buffer[m_currentBuffer]);
///			m_canvasBuffer.drawRGB(100, 0, 0);
			
			if ((ints.width() > 0) && (ints.height() > 0))
			{
				Rect src = new Rect((ints.left - m_lastQuads.left) * quadPixels, (ints.top-m_lastQuads.top) * quadPixels,
						(m_lastQuads.width() - (ints.right-m_lastQuads.right)) * quadPixels, (m_lastQuads.height() - (ints.bottom-m_lastQuads.bottom)) * quadPixels);
				int tgtX = (ints.left-quadRect.left) * quadPixels;
				int tgtY = (ints.top-quadRect.top) * quadPixels;
				Rect dst = new Rect(tgtX, tgtY,
						tgtX+src.width(), tgtY+src.height());
				m_canvasBuffer.drawBitmap(m_buffer[1-m_currentBuffer], src, dst, null);

                if (mDrawDebug)
                {
                    Paint paint = new Paint();
                    paint.setColor(0xffffffff);
                    paint.setStyle(Style.STROKE);
                    paint.setStrokeWidth(2.0f);
                    m_canvasBuffer.drawRect(dst, paint);
                }
			}


			int worldInc = (int)(QUAD_SIZE * world2Screen);
					
			Rect dstRect = new Rect();
			dstRect.left = 0;

			dstRect.right = worldInc;
					
			for (int x=quadRect.left; x<quadRect.right; x++)
			{
				dstRect.top    = 0;
				dstRect.bottom = worldInc;
	
				if ((x >= 0) && (x < WORLD_SIZE_X))
				{
					for (int y=quadRect.top; y<quadRect.bottom; y++)
					{
						if ((y >= 0) && (y<WORLD_SIZE_Y))
						{
							if (!ints.contains(x, y) || (m_terrain[x][y].scrollSpeed != 0.0f))
							{
								RenderQuad(m_canvasBuffer, dstRect, x, y, quadPixels);
							}
						}
				    	
						dstRect.top    += worldInc;
						dstRect.bottom += worldInc;
					}
				}
				
				dstRect.left  += worldInc;
				dstRect.right += worldInc;
			}
		}
		Bitmap currentBuffer = m_buffer[m_currentBuffer];

		if (doClipping)
		{
			if (m_clipCounter > 0)
			{
				canvas.drawRGB(0, 0, 0);
				m_clipCounter--;
			}
			canvas.clipRect(window);
		}
		else
		{
			m_clipCounter = 2;
			canvas.drawRGB(0, 0, 0xff);
		}
		Point ssPos = new Point(window.left, window.top);
		ssPos.x -= (int)((quadRectF.left - (float)quadRect.left) * quadPixels);
		ssPos.y -= (int)((quadRectF.top - (float)quadRect.top) * quadPixels);
		canvas.drawBitmap(currentBuffer, ssPos.x, ssPos.y, null);
		
/*		Paint unfilledPaint = new Paint();
		unfilledPaint.setStyle(Style.STROKE);
		unfilledPaint.setARGB(0xff, 0xff, 0x40, 0xff);
		canvas.drawRect(window, unfilledPaint);

		String changedRectTxt   = noChanges ? "no Changes" : "Changes";
//		String changedWindowTxt = changedWindow ? "ChangedWindow" : "Same Window";
		String text = String.format("w2s = %f, gridSize = %d changes: %s (%d, %d) == (%d, %d)", world2Screen, quadPixels, changedRectTxt, m_lastQuads.left, m_lastQuads.top, quadRect.left, quadRect.top);
		canvas.drawText(text, 10, 10, unfilledPaint);
*/
		m_lastQuads.set(quadRect);

		return true;
	}


    public boolean RenderPainted(Canvas canvas, Rect window, RectF world)
    {
        m_totalTime += 0.1f;
        float world2Screen = (0.1f+window.width()) / world.width();
        RectF quadRectF = new RectF(world.left/QUAD_SIZE, world.top/QUAD_SIZE, world.right/QUAD_SIZE, world.bottom/QUAD_SIZE);
        Rect  quadRect  = new Rect((int)quadRectF.left, (int)quadRectF.top, (int)(quadRectF.right)+1, (int)(quadRectF.bottom)+1);
        int quadPixels = (int)(world2Screen * QUAD_SIZE);

        final int xQuads = quadRect.width()+1;
        final int yQuads = quadRect.height()+1;

        boolean sameWindow = (m_lastWindow.equals(window));
        if (!sameWindow)
        {
            m_lastWindow.set(window);
            m_buffer[0] = Bitmap.createBitmap(xQuads * quadPixels, yQuads * quadPixels, Bitmap.Config.ARGB_8888);
            m_buffer[1] = Bitmap.createBitmap(xQuads * quadPixels, yQuads * quadPixels, Bitmap.Config.ARGB_8888);
            m_currentBuffer = 0;

            m_canvasBuffer = new Canvas(m_buffer[m_currentBuffer]);

            m_quadBuffer = Bitmap.createBitmap(quadPixels, quadPixels, Bitmap.Config.ARGB_8888);
            m_quadCanvas = new Canvas(m_quadBuffer);
        }

        boolean noChanges = false;//(m_lastQuads.equals(quadRect));

        if (!noChanges)
        {
            Rect ints = new Rect(Math.max(quadRect.left, m_lastQuads.left), Math.max(quadRect.top, m_lastQuads.top),
                    Math.min(quadRect.right, m_lastQuads.right), Math.min(quadRect.bottom, m_lastQuads.bottom));

            m_currentBuffer = 1-m_currentBuffer;
            m_canvasBuffer.setBitmap(m_buffer[m_currentBuffer]);
///			m_canvasBuffer.drawRGB(100, 0, 0);

            if ((ints.width() > 0) && (ints.height() > 0))
            {
                Rect src = new Rect((ints.left - m_lastQuads.left) * quadPixels, (ints.top-m_lastQuads.top) * quadPixels,
                        (m_lastQuads.width() - (ints.right-m_lastQuads.right)) * quadPixels, (m_lastQuads.height() - (ints.bottom-m_lastQuads.bottom)) * quadPixels);
                int tgtX = (ints.left-quadRect.left) * quadPixels;
                int tgtY = (ints.top-quadRect.top) * quadPixels;
                Rect dst = new Rect(tgtX, tgtY,
                        tgtX+src.width(), tgtY+src.height());
                m_canvasBuffer.drawBitmap(m_buffer[1-m_currentBuffer], src, dst, null);

                if (mDrawDebug)
                {
                    Paint paint = new Paint();
                    paint.setColor(0xffffffff);
                    paint.setStyle(Style.STROKE);
                    paint.setStrokeWidth(2.0f);
                    m_canvasBuffer.drawRect(dst, paint);
                }
            }


            int worldInc = (int)(QUAD_SIZE * world2Screen);

            Rect dstRect = new Rect();
            dstRect.left = 0;

            dstRect.right = worldInc;

            for (int x=quadRect.left; x<quadRect.right; x++)
            {
                dstRect.top    = 0;
                dstRect.bottom = worldInc;

                if ((x >= 0) && (x < WORLD_SIZE_X))
                {
                    for (int y=quadRect.top; y<quadRect.bottom; y++)
                    {
                        if ((y >= 0) && (y<WORLD_SIZE_Y))
                        {
//                            if (!ints.contains(x, y) || (m_terrain[x][y].scrollSpeed != 0.0f))
                            {

                                Matrix brushPos = new Matrix();
                                float xpos = dstRect.left + (dstRect.width() * m_random.nextFloat());
                                float ypos = dstRect.top + (dstRect.height() * m_random.nextFloat());
                                brushPos.setTranslate(xpos, ypos);
                                m_canvasBuffer.drawBitmap(m_brushStroke, brushPos, null);
//                                RenderQuad(m_canvasBuffer, dstRect, x, y, quadPixels);
                            }
                        }

                        dstRect.top    += worldInc;
                        dstRect.bottom += worldInc;
                    }
                }

                dstRect.left  += worldInc;
                dstRect.right += worldInc;
            }
        }
        Bitmap currentBuffer = m_buffer[m_currentBuffer];

        if (doClipping)
        {
            if (m_clipCounter > 0)
            {
                canvas.drawRGB(0, 0, 0);
                m_clipCounter--;
            }
            canvas.clipRect(window);
        }
        else
        {
            m_clipCounter = 2;
            canvas.drawRGB(0, 0, 0xff);
        }
        Point ssPos = new Point(window.left, window.top);
        ssPos.x -= (int)((quadRectF.left - (float)quadRect.left) * quadPixels);
        ssPos.y -= (int)((quadRectF.top - (float)quadRect.top) * quadPixels);
        canvas.drawBitmap(currentBuffer, ssPos.x, ssPos.y, null);

/*		Paint unfilledPaint = new Paint();
		unfilledPaint.setStyle(Style.STROKE);
		unfilledPaint.setARGB(0xff, 0xff, 0x40, 0xff);
		canvas.drawRect(window, unfilledPaint);

		String changedRectTxt   = noChanges ? "no Changes" : "Changes";
//		String changedWindowTxt = changedWindow ? "ChangedWindow" : "Same Window";
		String text = String.format("w2s = %f, gridSize = %d changes: %s (%d, %d) == (%d, %d)", world2Screen, quadPixels, changedRectTxt, m_lastQuads.left, m_lastQuads.top, quadRect.left, quadRect.top);
		canvas.drawText(text, 10, 10, unfilledPaint);
*/
        m_lastQuads.set(quadRect);

        return true;
    }

    public boolean IsBlocked(Vector2f pos)
    {
        return IsBlocked((int)(pos.x / QUAD_SIZE), (int)(pos.y / QUAD_SIZE));
    }

    public boolean IsBlocked(int x, int y)
    {
        return (x < 0) || (x >= WORLD_SIZE_X)
                || (y < 0) || (y >= WORLD_SIZE_Y)
                || m_terrain[x][y].blocked;
    }

    public void CheckCollision(Vector2f oldPos, Vector2f newPos)
    {
        final float DELTA = 0.001f;
        final int x0 = (int)(oldPos.x / QUAD_SIZE);
        final int y0 = (int)(oldPos.y / QUAD_SIZE);

        newPos.x = Math.max(newPos.x, 0);
        newPos.y = Math.max(newPos.y, 0);
        newPos.x = Math.min(newPos.x, WORLD_SIZE_X);
        newPos.y = Math.min(newPos.y, WORLD_SIZE_Y);

        for (int numIts=0; numIts<3; numIts++)
        {
            int x1 = (int)(newPos.x / QUAD_SIZE);
            int y1 = (int)(newPos.y / QUAD_SIZE);


            if ((x0 != x1) || (y0 != y1))
            {
                int testX = 0, testY = 0;

                boolean found = false;
                if (Math.abs(newPos.x - oldPos.x) > Math.abs(newPos.y - oldPos.y))
                {
                    if (IsBlocked(x1,y0))
                    {
                        testX = x1;
                        testY = y0;
                        found = true;
                    }
                    else if (IsBlocked(x0,y1))
                    {
                        testX = x0;
                        testY = y1;
                        found = true;
                    }
                }
                else
                {
                    if (IsBlocked(x0, y1))
                    {
                        testX = x0;
                        testY = y1;
                        found = true;
                    }
                    else if (IsBlocked(x1, y0))
                    {
                        testX = x1;
                        testY = y0;
                        found = true;
                    }
                }
                if (!found && IsBlocked(x1, y1))
                {
                    testX = x1;
                    testY = y1;
                    found = true;
                }

                if (found)
                {
                    final float xMin = (testX * QUAD_SIZE);
                    final float xMax = xMin + QUAD_SIZE;
                    float deltaX = 0.0f, deltaY = 0.0f;
                    if (oldPos.x < xMin)
                    {
    //                    deltaX = Math.min(newPos.x, xMin - DELTA) - newPos.x;
                        newPos.x = Math.min(newPos.x, xMin - DELTA);
                    }
                    else if (oldPos.x > xMax)
                    {
    //                    deltaX = Math.max(newPos.x, xMax + DELTA) - newPos.x;
                        newPos.x = Math.max(newPos.x, xMax + DELTA);
                    }
                    final float yMin = (testY * QUAD_SIZE);
                    final float yMax = yMin + QUAD_SIZE;
                    if (oldPos.y < yMin)
                    {
    //                    deltaY = Math.min(newPos.y, yMin - DELTA) - newPos.y;
                        newPos.y = Math.min(newPos.y, yMin - DELTA);
                    }
                    else if (oldPos.y > yMax)
                    {
    //                    deltaY = Math.max(newPos.y, yMax + DELTA) - newPos.y;
                        newPos.y = Math.max(newPos.y, yMax + DELTA);
                    }

    //                if (Math.abs(deltaX) > Math.abs(deltaY))
    //                {
    //                    newPos.x += deltaX;
    //                }
    //                else
    //                {
    //                    newPos.y += deltaY;
    //                }
                }
            }
        }

        /*
        int dx = x1-x0;
        int dy = y1-y0;
        int stepX, stepY;

        if (dx > 0)
        {
            stepX = 1;
        }
        else
        {
            stepX = -1;
        }
        if (dy > 0)
        {
            stepY = 1;
        }
        else
        {
            stepY = -1;
        }

        while(true)
        {
            if (tMaxX < tMaxY)
            {
                tMaxX = tMaxX + tDeltaX;
                X = X + stepX;
            }
            else
            {
                tMaxY = tMaxY + tDeltaY;
                Y = Y + stepY;
            }
        }
        */
    }
	
	public void RenderQuad(Canvas canvas, Rect dstRect, int x, int y, int gridPixels)
	{
		ETerrainType terrain = m_terrain[x][y];
		Bitmap bitmap = m_bitmaps.get(terrain);
		
		final Rect ovDst = new Rect(0, 0, gridPixels, gridPixels);

		final float STRETCH_MAX = 0.05f;
		final int bmpTilesX = bitmap.getWidth() / gridPixels;
		final int bmpTilesY = bitmap.getHeight() / gridPixels;
		float xMin = x%bmpTilesX;
		float yMin = y%bmpTilesY;
        float xMax = xMin + 1.0f;
        float yMax = yMin + 1.0f;
        if (terrain.scrollSpeed != 0.0f)
        {
            if (xMax != bmpTilesX)
            {
                xMax += (STRETCH_MAX*Math.sin((x+1.0f)*m_totalTime*terrain.scrollSpeed));
                xMax = Math.max(Math.min(xMax, bmpTilesX), 0.0f);
            }
            if (xMin != 0.0f)
            {
                xMin += (STRETCH_MAX*Math.sin(((float)x)*m_totalTime*terrain.scrollSpeed));
                xMin = Math.max(Math.min(xMin, bmpTilesX), 0.0f);
            }
            if (yMax != bmpTilesY)
            {
                yMax += (STRETCH_MAX*Math.cos((y+1.0f)*m_totalTime*terrain.scrollSpeed));
                yMax = Math.max(Math.min(yMax, bmpTilesY), 0.0f);
            }
            if (yMin != 0.0f)
            {
                yMin += (STRETCH_MAX*Math.cos(((float)y)*m_totalTime*terrain.scrollSpeed));
                yMin = Math.max(Math.min(yMin, bmpTilesY), 0.0f);
            }
        }
		final Rect srcRect = new Rect((int)(xMin * gridPixels), (int)(yMin * gridPixels), (int)(xMax * gridPixels), (int)(yMax * gridPixels));

		Paint paint = new Paint();

		canvas.drawBitmap(bitmap, srcRect, dstRect, null);
		
		for (ETerrainType overlayTerrain : m_higherPriority.get(terrain))
		{
            int overlays = QuerySurround(overlayTerrain, x, y);
//			EnumSet<EDirection> overlays = QuerySurround(overlayTerrain, x, y);
			
			if (overlays != 0)
			{
				int numSides = 0;
				
				final EDirection sideDirs[] = {EDirection.Top, EDirection.Right, EDirection.Bottom, EDirection.Left};
				
				EDirection setDir = EDirection.BottomLeft;
				int setIdx = 0;
				int curIdx = 0;
				EDirection unsetDir = EDirection.BottomLeft;
				for (EDirection dir : sideDirs)
				{
					if ((overlays & dir.mask) != 0)
					{
						numSides++;
						setDir = dir;
						setIdx = curIdx;
					}
					else
					{
						unsetDir = dir;
					}
					curIdx++;
				}
				
				EAlphaMap alphaMap = EAlphaMap.Corner;
				
				float rotation = 0.0f;
				switch (numSides)
				{
				case 0:									
					if ((overlays & EDirection.TopLeft.mask) != 0)
					{
						rotation = 180.0f;
					}
					else if ((overlays & EDirection.TopRight.mask) != 0)
//                        if (overlays.contains(EDirection.TopRight))
					{
						rotation = -90.0f;
					}
                    else if ((overlays & EDirection.BottomLeft.mask) != 0)
//                    else if (overlays.contains(EDirection.BottomLeft))
					{
						rotation = 90.0f;
					}
					break;
				case 1:
					alphaMap = EAlphaMap.OneSided;
					
					switch(setDir)
					{
					case Left:
						rotation = 180.0f;
						break;
					case Top:
						rotation = -90.0f;
						break;
					case Bottom:
						rotation = 90.0f;
						break;
					}
					break;
				case 2:
					int nextIdx = (setIdx+1)%4;
					int lastIdx = (setIdx+3)%4;
					boolean isLast = (overlays&sideDirs[lastIdx].mask) != 0;
					boolean isNext = (overlays&sideDirs[nextIdx].mask) != 0;
					if (isLast || isNext)
					{
						EDirection firstSide = isLast ? sideDirs[lastIdx] : setDir;
						alphaMap = EAlphaMap.TwoSidedCorner;
						switch(firstSide)
						{
						case Left:
							rotation = -90.0f;
							break;
						case Right:
							rotation = 90.0f;
							break;
						case Top:
							rotation = 0.0f;
							break;
						case Bottom:
							rotation = 180.0f;
							break;
						}
					}
					else
					{
						alphaMap = EAlphaMap.TwoSidedParallel;
						
						if ((setDir == EDirection.Left) || (setDir == EDirection.Right))
						{
							rotation = 0.0f;
						}
						else
						{
							rotation = 90.0f;
						}
					}
						
					break;
				case 3:
					alphaMap = EAlphaMap.ThreeSided;
					switch(unsetDir)
					{
					case Right:
						rotation = -90.0f;
						break;
					case Left:
						rotation = 90.0f;
						break;
					case Top:
						rotation = 180.0f;
						break;
					}
					break;
				case 4:									
					alphaMap = EAlphaMap.FourSided;
					break;
				}
				paint.setFilterBitmap(false);

				Bitmap bitmapOverlay = m_bitmaps.get(overlayTerrain);

				int bmpOverlayTilesX = bitmapOverlay.getWidth() / gridPixels;
				int bmpOverlayTilesY = bitmapOverlay.getHeight() / gridPixels;
				int xOverlayMin = x%bmpOverlayTilesX;
				int yOverlayMin = y%bmpOverlayTilesY;

				Rect ov1Src = new Rect(xOverlayMin*gridPixels, yOverlayMin*gridPixels, (xOverlayMin+1)*gridPixels, (yOverlayMin+1)*gridPixels);

				m_quadCanvas.drawBitmap(bitmapOverlay, ov1Src, ovDst, paint);
				PorterDuffXfermode xferMode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
				paint.setXfermode(xferMode);
				
				Bitmap alphaBMap = m_alphaBitmaps.get(alphaMap);
				Matrix mat = new Matrix();
				float scale = (float)gridPixels / alphaBMap.getWidth();
				mat.setScale(scale, scale);
				mat.preRotate(rotation, alphaBMap.getWidth()/2, alphaBMap.getHeight()/2);
				m_quadCanvas.drawBitmap(alphaBMap, mat, paint);

				if (mDrawEdging && m_fringeBitmaps.containsKey(alphaMap))
				{
					PorterDuffXfermode xferMode2 = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
					paint.setXfermode(xferMode2);

					Bitmap fringeBMap = m_fringeBitmaps.get(alphaMap);
					m_quadCanvas.drawBitmap(fringeBMap, mat, paint);
				}

				paint.setXfermode(null);

				canvas.drawBitmap(m_quadBuffer, dstRect.left, dstRect.top, null);
			}
		}
	}

    public void ToggleDebug()
    {
        mDrawDebug = !mDrawDebug;
    }
}
