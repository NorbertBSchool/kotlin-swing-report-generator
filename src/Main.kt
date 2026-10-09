import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import javax.swing.JTextArea
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

class Main : JFrame() {

    private val tabbedPane = JTabbedPane()
    private val generateButton = JButton("Generate Report in Background")
    private val newTabButton = JButton("New Tab")
    private val progressBar = JProgressBar(0, 100)
    private var reportCounter = 0

    init {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (_: Exception) {
        }

        title = "INF.04 - Report Generator"
        setSize(500, 400)
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        layout = BorderLayout()

        val menuBar = JMenuBar()
        val menuFile = JMenu("File")
        val newItem = JMenuItem("New Tab")
        val saveItem = JMenuItem("Save Report")
        val closeItem = JMenuItem("Close Tab")
        val exitItem = JMenuItem("Exit")

        newItem.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK)
        saveItem.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)
        closeItem.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK)

        newItem.addActionListener { createNewTab() }
        saveItem.addActionListener { onSaveReport() }
        closeItem.addActionListener { onCloseTab() }
        exitItem.addActionListener { dispose() }

        menuFile.add(newItem)
        menuFile.add(saveItem)
        menuFile.add(closeItem)
        menuFile.addSeparator()
        menuFile.add(exitItem)
        menuBar.add(menuFile)
        jMenuBar = menuBar

        createNewTab()
        createNewTab()
        createNewTab()
        add(tabbedPane, BorderLayout.CENTER)

        progressBar.value = 0
        progressBar.isStringPainted = true

        generateButton.addActionListener { onGenerate() }
        newTabButton.addActionListener { createNewTab() }

        val southPanel = JPanel()
        southPanel.add(generateButton)
        southPanel.add(newTabButton)
        southPanel.add(progressBar)
        add(southPanel, BorderLayout.SOUTH)
    }

    private fun createNewTab() {
        reportCounter++
        val area = JTextArea()
        area.font = Font(Font.SERIF, Font.PLAIN, 20)
        tabbedPane.addTab("Report $reportCounter", JScrollPane(area))
        tabbedPane.selectedIndex = tabbedPane.tabCount - 1
    }

    private fun getCurrentTextArea(): JTextArea? {
        val scroll = tabbedPane.selectedComponent as? JScrollPane ?: return null
        return scroll.viewport.view as? JTextArea
    }

    private fun onGenerate() {
        val area = getCurrentTextArea() ?: return
        generateButton.isEnabled = false
        area.text = ""
        progressBar.value = 0
        Worker(progressBar, generateButton, area).execute()
    }

    private fun onCloseTab() {
        val index = tabbedPane.selectedIndex
        if (index < 0) return
        if (tabbedPane.tabCount <= 1) {
            JOptionPane.showMessageDialog(
                this,
                "Cannot close the last tab.",
                "Warning",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }
        tabbedPane.removeTabAt(index)
    }

    private fun onSaveReport() {
        val area = getCurrentTextArea() ?: return
        if (area.text.isBlank()) {
            JOptionPane.showMessageDialog(
                this,
                "Nothing to save. Generate the report first.",
                "Warning",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Save report"
        fileChooser.fileFilter = FileNameExtensionFilter("Text files (*.txt)", "txt")

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return

        var fileToSave = fileChooser.selectedFile
        if (!fileToSave.name.endsWith(".txt", ignoreCase = true)) {
            fileToSave = File(fileToSave.absolutePath + ".txt")
        }

        try {
            fileToSave.writeText(area.text, Charsets.UTF_8)
            tabbedPane.setTitleAt(tabbedPane.selectedIndex, fileToSave.name)
            JOptionPane.showMessageDialog(
                this,
                "Report saved to:\n${fileToSave.absolutePath}",
                "Information",
                JOptionPane.INFORMATION_MESSAGE
            )
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to save file:\n${e.message}",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
}

fun main() {
    SwingUtilities.invokeLater { Main().isVisible = true }
}
