package com.charles.warmwords.app

import android.app.Application
import com.charles.warmwords.app.ads.AdsManager
import com.charles.warmwords.app.data.local.dao.CrisisResourceDao
import com.charles.warmwords.app.data.repository.CrisisRepositoryImpl
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class WarmWordApp : Application() {

    @Inject
    lateinit var crisisResourceDao: CrisisResourceDao

    @Inject
    lateinit var adsManager: AdsManager

    override fun onCreate() {
        super.onCreate()

        adsManager.initialize()

        CoroutineScope(Dispatchers.IO).launch {
            if (crisisResourceDao.getCount() == 0) {
                crisisResourceDao.insertAll(CrisisRepositoryImpl.getDefaultResources())
            }
        }
    }
}
