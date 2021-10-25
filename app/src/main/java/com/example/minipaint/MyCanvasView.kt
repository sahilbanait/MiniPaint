package com.example.minipaint

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat



private const val STROKE_WIDTH = 12f // has to be float


class MyCanvasView(context: Context?) : View(context){

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private var currentX = 0f
    private var currentY = 0f

    private lateinit var frame : Rect

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)


    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        // Paint style specifies if the primitive being drawn is filled, stroked or both
        style = Paint.Style.STROKE // default: FILL

        strokeJoin = Paint.Join.ROUND // default: MITER

        strokeCap = Paint.Cap.ROUND // default: BUTT

        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    /**
     * it defines what is being drawn
     */
    private var path = Path()


    /**
     * onSizeChanged() is called whenever a view changes the size, it is ideal place to create and setup the views canvas
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        /**
                This method recyle the bitmap before it created
         */
        if (::extraBitmap.isInitialized) extraBitmap.recycle()


        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)

        // Calculate a rectangular frame around the picture.
        val inset = 40
        frame = Rect(inset, inset, width - inset, height - inset)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(extraBitmap, 0f, 0f, null)
        // Draw a frame around the canvas.
        canvas?.drawRect(frame, paint)
    }

    /**
     * onTOuchMethod is called when user touches the display
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }
//    When user first touches the screen
    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        path.reset()
    }


    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
    /**
     * Calculate the distance that has been moved (dx, dy).
    If the movement was further than the touch tolerance, add a segment to the path.
    Set the starting point for the next segment to the endpoint of this segment.
    Using quadTo() instead of lineTo() create a smoothly drawn line without corners. See Bezier Curves.
    Call invalidate() to (eventually call onDraw() and) redraw the view.
     */
    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to cache it.
            extraCanvas.drawPath(path, paint)
        }
        invalidate()
    }



}