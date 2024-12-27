package Videocode;

import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;

public class VideoServer {

    private static final int PORT = 12345;
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    public static void main(String[] args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected");

        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();
        BufferedImage image;

        // 비디오 캡처 (웹캠)
        VideoCapture capture = new VideoCapture(0); // 첫 번째 웹캠
        capture.set(3, WIDTH);  // 넓이 설정
        capture.set(4, HEIGHT); // 높이 설정

        while (true) {
            // 웹캠에서 이미지 캡처
            Mat frame = new Mat();
            capture.read(frame);
            if (!frame.empty()) {
                image = matToBufferedImage(frame);
                sendImage(image, outputStream);
            }
        }
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        byte[] data = new byte[width * height * channels];
        mat.get(0, 0, data);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    private static void sendImage(BufferedImage image, OutputStream outputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(byteArray.length);
        dataOutputStream.write(byteArray);
        dataOutputStream.flush();
    }
}
