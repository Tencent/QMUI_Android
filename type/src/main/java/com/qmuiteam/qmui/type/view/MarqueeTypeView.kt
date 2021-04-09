package com.qmuiteam.qmui.type.view

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import com.qmuiteam.qmui.type.TypeModel
import com.qmuiteam.qmui.type.parser.PlainTextParser
import com.qmuiteam.qmui.type.parser.TextParser

class MarqueeTypeView : BaseTypeView {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    var typeModel: TypeModel? = null
    var textParser: TextParser = PlainTextParser.instance
        set(value) {
            if (field != value) {
                field = value
                reset()
                contentWidth = -1
                typeModel = value.parse(text)
                requestLayout()
                if (isAttachedToWindow) {
                    start()
                }
            }
        }

    var text: CharSequence? = null
        set(value) {
            if (field != value) {
                field = value
                reset()
                contentWidth = -1
                typeModel = textParser.parse(value)
                requestLayout()
                if (isAttachedToWindow) {
                    start()
                }
            }
        }

    var keepTime: Long = 2000
    var gap: Float = resources.displayMetrics.density * 50
    var moveSpeedPerMs: Float = resources.displayMetrics.density / 36
    var lastDrawTime = -2L

    private var elementMaxHeight = 0
    private var contentWidth = -1
    private var fadeHelper: FadeHelper? = null
    private var startX: Float = 0f


    var fadeWidth: Float
        get() = fadeHelper?.fadeWidth ?: 0f
        set(value) {
            (fadeHelper ?: FadeHelper().also { fadeHelper = it }).fadeWidth = value
            invalidate()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        environment.setMeasureLimit(widthSize, heightSize)
        measureAndLayoutModel()
        val usedWidth = if (widthMode == MeasureSpec.AT_MOST) {
            contentWidth.toInt()
        } else widthSize
        val usedHeight = if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            elementMaxHeight.toInt()
        } else heightSize

        var current = typeModel?.firstElement()
        while (current != null) {
            current.y = (usedHeight - current.measureHeight) / 2
            current = current.next
        }
        setMeasuredDimension(usedWidth, usedHeight)
    }


    private fun measureAndLayoutModel() {
        elementMaxHeight = 0
        var current = typeModel?.firstElement()
        var x = 0
        while (current != null) {
            current.measure(environment)
            current.x = x
            elementMaxHeight = elementMaxHeight.coerceAtLeast(current.measureHeight)
            x += current.measureWidth
            current = current.next
        }
        contentWidth = x
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!text.isNullOrBlank()) {
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    fun start() {
        lastDrawTime = -1
        invalidate()
    }

    fun stop() {
        lastDrawTime = -2
    }

    fun reset() {
        startX = 0f
    }

    override fun onDraw(canvas: Canvas) {
        if(fadeHelper == null){
            drawContent(canvas)
        }else{
            fadeHelper!!.drawFade(canvas){
                drawContent(canvas)
            }
        }
    }


    private fun drawContent(canvas: Canvas){
        if (contentWidth >0 && contentWidth <= width) {
            lastDrawTime = -2
            startX = 0f
        }
        canvas.save()
        canvas.translate(startX, 0f)
        var current = typeModel?.firstElement()
        while (current != null) {
            if (current.x + startX > width) {
                break
            } else if (current.x + startX + current.measureWidth > 0) {
                current.draw(environment, canvas)
            }
            current = current.next
        }
        canvas.restore()
        val gapRight = startX + contentWidth + gap
        if (startX < 0 && gapRight < width) {
            canvas.save()
            canvas.translate(gapRight, 0f)
            current = typeModel?.firstElement()
            while (current != null) {
                if (current.x + gapRight > width) {
                    break
                } else {
                    current.draw(environment, canvas)
                }
                current = current.next
            }
            canvas.restore()
        }
        if (lastDrawTime == -2L) {
            return
        }
        if (lastDrawTime == -1L) {
            lastDrawTime = SystemClock.elapsedRealtime() + if(startX == 0f) keepTime else 0
        } else {
            val newTime = SystemClock.elapsedRealtime()
            if(lastDrawTime < newTime){
                startX -= moveSpeedPerMs * (newTime - lastDrawTime)
                if(startX >= 0f && startX < resources.displayMetrics.density * 1.5){
                    // allow 1.5dp error
                    startX = 0f
                    lastDrawTime = newTime + keepTime
                }else{
                    lastDrawTime = newTime
                }

                if (startX + contentWidth < 0) {
                    startX += contentWidth + gap
                }
                // if drop many many frames. this condition can be matched, so recover to normal state.
                if (startX + contentWidth < 0) {
                    startX = 0f
                }
            } // else is keep time
        }
        postInvalidateOnAnimation()
    }

    private inner class FadeHelper{

        var fadeWidth = 0f

        private val paint = Paint().apply {
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }

        private val leftFadeShader by lazy(LazyThreadSafetyMode.NONE) {
            LinearGradient(
                    0f, 0f, fadeWidth, 0f,
                    intArrayOf(Color.TRANSPARENT, Color.BLACK), null, Shader.TileMode.CLAMP
            )
        }

        private val rightFadeShader by lazy(LazyThreadSafetyMode.NONE) {
            LinearGradient(
                    0f, 0f, fadeWidth, 0f,
                    intArrayOf(Color.BLACK, Color.TRANSPARENT), null, Shader.TileMode.CLAMP
            )
        }

        inline fun drawFade(canvas: Canvas, action: (Canvas) -> Unit) = canvas.apply {
            if(fadeWidth <= 0 || contentWidth <= width){
                action(this)
            }else{
                val layerId = saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
                action(this)
                if(startX < 0){
                    paint.shader = leftFadeShader
                    drawRect(0f, 0f, fadeWidth, height.toFloat(), paint)
                }

                translate((width - fadeWidth).coerceAtLeast(0f), 0f)
                paint.shader = rightFadeShader
                drawRect(0f, 0f, fadeWidth, height.toFloat(), paint)
                restoreToCount(layerId)
            }
        }
    }
}