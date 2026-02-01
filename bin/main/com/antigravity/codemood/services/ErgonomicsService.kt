package com.antigravity.codemood.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import javax.swing.Timer

@Service(Service.Level.PROJECT)
class ErgonomicsService(private val project: Project) {

    // 20 minutes in ms = 20 * 60 * 1000 = 1,200,000
    // specific timing for demo purposes: 10 seconds? No, let's stick to real logic but maybe shorter for testing.
    private val interval = 20 * 60 * 1000 
    
    private val timer = Timer(interval) {
        triggerNotification()
    }

    init {
        timer.start()
    }

    private fun triggerNotification() {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("CodeMood Notifications")
            .createNotification(
                "👀 20-20-20 Rule",
                "Look away at something 20 feet away for 20 seconds!",
                NotificationType.INFORMATION
            )
        
        notification.notify(project)
    }
}
