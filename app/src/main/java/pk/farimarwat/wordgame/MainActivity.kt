package pk.farimarwat.wordgame

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.view.children
import pk.farimarwat.wordgame.databinding.ActivityMainBinding
import pk.farimarwat.wordgamepad.*

class MainActivity : AppCompatActivity() {
    lateinit var mContext:Context
    lateinit var mTLevel:TLevel
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mContext = this
        val list_answers = mutableListOf<TAnswer>()
        list_answers.add(TAnswer("CUP",false))
        list_answers.add(TAnswer("UP",false))
        mTLevel = TLevel("UCP",list_answers)
        binding.padview.setWordList(mTLevel)
        binding.padview.addListener(object :PadViewListener{
            override fun onLetterAdded(item: PadButton, selected: List<PadButton>?) {
               var word = ""
                selected?.let {
                    for(item in it){
                        word += item.title.toString()
                    }
                    binding.txtSelectedLetters.apply {
                        visibility = View.VISIBLE
                        text = word
                    }
                }
            }

            override fun onDragCompleted(list: List<PadButton>) {
                binding.txtSelectedLetters.visibility = View.GONE
                if(list.isNotEmpty()){
                    var word = ""
                    for(item in list){
                        word += item.title
                    }
                    checkWord(word)
                }
            }

        })
        populateWordView(mTLevel)
        binding.imgShuffle.setOnClickListener {
            binding.padview.shuffleLetters()
        }
    }

    fun checkWord(word:String){
        var completedword:String? = null
        for(view in binding.containerWordview.children){
            val wordview = view.findViewById<GameWordView>(R.id.gamewordview)
            val text = wordview.getWord()
            if (text != null) {
                if(word.uppercase() == text.uppercase()){
                    wordview.setCompleted(true)
                    completedword = word
                    break
                }
            }
        }
        completedword?.let {
            for(answer in mTLevel.listanswers){
                if(answer.answer.uppercase() == it.uppercase()){
                    answer.iscompleted = true
                    break
                }
            }
        }
        val incompleteanswer = mTLevel.listanswers.find {
            it.iscompleted == false
        }
        if(incompleteanswer == null){
            Toast.makeText(mContext,"Level Completed",Toast.LENGTH_LONG).show()
        }
    }

    fun populateWordView(level:TLevel){
        for(answer in level.listanswers){
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_wordview,null)
            val rl_container = view.findViewById<RelativeLayout>(R.id.container_wordview)
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(10,10,10,10)
            rl_container.layoutParams = params
            val wordview = view.findViewById<GameWordView>(R.id.gamewordview)
            wordview.setWord(answer.answer)
            binding.containerWordview.addView(rl_container)
        }
    }
}