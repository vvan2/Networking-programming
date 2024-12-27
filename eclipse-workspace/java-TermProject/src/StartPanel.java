// WebcamClient.java
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
public class StartPanel extends JPanel {
    private URL backGroundGif=null;//첫 장면의 배경에 사용될 url이다.
	private ImageIcon backGroundIcon=null;//배경 아이콘
	private Image backGroundImage=null;//배경 사진
	
	private JLabel userName=new JLabel("User Name");
	private JLabel ipAddress=new JLabel("IP Address");
	private JLabel portNumber=new JLabel("Port Number");
	
	private JTextField userNameTextField=new JTextField();
	private JTextField ipAddressTextField=new JTextField();
	private JTextField portNumberTextField=new JTextField();
	
	private JButton startButton = new JButton("Start");
	
	 public StartPanel(JFrame myJFrame) throws MalformedURLException {
       backGroundGif = new URL("https://media.fmkorea.com/files/attach/new/20170207/486616/478132416/578029390/2a71c7fe2a9506b31ff99fe870fa42b5.gif");
       backGroundIcon = new ImageIcon(backGroundGif);
       backGroundImage = backGroundIcon.getImage();
       
       setLayout(null);
       
       // Labels - 왼쪽 정렬
       userName.setSize(150, 50);
       userName.setLocation(200, 250);
       userName.setFont(new Font("맑은 고딕", Font.BOLD, 20));
       userName.setForeground(new Color(165, 216, 243)); 
       add(userName);
       
       ipAddress.setSize(150, 50);
       ipAddress.setLocation(200, 320);
       ipAddress.setFont(new Font("맑은 고딕", Font.BOLD, 20));
       ipAddress.setForeground(new Color(165, 216, 243));
       add(ipAddress);
       
       portNumber.setSize(150, 50);
       portNumber.setLocation(200, 390);
       portNumber.setFont(new Font("맑은 고딕", Font.BOLD, 20));
       portNumber.setForeground(new Color(165, 216, 243));
       add(portNumber);
       
       // TextFields - 라벨 옆에 배치
       userNameTextField.setSize(350, 40);
       userNameTextField.setLocation(370, 255);
       userNameTextField.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
       add(userNameTextField);
       
       ipAddressTextField.setSize(350, 40);
       ipAddressTextField.setLocation(370, 325);
       ipAddressTextField.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
       add(ipAddressTextField);
       
       portNumberTextField.setSize(350, 40);
       portNumberTextField.setLocation(370, 395);
       portNumberTextField.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
       add(portNumberTextField);
       
       // Start Button
       startButton.setSize(200, 50);
       startButton.setLocation(350, 480);
       startButton.setFont(new Font("맑은 고딕", Font.BOLD, 24));
       startButton.setBackground(new Color(100, 194, 249));
       startButton.setForeground(Color.WHITE);
       startButton.setBorderPainted(false);
       startButton.setFocusPainted(false);
       add(startButton);
       
       startButton.addActionListener(new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			String userNameText=userNameTextField.getText().trim();
			String ipAddressText=ipAddressTextField.getText().trim();
			String portNumberText=portNumberTextField.getText().trim();
			
			//빈칸을 입력했을경우.
			if(userNameText.equals("")||ipAddressText.equals("")||portNumberText.equals("")) {
				 JOptionPane.showMessageDialog(
				            null,                       // 부모 컴포넌트 (null이면 기본 화면 중앙에 표시)
				            "빈칸이 존재합니다. 빈칸 없이 모두 입력해주세요!!",         // 메시지 내용
				            "입력 확인",                // 다이얼로그 제목
				            JOptionPane.WARNING_MESSAGE // 다이얼로그 아이콘 (경고 아이콘)
				        );
				 return;
			}
			
			//빈칸없이 입력했다면 다음화면으로 넘어간다.
			Container c=StartPanel.this.getParent();
			c.remove(StartPanel.this);
			c.revalidate();
			c.repaint();
			MainPanelWrapper mainPanelWrapper=new  MainPanelWrapper(userNameText, ipAddressText, portNumberText, myJFrame);

			
		}
	});
       
       // 버튼에 마우스 이벤트 추가
       startButton.addMouseListener(new MouseAdapter() {
           public void mouseEntered(MouseEvent e) {
               startButton.setBackground(new Color(81, 177, 230)); // 마우스 오버시 더 진한 핑크
           }
           public void mouseExited(MouseEvent e) {
               startButton.setBackground(new Color(100, 194, 249)); // 원래 색으로
           }
       });
   }
   
   public void paintComponent(Graphics g) {
       super.paintComponent(g);
       g.drawImage(backGroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
       g.setColor(new Color(12, 123, 214)); // 타이틀도 같은 색으로
       g.setFont(new Font("고령딸기체", Font.ITALIC, 70));
       g.drawString("몸으로 말해요", this.getWidth()/3, 150);
   }
    
}