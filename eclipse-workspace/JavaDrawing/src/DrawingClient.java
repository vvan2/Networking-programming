import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class DrawingClient {
    private JFrame frame;
    private JPanel drawingPanel;
    private Socket socket;
    private DataOutputStream output;
    private int lastX = -1, lastY = -1;

    public DrawingClient(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            output = new DataOutputStream(socket.getOutputStream());
            setupUI();
            new Thread(this::listenToServer).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void setupUI() {
        frame = new JFrame("Multi-client Drawing Application");
        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };

        drawingPanel.setPreferredSize(new Dimension(800, 600));
        drawingPanel.setBackground(Color.WHITE);

        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (lastX != -1 && lastY != -1) {
                    Graphics g = drawingPanel.getGraphics();
                    g.drawLine(lastX, lastY, x, y);
                    sendDrawCommand(lastX, lastY, x, y);
                }
                lastX = x;
                lastY = y;
            }
        });

        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                lastX = -1;
                lastY = -1;
            }
        });

        frame.add(drawingPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void sendDrawCommand(int x1, int y1, int x2, int y2) {
        try {
            output.writeUTF(x1 + " " + y1 + " " + x2 + " " + y2);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenToServer() {
        try (DataInputStream input = new DataInputStream(socket.getInputStream())) {
            while (true) {
                String[] drawCommand = input.readUTF().split(" ");
                int x1 = Integer.parseInt(drawCommand[0]);
                int y1 = Integer.parseInt(drawCommand[1]);
                int x2 = Integer.parseInt(drawCommand[2]);
                int y2 = Integer.parseInt(drawCommand[3]);
                SwingUtilities.invokeLater(() -> {
                    Graphics g = drawingPanel.getGraphics();
                    g.drawLine(x1, y1, x2, y2);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 5000;
        new DrawingClient(serverAddress, serverPort);
    }
}
