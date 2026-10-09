import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JProgressBar
import javax.swing.JTextArea
import javax.swing.SwingWorker

class Worker(
    private val progressBar: JProgressBar,
    private val generateButton: JButton,
    private val textArea: JTextArea
) : SwingWorker<Void?, Int>() {

    override fun doInBackground(): Void? {
        for (i in 1..10) {
            Thread.sleep(300)
            publish(i)
        }
        return null
    }

    override fun process(chunks: List<Int>) {
        val step = chunks.last()
        progressBar.value = step * 10
        textArea.append("Generating report data... ${step * 10}%\n")
        textArea.caretPosition = textArea.document.length
    }

    override fun done() {
        try {
            get()
            progressBar.value = 100
            JOptionPane.showMessageDialog(
                null,
                "Report generation completed.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE
            )
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                null,
                "Error during report generation:\n${e.cause?.message ?: e.message}",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        } finally {
            generateButton.isEnabled = true
        }
    }
}
