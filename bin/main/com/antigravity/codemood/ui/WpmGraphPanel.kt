package com.antigravity.codemood.ui

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.util.LinkedList
import javax.swing.JPanel

class WpmGraphPanel : JPanel() {
    private val history = LinkedList<Int>()
    private val maxHistory = 60 
    
    init {
        background = Color(20, 20, 30) // Deep Dark Background
    }
    
    fun addValue(wpm: Int) {
        history.add(wpm)
        if (history.size > maxHistory) {
            history.removeFirst()
        }
        repaint()
    }
    
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        val w = width
        val h = height
        val padding = 20
        
        // 1. Draw Subtle Grid
        g2.color = Color(40, 40, 50)
        for (i in 0..5) {
            val y = padding + i * (h - 2 * padding) / 5
            g2.drawLine(padding, y, w - padding, y)
        }
        
        if (history.isEmpty()) return
        
        // Find Scale
        val maxVal = history.maxOrNull() ?: 1
        val scaleY = (h - 2 * padding).toDouble() / Math.max(70, maxVal)
        val scaleX = (w - 2 * padding).toDouble() / (maxHistory - 1)
        
        // 2. Create Layout Path
        val path = java.awt.geom.Path2D.Double()
        val fillPath = java.awt.geom.Path2D.Double()
        
        fillPath.moveTo(padding.toDouble(), (h - padding).toDouble())
        
        for (i in history.indices) {
            val wpm = history[i]
            val x = padding + i * scaleX
            val y = h - padding - (wpm * scaleY)
            
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        
        // Close fill path
        fillPath.lineTo((padding + (history.size - 1) * scaleX), (h - padding).toDouble())
        fillPath.closePath()
        
        // 3. Draw Gradient Fill (Cyberpunk Cyan)
        val gradient = java.awt.LinearGradientPaint(
            0f, 0f, 0f, h.toFloat(),
            floatArrayOf(0f, 1f),
            arrayOf(Color(0, 255, 255, 60), Color(0, 255, 255, 0)) // Cyan fading to transparent
        )
        g2.paint = gradient
        g2.fill(fillPath)
        
        // 4. Draw Neon Glow Line
        // Outer glow
        g2.stroke = BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g2.color = Color(0, 255, 255, 80)
        g2.draw(path)
        
        // Inner core
        g2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g2.color = Color(0, 255, 255) // Bright Cyan
        g2.draw(path)
    }
}
