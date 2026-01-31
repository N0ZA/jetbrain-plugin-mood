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
    private val maxHistory = 60 // 1 minute of seconds view, or more? Let's keep 60 points
    
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
        
        if (history.isEmpty()) return
        
        val w = width
        val h = height
        val padding = 10
        
        // Find Scale
        val maxVal = history.maxOrNull() ?: 1
        val scaleY = (h - 2 * padding).toDouble() / Math.max(10, maxVal) // Minimum scale 10 WPM
        val scaleX = (w - 2 * padding).toDouble() / (maxHistory - 1)
        
        // Draw Grid
        g2.color = Color.GRAY.brighter()
        g2.drawRect(padding, padding, w - 2 * padding, h - 2 * padding)
        
        // Draw Line
        g2.stroke = BasicStroke(2f)
        g2.color = Color(30, 144, 255) // Dodger Blue
        
        val path = java.awt.geom.Path2D.Double()
        
        for (i in history.indices) {
            val valIndex = i
            val wpm = history[i]
            
            val x = padding + valIndex * scaleX
            val y = h - padding - (wpm * scaleY)
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        g2.draw(path)
    }
}
