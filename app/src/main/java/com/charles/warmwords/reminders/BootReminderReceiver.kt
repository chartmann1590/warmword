package com.charles.warmwords.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.charles.warmwords.domain.usecase.SessionReminderUseCases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sessionReminderUseCases: SessionReminderUseCases

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sessionReminderUseCases.getUpcoming().forEach { reminder ->
                    reminderScheduler.schedule(reminder.id, reminder.timestamp, reminder.label)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
