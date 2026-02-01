package com.antigravity.codemood.ui

import com.antigravity.codemood.services.DailyStatsService
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel

class CircadianGraphPanel(private val stats: DailyStatsService.DailyStats?) : JPanel() {

    init {
        background = Color(20, 20, 30) // Deep Dark Background
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        val w = width
        val h = height
        val padding = 30
        
        // Title
        g2.color = Color.LIGHT_GRAY
        g2.font = Font("SansSerif", Font.PLAIN, 12)
        g2.drawString("Average Efficiency (WPM) by Hour", padding, padding - 10)
        
        // Labels
        g2.color = Color.GRAY
        g2.drawString("00:00", padding, h - 5)
        g2.drawString("12:00", w / 2, h - 5)
        g2.drawString("23:59", w - padding - 20, h - 5)

        if (stats == null) {
            g2.color = Color.GRAY
            g2.drawString("No Data Yet", w / 2 - 30, h / 2)
            return
        }

        // Calculate Max for Scaling
        var maxAvg = 10.0
        for (i in 0..23) {
            val avg = stats.getHourlyAvg(i)
            if (avg > maxAvg) maxAvg = avg
        }
        
        // Draw Bars
        val barWidth = (w - 2 * padding) / 24.0
        val scaleY = (h - 2 * padding).toDouble() / maxAvg
        
        for (hour in 0..23) {
            val avgWpm = stats.getHourlyAvg(hour)
            val barHeight = avgWpm * scaleY
            
            val x = padding + hour * barWidth
            val y = h - padding - barHeight
            
            if (avgWpm > 0) {
                // Bar Fill
                val gradient = java.awt.LinearGradientPaint(
                    x.toFloat(), y.toFloat(), x.toFloat(), (h - padding).toFloat(),
                    floatArrayOf(0f, 1f),
                    arrayOf(Color(100, 255, 100), Color(0, 150, 50)) // Green Gradient
                )
                g2.paint = gradient
                g2.fill(java.awt.geom.Rectangle2D.Double(x + 1, y, barWidth - 2, barHeight))
                
                // Bar Outline
                g2.color = Color(150, 255, 150)
                g2.stroke = BasicStroke(1f)
                g2.draw(java.awt.geom.Rectangle2D.Double(x + 1, y, barWidth - 2, barHeight))
            }
        }
        
        // Current Hour Indicator
        val currentHour = java.time.LocalTime.now().hour
        val indicatorX = padding + currentHour * barWidth + barWidth / 2
        g2.color = Color.CYAN
        g2.fillOval(indicatorX.toInt() - 3, h - padding + 2, 6, 6)
    }
}
