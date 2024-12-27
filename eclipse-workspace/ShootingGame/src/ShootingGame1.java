import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

// ShootingGame 클래스
public class ShootingGame1 extends JPanel implements ActionListener, KeyListener {
    private Timer timer; // 게임 업데이트를 위한 타이머
    private Image playerImage; // 플레이어 이미지
    private Image backgroundImage; // 배경 이미지
    private Image monsterImage; // 몬스터 이미지
    private Image missileImage; // 미사일 이미지
    private int playerX, playerY; // 플레이어 위치 좌표
    private final int SPEED = 5; // 플레이어 이동 속도
    private int backgroundY; // 배경 Y 좌표
    private boolean[] keys; // 키 입력 상태 배열
    private List<Rectangle> missiles; // 미사일을 추적하는 리스트
    private Rectangle monster; // 몬스터의 위치와 크기를 추적하는 직사각형

    public ShootingGame1() {
        playerImage = new ImageIcon("images/spaceship5.png").getImage();
        backgroundImage = new ImageIcon("images/back2.png").getImage();
        monsterImage = new ImageIcon("images/monster1.png").getImage();
        missileImage = new ImageIcon("images/missile.png").getImage();
        keys = new boolean[256];
        missiles = new ArrayList<>();
        monster = new Rectangle((int) (Math.random() * (400 - monsterImage.getWidth(null))), 0, 
                                monsterImage.getWidth(null), monsterImage.getHeight(null));

        playerX = 180;
        playerY = 600;
        
        // 초기 배경 위치 설정 (이미지의 높이만큼 위로 올려 스크린 바깥에서 시작하도록)
        backgroundY = 0;

        timer = new Timer(15, this);
        timer.start();

        addKeyListener(this);
        setFocusable(true);
        setPreferredSize(new Dimension(400, 800));
    }

    private void spawnMonster() {
        monster.setLocation((int) (Math.random() * (getWidth() - monsterImage.getWidth(null))), 0);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, backgroundY, this);
        g.drawImage(playerImage, playerX, playerY, this);
        g.drawImage(monsterImage, monster.x, monster.y, this);
        
        // 미사일들을 그립니다.
        for (Rectangle missile : missiles) {
            g.drawImage(missileImage, missile.x, missile.y, this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        updatePlayerPosition();
        updateMissiles();
        checkCollisions();
        updateBackground();
        repaint();
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

    private void updateMissiles() {
        for (Iterator<Rectangle> it = missiles.iterator(); it.hasNext();) {
            Rectangle missile = it.next();
            missile.y -= 10; // 미사일 속도
            if (missile.y < 0) {
                it.remove(); // 화면 상단을 벗어난 미사일을 제거합니다.
            }
        }
    }

    private void checkCollisions() {
        for (Iterator<Rectangle> it = missiles.iterator(); it.hasNext();) {
            Rectangle missile = it.next();
            if (monster.intersects(missile)) {
                it.remove(); // 몬스터에 맞은 미사일을 제거합니다.
                spawnMonster(); // 새 몬스터를 생성합니다.
                break; // 한 번에 하나의 미사일만 처리합니다.
            }
        }
    }

    // 배경의 위치를 업데이트하여 스크롤링 효과를 주는 메소드
    public void updateBackground() {
        backgroundY += 1; // 배경을 아래로 1픽셀씩 이동
        if (backgroundY > 0) {
            // 배경이 아래로 완전히 내려오면 다시 위로 설정
            backgroundY = -backgroundImage.getHeight(null) + getHeight();
        }
    }

    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // 미사일 발사: 플레이어 위치에서 시작하는 새 미사일을 추가합니다.
            missiles.add(new Rectangle(playerX + playerImage.getWidth(null) / 2 - missileImage.getWidth(null) / 2,
                                       playerY, missileImage.getWidth(null), missileImage.getHeight(null)));
        }
    }

    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    public void keyTyped(KeyEvent e) { }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Shooting Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ShootingGame1 gamePanel = new ShootingGame1();
        frame.add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        gamePanel.requestFocusInWindow();
    }
}
