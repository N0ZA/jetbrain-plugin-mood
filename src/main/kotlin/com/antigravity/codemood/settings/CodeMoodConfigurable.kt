package com.antigravity.codemood.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class CodeMoodConfigurable(private val project: Project) : Configurable {

    private val mySettings = CodeMoodSettings.getInstance(project)
    
    private val myApiKeyField = JBPasswordField()
    private val myEnableAiCheckBox = JBCheckBox("Enable AI Motivation (requires OpenAI Key)")
    
    override fun getDisplayName(): String = "CodeMood"

    override fun createComponent(): JComponent {
        myApiKeyField.columns = 30
        
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("OpenAI API Key:"), myApiKeyField, 1, false)
            .addComponent(myEnableAiCheckBox, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val state = mySettings.state
        val currentKey = String(myApiKeyField.password)
        return isModified(myEnableAiCheckBox, state.enableAiMotivation) ||
                currentKey != state.openAiKey
    }

    override fun apply() {
        val state = mySettings.state
        state.enableAiMotivation = myEnableAiCheckBox.isSelected
        state.openAiKey = String(myApiKeyField.password)
    }

    override fun reset() {
        val state = mySettings.state
        myApiKeyField.text = state.openAiKey
        myEnableAiCheckBox.isSelected = state.enableAiMotivation
    }
}
