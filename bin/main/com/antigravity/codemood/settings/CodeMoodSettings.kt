package com.antigravity.codemood.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "com.antigravity.codemood.settings.CodeMoodSettings",
    storages = [Storage("CodeMoodSettings.xml")]
)
class CodeMoodSettings : PersistentStateComponent<CodeMoodSettings.State> {

    data class State(
        var openAiKey: String = "",
        var enableAiMotivation: Boolean = false,
        var flowThresholdMultiplier: Double = 1.2
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }
    
    companion object {
        fun getInstance(project: Project): CodeMoodSettings =
            project.getService(CodeMoodSettings::class.java)
    }
}
