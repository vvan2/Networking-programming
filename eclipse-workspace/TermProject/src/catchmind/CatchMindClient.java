package catchmind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class CatchMindClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12346;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JTextField answerField;
    private JPanel drawingPanel;
    private JLabel statusLabel;

    public CatchMindClient() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            JFrame frame = new JFrame("Catch Mind Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());

            // 그림 그리기 패널
            drawingPanel = new JPanel() {
                private int x = -1, y = -1;

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                }

                public void draw(int x, int y) {
                    Graphics g = getGraphics();
                    g.fillOval(x, y, 8, 8); // 작은 원을 그리기
                    out.println("DRAW:" + x + "," + y); // 서버로 좌표 전송
                }
            };
            drawingPanel.setBackground(Color.WHITE);
            drawingPanel.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    drawingPanel.draw(e.getX(), e.getY());
                }
            });

            // 정답 입력 필드와 제출 버튼
            JPanel answerPanel = new JPanel();
            answerField = new JTextField(20);
            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    submitAnswer();
                }
            });

            statusLabel = new JLabel("Guess the word!");

            answerPanel.add(new JLabel("Your Answer: "));
            answerPanel.add(answerField);
            answerPanel.add(submitButton);

            frame.add(drawingPanel, BorderLayout.CENTER);
            frame.add(answerPanel, BorderLayout.SOUTH);
            frame.add(statusLabel, BorderLayout.NORTH);

            frame.setVisible(true);

            // 서버로부터 메시지 수신
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            if (message.startsWith("DRAW:")) {
                                // 서버에서 그림을 그리는 데이터를 받았을 때
                                String coordinates = message.substring(5);
                                String[] parts = coordinates.split(",");
                                int x = Integer.parseInt(parts[0]);
                                int y = Integer.parseInt(parts[1]);
                                drawingPanel.draw(x, y);
                            } else if (message.startsWith("CORRECT:")) {
                                statusLabel.setText("Correct! You guessed the word.");
                            } else if (message.startsWith("INCORRECT:")) {
                                statusLabel.setText("Incorrect. Try again!");
                            } else {
                                statusLabel.setText(message);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    private void submitAnswer() {
        String answer = answerField.getText().trim();
        out.println("ANSWER:" + answer); // 서버로 정답 전송
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CatchMindClient();
            }
        });
    }
}
