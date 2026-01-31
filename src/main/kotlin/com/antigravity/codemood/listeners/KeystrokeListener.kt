package com.antigravity.codemood.listeners

import com.antigravity.codemood.services.MoodAnalysisService
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class KeystrokeListener : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        val service = project.getService(MoodAnalysisService::class.java)
        service?.recordKeystroke()
        return Result.CONTINUE
    }
}
