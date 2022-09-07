package pk.farimarwat.wordgamepad

import android.graphics.PointF

data class PadButton(
    var id:String,
    var title:Char,
    var pointf: PointF?
)
