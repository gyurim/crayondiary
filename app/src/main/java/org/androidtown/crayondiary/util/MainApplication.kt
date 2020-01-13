package org.androidtown.crayondiary.util

import android.app.Application

/* application설정, context라는 전역변수를 설정하여 혹시 ApplicationContext가 필요한 경우 사용할 수 있도록 해준다.
구글 광고 sdk나 카카오 sdk를 사용할 때
광고 모듈 초기화, 인증 정보 초기화 등을 여기서 해주라는 경우가 많아서 필요한 경우 oncreate에 로직을 추가하게 된다.
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        var context: MainApplication = MainApplication()
            private set
    }
}