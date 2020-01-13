package org.androidtown.crayondiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Diary(
    @PrimaryKey(autoGenerate = true) var id: Int = -1,
    var content: String,
    var drawFileId: String,
    var date: String,
    var weather: String,
    var screenshotId : String

    /*@PrimaryKey(autoGenerate = true)란 key가 추가되면 해당 key를 자동으로 primary로 설정
    * 자동으로 증가하는 id값
    * */
)