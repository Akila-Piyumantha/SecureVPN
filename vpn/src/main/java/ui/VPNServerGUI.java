package ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VPNServerGUI extends JFrame {
    private JTextArea logArea;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;

    public VPNServerGUI() {
        setTitle("VPN Server");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);

        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(clientList), new JScrollPane(logArea));
        splitPane.setDividerLocation(150);

        add(splitPane, BorderLayout.CENTER);
        setVisible(true);
    }

    public void updateClients(List<String> clients) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.clear();
            for (String client : clients) {
                clientListModel.addElement(client);
            }
        });
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}
