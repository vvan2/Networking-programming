
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

// ShootingGame 클래스
public class ShootingGame extends JPanel implements ActionListener, KeyListener {
    private Timer timer; // 게임 업데이트를 위한 타이머
    private Image playerImage; // 플레이어 비행기 이미지
    private Image backgroundImage; // 배경 이미지
    private int playerX, playerY; // 플레이어 위치 좌표
    private final int SPEED = 5; // 플레이어의 이동 속도
    private int backgroundY; // 배경의 Y 좌표 (스크롤링을 위함)
    private boolean[] keys; // 키 입력 상태를 추적하는 불리언 배열


    public ShootingGame() {
        playerImage = new ImageIcon("images/spaceship5.png").getImage(); // 플레이어 이미지 로드
        backgroundImage = new ImageIcon("images/back2.png").getImage(); // 배경 이미지 로드
        keys = new boolean[256]; // 키 입력 상태를 저장할 충분한 크기의 배열을 초기화

        // 초기 플레이어 위치 설정
        playerX = 180;
        playerY = 600;
        
        // 초기 배경 위치 설정 (이미지의 높이만큼 위로 올려 스크린 바깥에서 시작하도록)
        backgroundY = 0;

        timer = new Timer(5, this); // 타이머를 5ms 간격으로 설정, 두번째 인자는 ActionListener를 구현한 현재 객체
        timer.start(); // 타이머 시작

        addKeyListener(this); // 키 리스너 추가
        setFocusable(true); // 키 입력을 받기 위해 포커스 가능하도록 설정
        setPreferredSize(new Dimension(400, 800)); // 패널의 선호 사이즈 설정
    }

    // 컴포넌트를 그릴 때 호출되는 메소드
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // 상위 클래스의 paintComponent 호출
        
        // 배경 이미지를 그립니다.
        g.drawImage(backgroundImage, 0, backgroundY, this);
        // 플레이어 이미지를 그립니다.
        g.drawImage(playerImage, playerX, playerY, this);
    }

    // ActionListener 인터페이스를 구현한 메소드, 타이머 이벤트가 발생할 때마다 호출
    public void actionPerformed(ActionEvent e) {
        updatePlayerPosition(); // 플레이어 위치를 업데이트
        updateBackground(); // 배경을 업데이트
        repaint(); // 패널을 다시 그림 (paintComponent 호출)
    }

    // 플레이어의 위치를 업데이트하는 메소드
    private void updatePlayerPosition() {
        if (keys[KeyEvent.VK_LEFT]) {
            playerX -= SPEED; // 왼쪽 키가 눌리면 왼쪽으로 이동
        }
        if (keys[KeyEvent.VK_RIGHT]) {
            playerX += SPEED; // 오른쪽 키가 눌리면 오른쪽으로 이동
        }
        if (keys[KeyEvent.VK_UP]) {
            playerY -= SPEED; // 위쪽 키가 눌리면 위로 이동
        }
        if (keys[KeyEvent.VK_DOWN]) {
            playerY += SPEED; // 아래쪽 키가 눌리면 아래로 이동
        }
        
        // 플레이어가 창의 경계를 넘지 않도록 위치를 조정
        playerX = Math.max(playerX, 0);
        playerX = Math.min(playerX, getWidth() - playerImage.getWidth(null));
        playerY = Math.max(playerY, 0);
        playerY = Math.min(playerY, getHeight() - playerImage.getHeight(null));
    }

    // 배경의 위치를 업데이트하여 스크롤링 효과를 주는 메소드
    public void updateBackground() {
        backgroundY += 1; // 배경을 아래로 1픽셀씩 이동
        if (backgroundY > 0) {
            // 배경이 아래로 완전히 내려오면 다시 위로 설정
            backgroundY = -backgroundImage.getHeight(null) + getHeight();
        }
    }

    // KeyListener 인터페이스를 구현한 메소드, 키가 눌렸을 때 호출
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true; // 해당 키가 눌렸다면 배열에 true를 설정
    }

    // KeyListener 인터페이스를 구현한 메소드, 키에서 손을 떼었을 때 호출
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false; // 해당 키에서 손을 떼었다면 배열에 false를 설정
    }

    // KeyListener 인터페이스의 메소드, 키 타이핑 이벤트를 처리, 여기서는 구현x
    public void keyTyped(KeyEvent e) { }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Shooting Game"); // 게임 윈도우를 생성
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 윈도우 닫힐 때 프로그램 종료
        ShootingGame gamePanel = new ShootingGame(); // 게임 패널 객체 생성
        frame.add(gamePanel); // 프레임에 게임 패널 추가
        frame.pack(); 
        frame.setLocationRelativeTo(null); // 윈도우를 화면 가운데에 위치
        frame.setVisible(true); // 윈도우를 보이게 설정
        gamePanel.requestFocusInWindow(); // 키 입력을 받기 위해 게임 패널에 포커스 요청
    }
}
