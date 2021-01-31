package com.mr.pigeonone.models

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.mr.pigeonone.utils.formatAsHeader
import java.util.*


interface ChatEvent{
    val sentAt:Date
}
data class Message(
    val msg:String,
    var senderId:String,
    val msgId:String,
    val type:String="TEXT",
    val status:Int=1,
    val liked:Boolean=false,
    override val sentAt:Date=Date()
):ChatEvent{
    constructor():this("","","","",1,false, Date())
}

data class DateHeader(
    override  val sentAt: Date=Date(),val context: Context
):ChatEvent{
    @RequiresApi(Build.VERSION_CODES.N)
    val date:String=sentAt.formatAsHeader(context)
}