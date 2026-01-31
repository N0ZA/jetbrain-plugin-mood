package com.antigravity.codemood.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

class BreathingExerciseDialog(project: Project?) : DialogWrapper(project) {

    init {
        title = "Take a Deep Breath 🌿"
        init()
        isModal = true
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = JBUI.size(400, 300)
        
        val instructionLabel = JLabel("Inhale... Exhale...", SwingConstants.CENTER)
        instructionLabel.font = instructionLabel.font.deriveFont(20f)
        panel.add(instructionLabel, BorderLayout.NORTH)

        val animationPanel = BreathingAnimationPanel(instructionLabel)
        panel.add(animationPanel, BorderLayout.CENTER)

        return panel
    }
    
    // Custom Actions (buttons)
    override fun createActions(): Array<Action> {
        return arrayOf(okAction) // Just an "OK" / "I feel better" button
    }
}

private class BreathingAnimationPanel(private val label: JLabel) : JPanel() {
    private var scale = 0.5f
    private var growing = true
    private val timer: Timer

    init {
        // 50ms per frame = 20fps
        timer = Timer(50) {
            if (growing) {
                scale += 0.01f
                if (scale >= 1.0f) {
                    scale = 1.0f
                    growing = false
                    label.text = "Exhale..."
                }
            } else {
                scale -= 0.01f
                if (scale <= 0.4f) {
                    scale = 0.4f
                    growing = true
                    label.text = "Inhale..."
                }
            }
            repaint()
        }
        timer.start()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val size = Math.min(width, height) - 40
        val circleSize = (size * scale).toInt()
        val x = (width - circleSize) / 2
        val y = (height - circleSize) / 2

        // Calming Blue/Green Gradient
        val color = Color(100, 200, 255, 150)
        g2.color = color
        g2.fillOval(x, y, circleSize, circleSize)
    }
}
