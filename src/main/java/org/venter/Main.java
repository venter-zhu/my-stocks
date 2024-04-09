package org.venter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author venter.zhu
 * @date 2024/3/14 17:28
 */
public class Main {

    public static void hello() {
        JFrame frame = new JFrame("File Chooser Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        JButton chooseFileButton = new JButton("Choose File");
        JButton confirmButton = new JButton("Confirm");
        confirmButton.setEnabled(false);

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    confirmButton.setEnabled(true);
                }
            }
        });

        confirmButton.addActionListener(e -> {
            // 在这里执行确认按钮点击后的操作
            JOptionPane.showMessageDialog(null, "Confirmed!");
        });

        panel.add(chooseFileButton);
        panel.add(confirmButton);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Test test = new Test();
            test.setVisible(true);
            test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }

    public JPanel getPanel() {
        JPanel panel1 = new JPanel();
        JButton button1 = new JButton("Open File Chooser 1");
        JLabel label1 = new JLabel("Selected File Path 1: ");
        ActionListener buttonListener = e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                String selectedFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                JButton sourceButton = (JButton) e.getSource();
                if (sourceButton == button1) {
                    label1.setText("Selected File Path 1: " + selectedFilePath);
                }
            }
        };
        button1.addActionListener(buttonListener);
        panel1.add(label1);
        panel1.add(button1);
        return panel1;
    }
}
