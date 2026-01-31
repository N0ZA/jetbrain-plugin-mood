package com.antigravity.codemood.listeners

import com.antigravity.codemood.services.MoodAnalysisService
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionResult
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.project.ProjectManager

class DeletionActionListener : AnActionListener {
    override fun afterActionPerformed(action: AnAction, event: AnActionEvent, result: AnActionResult) {
        // Check if the action was Backspace
        // There are a few variants, but $EditorBackSpace is the main one
        val actionId = ActionManager.getInstance().getId(action)
        
        if (IdeActions.ACTION_EDITOR_BACKSPACE == actionId || IdeActions.ACTION_EDITOR_DELETE == actionId) {
            // Find the relevant project. Since AnAction is app-level, we try to get it from context
            val project = event.project ?: ProjectManager.getInstance().openProjects.firstOrNull()
            
            project?.let {
                val service = it.getService(MoodAnalysisService::class.java)
                service?.recordDeletion()
            }
        }
    }
}
