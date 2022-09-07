package pk.farimarwat.wordgamepad

interface PadViewListener {
    fun onLetterAdded(item:PadButton, selected:List<PadButton>?)
    fun onDragCompleted(list:List<PadButton>)
}