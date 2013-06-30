/*
   Copyright (C) 1997,1998,1999
   Kenji Hiranabe, Eiwa System Management, Inc.

   This program is free software.
   Implemented by Kenji Hiranabe(hiranabe@esm.co.jp),
   conforming to the Java(TM) 3D API specification by Sun Microsystems.

   Permission to use, copy, modify, distribute and sell this software
   and its documentation for any purpose is hereby granted without fee,
   provided that the above copyright notice appear in all copies and
   that both that copyright notice and this permission notice appear
   in supporting documentation. Kenji Hiranabe and Eiwa System Management,Inc.
   makes no representations about the suitability of this software for any
   purpose.  It is provided "AS IS" with NO WARRANTY.
*/
package games.landscape;

import android.graphics.RectF;

import java.io.Serializable;

/**
  * A 2 element vector that is represented by single precision
  * floating point x,y coordinates.
  * @version specification 1.1, implementation $Revision: 1.9 $, $Date: 1999/10/05 07:03:50 $
  * @author Kenji hiranabe
  */
public class Vector2f implements Serializable
{
    float x;
    float y;

    /**
      * Constructs and initializes a Vector2f from the specified xy coordinates.
      * @param x the x coordinate
      * @param y the y coordinate
      */
    public Vector2f(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    /**
      * Constructs and initializes a Vector2f from the specified array.
      * @param v the array of length 2 containing xy in order
      */
    public Vector2f(float v[])
    {
        this.x = v[0];
        this.y = v[1];
    }

    /**
      * Constructs and initializes a Vector2f from the specified Vector2f.
      * @param v1 the Vector2f containing the initialization x y data
      */
    public Vector2f(Vector2f v1)
    {
        this.x = v1.x;
        this.y = v1.y;
    }

    /**
      * Constructs and initializes a Vector2f to (0,0).
      */
    public Vector2f()
    {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Vector2f(Vector2f pos, Vector2f dir, float t)
    {
        x = pos.x + (dir.x * t);
        y = pos.y + (dir.y * t);
    }

    /**
      * Computes the dot product of the this vector and vector v1.
      * @param  v1 the other vector
      */
    public final float dot(Vector2f v1)
    {
	return x*v1.x + y*v1.y;
    }

    public void addScaled(Vector2f v1, float scale)
    {
        x += v1.x * scale;
        y += v1.y * scale;
    }

    /**
      * Returns the length of this vector.
      * @return the length of this vector
      */
    public final float length() {
	return (float)Math.sqrt(x*x + y*y);
    }

    /**
      * Returns the squared length of this vector.
      * @return the squared length of this vector
      */
    public final float lengthSquared() {
	return x*x + y*y;
    }

    /**
      * Normalizes this vector in place.
      */
    public final void normalize() {
	double d = length();

	// zero-div may occur.
	x /= d;
	y /= d;
    }

    /**
      * Sets the value of this vector to the normalization of vector v1.
      * @param v1 the un-normalized vector
      */
    public final void normalize(Vector2f v1)
    {
        x = v1.x;
        y = v1.y;
	    normalize();
    }

    public float getAngle()
    {
        return (float) Math.atan2(y, x);
    }

    /**
      * Returns the angle in radians between this vector and
      * the vector parameter; the return value is constrained to the
      * range [0,PI].
      * @param v1  the other vector
      * @return the angle in radians in the range [0,PI]
      */
    public final float angle(Vector2f v1) {
	// stabler than acos
	return (float)Math.abs(Math.atan2(x * v1.y - y * v1.x, dot(v1)));
    }

    public void scale(float scalar)
    {
        x *= scalar;
        y *= scalar;
    }

    public float distanceSquared(Vector2f pos)
    {
        float dx = (pos.x - x);
        float dy = (pos.y - y);

        return (dx*dx)+(dy*dy);
    }


    public float distance(Vector2f pos)
    {
        float dx = (pos.x - x);
        float dy = (pos.y - y);

        return (float)Math.sqrt((dx*dx)+(dy*dy));
    }

    public void subtract(Vector2f pos)
    {
        x -= pos.x;
        y -= pos.y;
    }

    public void subtract(Vector2f pos, Vector2f sub)
    {
        x = pos.x - sub.x;
        y = pos.y - sub.y;
    }

    public void set(Vector2f pos)
    {
        x = pos.x;
        y = pos.y;
    }

    public float distFromLine(Vector2f p1, Vector2f p2)
    {
        // Return minimum distance between line segment vw and point p
        final float l2 = p1.distanceSquared(p2);//length_squared(v, w);  // i.e. |w-v|^2 -  avoid a sqrt
        if (l2 == 0.0)
            return distance(p1);// distance(p, v);   // v == w case
        // Consider the line extending the segment, parameterized as v + t (w - v).
        // We find projection of point p onto the line.
        // It falls where t = [(p-v) . (w-v)] / |w-v|^2

        Vector2f p1ToPt = new Vector2f();
        Vector2f p1ToP2 = new Vector2f();
        p1ToPt.subtract(this, p1);
        p1ToP2.subtract(p2, p1);

        final float t = p1ToPt.dot(p1ToP2) / l2;//  dot(p - v, w - v) / l2;
        if (t < 0.0)
            return distance(p1);       // Beyond the 'v' end of the segment
        else if (t > 1.0)
            return distance(p2);  // Beyond the 'w' end of the segment
        p1ToP2.scale(1.0f - t);
        p1ToP2.add(p1);
        return distance(p1ToP2);
    }

    public void add(Vector2f p1)
    {
        x += p1.x;
        y += p1.y;
    }

    public float distanceSquared(RectF rect)
    {
        float xDist = Math.max(0.0f, Math.max(rect.left - x, x - rect.right));
        float yDist = Math.max(0.0f, Math.max(rect.top - y, y - rect.bottom));

        return (xDist*xDist) + (yDist*yDist);
    }

    public float distance(RectF rect)
    {
        float xDist = Math.max(0.0f, Math.max(rect.left - x, x - rect.right));
        float yDist = Math.max(0.0f, Math.max(rect.top - y, y - rect.bottom));

        return (float)Math.sqrt((xDist*xDist) + (yDist*yDist));
    }

    public void setRandom(RectF rc)
    {
        float tx = (float) Math.random();
        float ty = (float) Math.random();
        x = rc.left + (rc.width() * tx);
        y = rc.top + (rc.height() * ty);
    }
}
