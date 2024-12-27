package Videocode;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;

public class VideoClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Socket socket = new Socket(SERVER_ADDRESS, PORT);
        System.out.println("Connected to server");

        InputStream inputStream = socket.getInputStream();
        BufferedImage image;

        JFrame frame = new JFrame("Video Stream");
        JLabel label = new JLabel();
        frame.getContentPane().add(label);
        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        while (true) {
            image = receiveImage(inputStream);
            label.setIcon(new ImageIcon(image));
            frame.repaint();
        }
    }

    private static BufferedImage receiveImage(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int length = dataInputStream.readInt();
        byte[] byteArray = new byte[length];
        dataInputStream.readFully(byteArray);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        return ImageIO.read(byteArrayInputStream);
    }
}
