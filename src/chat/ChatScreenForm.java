package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

/**
 * Created by weijiangan on 14/10/2016.
 */
public class ChatScreenForm {
    private JTextArea taConvo;
    private JTextArea taMessage;
    private JButton sendButton;
    private JButton broadcastButton;
    private JPanel chatPanel;
    private Socket socket;
    private User loginUser;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Message message;
    private JFrame frame;
    private JFileChooser fileChooser;

    public ChatScreenForm() {
        frame = new JFrame("Chat");
        frame.setContentPane(chatPanel);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getRootPane().setDefaultButton(sendButton);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String txt = taMessage.getText();
                    String id = loginUser.getId();
                    message = new Message(id, txt);
                    out.writeObject(message);
                    taConvo.append(LocalDateTime.now() + "  " + id + ": " + txt + "\n");
                    taMessage.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Exception occurred: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        broadcastButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String txt = taMessage.getText();
                    String id = loginUser.getId();
                    message = new Message(id, txt);
                    message.setBroadcast(true);
                    out.writeObject(message);
                    taConvo.append(LocalDateTime.now() + "  " + id + ": " + txt + "\n");
                    taMessage.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Exception occurred: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        taMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == '\n') {
                    sendButton.doClick();
                }
            }
        });
        frame.pack();
        frame.setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        JMenuItem miSave = new JMenuItem("Save conversation...");
        JMenuItem miExit = new JMenuItem("Exit");
        miSave.addActionListener((ActionEvent e) -> {
            fileChooser = new JFileChooser();
            int returnVal = fileChooser.showSaveDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    FileWriter fw = new FileWriter(fileChooser.getSelectedFile() + ".txt");
                    fw.write(taConvo.getText());
                    fw.flush();
                    fw.close();
                } catch (Exception ex) {
                    System.out.println("Exception occurred " + ex);
                }
            }
        });
        miExit.setMnemonic(KeyEvent.VK_E);
        miExit.addActionListener((ActionEvent e) -> {
            System.exit(0);
        } );

        file.add(miSave);
        file.add(miExit);
        menuBar.add(file);
        frame.setJMenuBar(menuBar);
    }

    public void run() throws Exception {
        final int portNum = 8080;
        String ipAddress = (String) JOptionPane.showInputDialog(null,
                "Enter IP address of server",
                "Server Connection\n", JOptionPane.INFORMATION_MESSAGE, null,
                null, "127.0.0.1");
        socket = new Socket(ipAddress, portNum);
        out = new ObjectOutputStream(socket.getOutputStream());
        int receivedSignal;
        loginUser = new User();
        while (true) {
            new LoginForm();
            out.writeObject(loginUser);
            in = new ObjectInputStream(socket.getInputStream());
            message = (Message) in.readObject();
            receivedSignal = Integer.parseInt(message.getMsg());
            if (receivedSignal > 0) {
                break;
            } else {
                switch (receivedSignal) {
                    case 0:
                        JOptionPane.showMessageDialog(null, "User not found! Please try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    case -1:
                        JOptionPane.showMessageDialog(null, "Invalid password! Please try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    case -2:
                        JOptionPane.showMessageDialog(null, "User already logged in! Please log out first.", "Login Error", JOptionPane.ERROR_MESSAGE);
                        break;
                }
            }
        }

        if (receivedSignal == 1) {
            sendButton.setEnabled(false);
            broadcastButton.setEnabled(false);
            taConvo.append("Waiting for agent... Please be patient\n");
            message = (Message) in.readObject();
            taConvo.append(message.getLdt() + "  " + message.getFr() + ": " + message.getMsg() + "\n");
            out.writeObject(new Message(loginUser.getId(), message.getFr()));
            sendButton.setEnabled(true);
        } else {
            createMenuBar();
            Dimension curSize = frame.getSize();
            curSize.height += 20;
            frame.setSize(curSize);
            taConvo.append("Finding customer in queue...\n");
        }

        while (true) {
            message = (Message) in.readObject();
            taConvo.append(message.getLdt() + "  " + message.getFr() + ": " + message.getMsg() + "\n");
        }
    }

    public static void main(String[] args) {
        ChatScreenForm chatScreen = new ChatScreenForm();
        try {
            chatScreen.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Exception occurred: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class LoginForm {
        private JTextField usernameTextField;
        private JPanel panel1;
        private JButton loginButton;
        private JPasswordField passwordPasswordField;
        private JDialog dialog;

        LoginForm() throws Exception {
            dialog = new JDialog(frame, "Login", true);
            dialog.setLocationRelativeTo(dialog);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setContentPane(panel1);
            loginButton.setEnabled(false);

            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loginUser.setId(usernameTextField.getText());
                    loginUser.setPw(new String(passwordPasswordField.getPassword()));
                    dialog.dispose();
                }
            });

            usernameTextField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String temp = usernameTextField.getText();
                    if (!temp.equals("") && !temp.equals("username")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }

                }
            });
            passwordPasswordField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String temp = new String(passwordPasswordField.getPassword());
                    if (!temp.equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                }
            });
            dialog.pack();
            dialog.setVisible(true);
        }

    }
}
