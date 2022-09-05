package pk.farimarwat.wordgame

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.children
import pk.farimarwat.wordgame.databinding.ActivityMainBinding
import pk.farimarwat.wordgamepad.GameWordView
import pk.farimarwat.wordgamepad.PadButton
import pk.farimarwat.wordgamepad.PadViewListener

class MainActivity : AppCompatActivity() {
    lateinit var mContext:Context
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mContext = this
        val list = mutableListOf<String>()

        list.add("pin")
        list.add("ink")
        list.add("nip")
        list.add("pink")
        binding.padview.setWordList(list)
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

            override fun onCompleted(list: List<PadButton>) {
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
        populateWordView(list)
        binding.imgShuffle.setOnClickListener {
            binding.padview.shuffleLetters()
        }
    }

    fun checkWord(word:String){
        for(view in binding.containerWordview.children){
            val wordview = view.findViewById<GameWordView>(R.id.gamewordview)
            val text = wordview.getWord()
            Log.e("TEST","${text}")
            if (text != null) {
                if(word.uppercase() == text.uppercase()){
                    Log.e("TEST","Word Matched")
                    wordview.setCompleted(true)
                }
            }
        }
    }

    fun populateWordView(list:List<String>){
        for(word in list){
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
            wordview.setWord(word)
            binding.containerWordview.addView(rl_container)
        }
    }
}