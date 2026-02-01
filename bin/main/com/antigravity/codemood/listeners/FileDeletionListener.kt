package com.antigravity.codemood.listeners

import com.antigravity.codemood.services.MoodAnalysisService
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager

class FileDeletionListener(private val project: Project) : DocumentListener {
    // Note: We need a way to connect this. 
    // Usually registered via EditorFactory.eventMulticaster or directly on documents.
    // For simplicity sake in plugin.xml, we usually use an application/project level listener if available, 
    // or register via a StartupActivity. 
    // But let's try to register it as a project listener in plugin.xml if possible or use EditorFactoryListener.
    
    override fun documentChanged(event: DocumentEvent) {
        if (event.oldLength > event.newLength) {
            // It was a deletion
            val service = project.getService(MoodAnalysisService::class.java)
            service?.recordDeletion()
        }
    }
}
