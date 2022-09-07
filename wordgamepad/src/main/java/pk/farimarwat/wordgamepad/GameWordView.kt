package pk.farimarwat.wordgamepad

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.TypedArrayUtils

class GameWordView constructor(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {
    private var mWord: String? = null
    private var mIsCompleted = false
    private var mBoxSize = 0
    private var mTextSize = 0
    private var mTextColor = 0
    private var mBackground:Drawable? = null

    init {
        inflate(context, R.layout.container, null)
        gravity = Gravity.CENTER
        initAttributes(attrs)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    private fun initAttributes(attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.GameWordView)
        mBoxSize = ta.getDimensionPixelSize(R.styleable.GameWordView_gwv_boxsize, 0)
        if (mBoxSize < 1) {
            mBoxSize = resources.getDimensionPixelSize(R.dimen.letter_box_size)

        }

        mTextSize = ta.getDimensionPixelSize(R.styleable.GameWordView_gwv_textsize,0)
        if(mTextSize < 1){
            mTextSize = resources.getDimensionPixelSize(R.dimen.letter_size)
        }

        mTextColor = ta.getColor(R.styleable.GameWordView_gwv_textcolor,0)
        if(mTextColor == 0){
            mTextColor = ContextCompat.getColor(context,R.color.buttontextcolor)
        }

        mBackground = ta.getDrawable(R.styleable.GameWordView_gwv_backgrounddrawable)

        if(mBackground == null){
            mBackground = ContextCompat.getDrawable(context,R.drawable.bg_letter)
        }
        mWord = ta.getString(R.styleable.GameWordView_gwv_word)
        setWord(mWord)
        ta.recycle()
    }

    fun setWord(word: String?) {
        mWord = word
        mWord?.let { w ->
            removeAllViews()
            for (char in w) {
                val buttonview = LayoutInflater.from(context).inflate(R.layout.letter, null)
                val rl_container =
                    buttonview.findViewById<RelativeLayout>(R.id.container_background)
                val params = RelativeLayout.LayoutParams(
                    mBoxSize,
                    mBoxSize
                )
                params.setMargins(10, 0, 10, 0)
                rl_container.layoutParams = params
                rl_container.background = mBackground
                val txt = buttonview.findViewById<TextView>(R.id.txt_letter)
                txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
                txt.setTextColor(mTextColor)
                if (mIsCompleted) {
                    txt.visibility = View.VISIBLE
                } else {
                    txt.visibility = View.INVISIBLE
                }
                txt.text = char.toString().uppercase()
                addView(rl_container)
            }
        }
    }

    fun setCompleted(completed: Boolean) {
        mIsCompleted = completed
        setWord(mWord)
    }

    fun getWord(): String? {
        return mWord
    }
}