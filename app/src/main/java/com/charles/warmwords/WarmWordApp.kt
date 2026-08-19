package com.charles.warmwords

import android.app.Application
import com.charles.warmwords.ads.AdsManager
import com.charles.warmwords.billing.BillingManager
import com.charles.warmwords.data.local.dao.CrisisResourceDao
import com.charles.warmwords.data.repository.CrisisRepositoryImpl
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

    @Inject
    lateinit var billingManager: BillingManager

    override fun onCreate() {
        super.onCreate()

        adsManager.initialize()
        billingManager.connect()

        CoroutineScope(Dispatchers.IO).launch {
            if (crisisResourceDao.getCount() == 0) {
                crisisResourceDao.insertAll(CrisisRepositoryImpl.getDefaultResources())
            }
        }
    }
}
