package catchmind;

import java.io.*;
import java.net.*;
import java.util.*;

public class CatchMindServer {
    private static final int PORT = 12346;
    private static final String ANSWER = "apple"; // 정답 단어
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientAnswer = "";

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Welcome to Catch Mind Game! Guess the word!");

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("DRAW:")) {
                        // 클라이언트가 그림을 그렸을 때 (좌표 데이터)
                        String coordinates = line.substring(5);
                        sendToAllClients("DRAW:" + coordinates);
                    } else if (line.startsWith("ANSWER:")) {
                        // 클라이언트가 정답을 제출했을 때
                        clientAnswer = line.substring(8).trim();
                        if (clientAnswer.equalsIgnoreCase(ANSWER)) {
                            sendToAllClients("CORRECT: Player guessed the word!");
                        } else {
                            sendToAllClients("INCORRECT: Player guessed wrong.");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection lost: " + socket.getInetAddress());
            } finally {
                try {
                    // 클라이언트와의 연결이 종료되었을 경우
                    socket.close();
                    clients.remove(this); // 클라이언트 목록에서 제거
                    System.out.println("Client disconnected: " + socket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendToAllClients(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }
    }
}

