package pk.farimarwat.wordgamepad


import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.util.*


class GamePadView constructor(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mPadSize = 400
    private lateinit var mTa: TypedArray
    private var mContext: Context
    private lateinit var mBackground: Bitmap
    private lateinit var mButtonBackground: Bitmap
    private var mButtonGlowColor = 0
    private var mButtonColor = 0
    private var mButtonStrokeColor = 0
    private var mButtonTextColor = 0
    private var mDragLineColor = 0
    private var mPathMainCircle = Path()
    private var mListLetters = mutableListOf<PadButton>()
    private var mListLeters_Selected: MutableList<PadButton>? = null


    private var mPaintDragLine = Paint()
    private var mPathDragline = Path()
    private var mPathDraglineTemp = Path()
    private var mDetectRadius = 0f
    var mShouldSelectButton = false

    private var mListPoints:MutableList<PointF>? = null

    private var mPadViewListener:PadViewListener? = null

    init {
        mContext = context
        initAttributes(mContext, attrs)
        mPaintDragLine.apply {
            isAntiAlias = true
            strokeWidth = 20f
            color = mDragLineColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        mDetectRadius = mPadSize * 0.1f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mPadSize = Math.min(measuredWidth, measuredHeight)
        setMeasuredDimension(mPadSize, mPadSize)
        mDetectRadius = mPadSize * 0.1f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setBackground(canvas)
        canvas?.drawBitmap(setMainCircle(), 0f, 0f, null)
        if(mListPoints == null){
            mListPoints = getPoints(mPathMainCircle, mListLetters.size)
        }
        if (mListLetters.isNotEmpty()) {
            mListPoints?.let { listpoints ->
                for (i in 0 until mListLetters.size) {
                    val pointf = listpoints[i]
                    val b = mListLetters[i]
                    b.pointf = pointf
                    mListLetters[i] = b
                    canvas?.drawBitmap(createButton(b), 0f, 0f, null)
                }
            }
        }
        drawDragLine(canvas)
        canvas?.drawPath(mPathDraglineTemp, mPaintDragLine)
        canvas?.drawPath(mPathDraglineTemp,mPaintDragLine)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    for (item in mListLetters) {
                        val istouched = item.pointf?.let {
                            isCircleTouched(
                                event.x, event.y,
                                it.x, it.y, mDetectRadius
                            )
                        }
                        if (istouched == true) {
                            mListLeters_Selected = mutableListOf()
                            mListLeters_Selected?.add(item)
                            mPadViewListener?.onLetterAdded(item,mListLeters_Selected)
                            mShouldSelectButton = true
                            break
                        }
                    }
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mShouldSelectButton) {
                        //Drawing Temp path
                        if(mListLeters_Selected?.isNotEmpty() == true){
                            mPathDraglineTemp.reset()
                            val last = mListLeters_Selected?.last()
                            last?.pointf?.let { mPathDraglineTemp.moveTo(it.x,it.y) }
                            mPathDraglineTemp.lineTo(event.x,event.y)
                        }
                        //

                        for (item in mListLetters) {
                            val istouched = item.pointf?.let {
                                isCircleTouched(
                                    event.x, event.y,
                                    it.x, it.y, mDetectRadius
                                )
                            }
                            if (istouched == true) {
                                mListLeters_Selected?.let { list ->
                                    if (list.size > 1 && isGoingBack(item)) {
                                        goBack()
                                    } else {
                                       if(mListLeters_Selected?.contains(item) == false){
                                           mListLeters_Selected?.add(item)
                                           mPadViewListener?.onLetterAdded(item,mListLeters_Selected)
                                       }
                                    }
                                }
                                break
                            }
                        }
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mListLeters_Selected?.let {
                        mPadViewListener?.onDragCompleted(it)
                    }

                    resetAll()
                    invalidate()
                }
            }
        }
        return true
    }

    private fun initAttributes(context: Context, attr: AttributeSet) {
        mTa = context.theme.obtainStyledAttributes(
            attr,
            R.styleable.GamePadView, 0, 0
        )
        val background = mTa.getDrawable(R.styleable.GamePadView_pv_backgroundimage)
        if (background == null) {
            mBackground = BitmapFactory.decodeResource(context.resources, R.drawable.background)
        } else {
            mBackground = background.toBitmap()
        }


        //Button Glow Color
        val bgc = mTa.getColor(R.styleable.GamePadView_pv_buttonglowcolor, 0)
        if (bgc != 0) {
            mButtonGlowColor = bgc
        } else {
            mButtonGlowColor = ContextCompat.getColor(mContext, R.color.buttonglow)
        }

        //Button  Color
        val bc = mTa.getColor(R.styleable.GamePadView_pv_buttoncolor, 0)
        if (bc != 0) {
            mButtonColor = bc
        } else {
            mButtonColor = ContextCompat.getColor(mContext, R.color.buttoncolor)
        }

        //Button Stroke  Color
        val bsc = mTa.getColor(R.styleable.GamePadView_pv_buttonstrokecolor, 0)
        if (bsc != 0) {
            mButtonStrokeColor = bsc
        } else {
            mButtonStrokeColor = ContextCompat.getColor(mContext, R.color.buttonstrokecolor)
        }

        //Button Text  Color
        val btc = mTa.getColor(R.styleable.GamePadView_pv_buttontextcolor, 0)
        if (btc != 0) {
            mButtonTextColor = btc
        } else {
            mButtonTextColor = ContextCompat.getColor(mContext, R.color.buttontextcolor)
        }
        //Drag Line  Color
        val dlc = mTa.getColor(R.styleable.GamePadView_pv_draglinecolor, 0)
        if (dlc != 0) {
            mDragLineColor = dlc
        } else {
            mDragLineColor = ContextCompat.getColor(mContext, R.color.draglinecolor)
        }

        mTa.recycle()
    }

    private fun resetAll() {
        mPathDragline.reset()
        mPathDraglineTemp.reset()
        mListLeters_Selected = null
        mShouldSelectButton = false
    }

    private fun drawDragLine(canvas: Canvas?) {
        mPathDragline.reset()
        mListLeters_Selected?.let { list ->
            var counter = 1
            for (item in list) {
                item.pointf?.let {
                    if (counter == 1) {
                        mPathDragline.moveTo(it.x, it.y)
                    } else {
                        mPathDragline.lineTo(it.x, it.y)
                    }
                }
                counter++
            }
            canvas?.drawPath(mPathDragline, mPaintDragLine)
        }
    }

    private fun setBackground(canvas: Canvas?) {
        val bitmap = Bitmap.createScaledBitmap(mBackground, mPadSize, mPadSize, false)
        canvas?.drawBitmap(bitmap, 0f, 0f, null)
    }

    private fun createButton(button: PadButton): Bitmap {
        val size = mPadSize * 0.1f
        val bitmap = Bitmap.createBitmap(mPadSize, mPadSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        var paint = Paint().apply { isAntiAlias = true }
        //Drawing glowing circle
        if (mListLeters_Selected?.contains(button) == true) {
            paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 30f
                color = mButtonGlowColor
                maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
            }
            button.pointf?.let { canvas.drawCircle(it.x, it.y, size, paint) }
        }

        paint = Paint().apply {
            isAntiAlias = true
            color = mButtonColor
        }
        button.pointf?.let { canvas.drawCircle(it.x, it.y, size - 10, paint) }

        //button stroke
        paint = Paint().apply {
            isAntiAlias = true
            color = mButtonStrokeColor
            style = Paint.Style.STROKE
            strokeWidth = 20f
        }
        button.pointf?.let { canvas.drawCircle(it.x, it.y, size - 10, paint) }

        //button text
        paint = Paint().apply {
            isAntiAlias = true
            color = mButtonTextColor
            textSize = mPadSize * 0.12f
            textAlign = Paint.Align.CENTER
        }
        button.pointf?.let {
            canvas.drawText(
                button.title.toString(),
                it.x,
                (it.y + (size / 2)),
                paint
            )
        }
        return bitmap
    }

    private fun setMainCircle(): Bitmap {
        val margin = mPadSize * 0.65
        val radius = mPadSize - margin
        val centerx = mPadSize / 2f
        val centery = mPadSize / 2f
        val bitmap = Bitmap.createBitmap(mPadSize, mPadSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            strokeWidth = 0.0f
            style = Paint.Style.STROKE
            color = Color.TRANSPARENT
        }
        mPathMainCircle.addCircle(centerx, centery, radius.toFloat(), Path.Direction.CCW)
        canvas.drawPath(mPathMainCircle, paint)

        return bitmap
    }

    private fun getPoints(path: Path, size: Int): MutableList<PointF> {
        val list = mutableListOf<PointF>()
        val pm = PathMeasure(path, false)
        val aCoordinates = floatArrayOf(0f, 0f)

        for (i in 1..size) {
            val point = PointF()

            var pointat = (i / size.toFloat()) * pm.length
            pm.getPosTan(pointat, aCoordinates, null)
            point.x = aCoordinates[0]
            point.y = aCoordinates[1]
            list.add(point)
            pointat += pointat
        }
        return list
    }

    private fun isGoingBack(button: PadButton): Boolean {
        var back = false
        mListLeters_Selected?.let { list ->
            val last = list[list.size - 2]
            if (button.id == last.id) {
                back = true
            }
        }
        return back
    }

    private fun goBack() {
        mListLeters_Selected?.removeLast()
        invalidate()
    }

    //methods
    fun setWordList(level:TLevel) {
        val list_letters = mutableListOf<Char>()
        for (element in level.word) {
            val char = element
            list_letters.add(char)
        }

        for (element in list_letters) {
            val letter = element
            val button = PadButton(
                UUID.randomUUID().toString(),
                letter, null
            )
            mListLetters.add(button)
        }
    }
    fun addListener(listener: PadViewListener?){
        this.mPadViewListener = listener
    }
    fun shuffleLetters(){
        Log.e("TEST","BeforeShuffle:${mListPoints}")
        mListPoints?.shuffle()
        Log.e("TEST","AfterShuffle:${mListPoints}")
        invalidate()
    }

    private fun isCircleTouched(
        touchX: Float,
        touchY: Float,
        centerX: Float,
        centerY: Float,
        r: Float
    ): Boolean {
        val x = touchX - centerX
        val y = touchY - centerY
        return touchX > centerX && Math.sqrt((x * x + y * y).toDouble()) < r
    }
}