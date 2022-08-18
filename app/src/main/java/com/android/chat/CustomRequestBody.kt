package com.android.chat

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class CustomRequestBody(private val file: File, private val content_type:String, private val progressData:(now:Long, total:Long)->Unit):RequestBody() {
    override fun contentType(): MediaType? = MediaType.parse(content_type)

    override fun writeTo(sink: BufferedSink) {

        val inputFile = FileInputStream(file)
        val c = ByteArray(1048)
        var buffer = 0L
        var read = 0
        while (inputFile.read(c).also { read = it } != -1){
            buffer += read
            sink.write(c,0,read)
            progressData.invoke(buffer, file.length())

        }
    }
}