
//이 패널에서, 방목록 보는 패널 , 게임보는 패널 등등 작성할것이다.

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.github.sarxos.webcam.Webcam;



public class MainPanelWrapper extends JPanel{
	
    private String userName, ipAddress;
    private int portNumber;
    private JFrame mainFrame;//화면이 전환되는 메인프레임
    private MessageThread messageThread;//서버에서 메시지를 받는 스레드
    private volatile boolean isRunning = true;//스레드 종료시키기
    
    //MainPanelWrapper 생성자에서 초기화 완료.
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    
    
    private MainPanel mainPanel;
    private GamePanel gamePanel;
    
    public MainPanelWrapper(String userName, String ipAddress, String portNumber, JFrame mainFrame) {
    	this.mainFrame=mainFrame;
    	this.userName = userName;
        this.ipAddress = ipAddress;
        System.out.println();
        try {
        	this.portNumber = Integer.parseInt(portNumber);
            socket = new Socket(this.ipAddress, this.portNumber);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            
            output.writeUTF(userName);//연결되자마자 닉네임을 보낸다.
            
            mainPanel=new MainPanel();
     
			Container c=mainFrame.getContentPane();
			c.add(mainPanel);
			c.revalidate();
			c.repaint();
            
            messageThread=new MessageThread();
            messageThread.start();
        }
        catch (Exception e) {
         
        	System.out.println(e);
			try {
				   
		            Container c = mainFrame.getContentPane();
		            c.removeAll();
		            StartPanel errorStartPanel;
		            errorStartPanel = new StartPanel(mainFrame);
		            c.add(errorStartPanel);
		            c.revalidate();
		            c.repaint();
		            JOptionPane.showMessageDialog(null, "서버에 연결할 수 없습니다!!");
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
          
            return;
        }
    }
    
    //MainPanel
    public class MainPanel extends JPanel{
    	 private List<RoomInfo> rooms = new ArrayList<>();
    	 private JPanel roomListPanel;
    	 private JButton createRoomBtn;
         private JScrollPane scrollPane; 
         
    	public MainPanel() {
    		 setLayout(new BorderLayout(0, 10));
             setBackground(new Color(249, 250, 251));
             
             //타이블바 세팅
             JPanel titlePanel = new JPanel(new BorderLayout());
             titlePanel.setBackground(Color.WHITE);
             titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
             JLabel titleLabel = new JLabel("게임 방 목록");
             titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
             titlePanel.add(titleLabel, BorderLayout.WEST);
             add(titlePanel, BorderLayout.NORTH); 
            
             //방 리스트를 보여주는 패널
             roomListPanel = new JPanel();
             roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
             roomListPanel.setBackground(new Color(249, 250, 251));


       
             // 스크롤팬인데, roomListPanel를 보여주는 스크롤팬
             scrollPane = new JScrollPane(roomListPanel);
             scrollPane.setBorder(null);
             scrollPane.setPreferredSize(new Dimension(getWidth(), 500)); // 스크롤팬 높이 고정
             scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
             scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
             add(scrollPane, BorderLayout.CENTER);
              
             
             roomListPanel.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), roomListPanel.getPreferredSize().height));
             roomListPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, roomListPanel.getPreferredSize().height));
             
          
             // 하단 버튼
             createRoomBtn = new JButton("+ 방 만들기");
             createRoomBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
             createRoomBtn.setPreferredSize(new Dimension(120, 35));
             createRoomBtn.setBackground(new Color(66, 133, 244));
             createRoomBtn.setForeground(Color.WHITE);
             createRoomBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
             createRoomBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
             createRoomBtn.setFocusPainted(false);

             JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
             buttonPanel.setBackground(Color.WHITE);
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
         
             
             JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
             bottomPanel.setBackground(new Color(255, 255, 255));
             bottomPanel.add(createRoomBtn);
             add(bottomPanel, BorderLayout.SOUTH);
             
             createRoomBtn.addActionListener(e -> {
            	    // 커스텀 다이얼로그 패널 생성
            	    JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
            	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            	    // 방 이름 입력 관련 컴포넌트
            	    JLabel nameLabel = new JLabel("방 이름:");
            	    JTextField roomNameField = new JTextField();
            	    
            	    // 카테고리 선택 관련 컴포넌트
            	    JLabel categoryLabel = new JLabel("카테고리:");
            	    String[] categories = {"영화", "드라마", "사물", "스포츠", "음식"};
            	    JComboBox<String> categoryCombo = new JComboBox<>(categories);
            	    
            	    // 스타일링
            	    roomNameField.setPreferredSize(new Dimension(200, 25));
            	    categoryCombo.setPreferredSize(new Dimension(200, 25));
            	    
            	    Font labelFont = new Font("맑은 고딕", Font.PLAIN, 12);
            	    nameLabel.setFont(labelFont);
            	    categoryLabel.setFont(labelFont);
            	    roomNameField.setFont(labelFont);
            	    categoryCombo.setFont(labelFont);

            	    // 패널에 컴포넌트 추가
            	    panel.add(nameLabel);
            	    panel.add(roomNameField);
            	    panel.add(categoryLabel);
            	    panel.add(categoryCombo);

            	    // 다이얼로그 표시
            	    int result = JOptionPane.showConfirmDialog(
            	        null, 
            	        panel, 
            	        "방 만들기",
            	        JOptionPane.OK_CANCEL_OPTION, 
            	        JOptionPane.PLAIN_MESSAGE
            	    );

            	    // OK 버튼 클릭 시 처리
            	    if (result == JOptionPane.OK_OPTION) {
            	        String roomName = roomNameField.getText().trim();
            	        String selectedCategory = (String) categoryCombo.getSelectedItem();
            	        
            	        if (!roomName.isEmpty()) {
            	            try {
            	                System.out.println("방만들기 요청 전송 /createRoom");
            	                output.writeUTF("/createRoom");
            	                output.writeUTF(roomName);
            	                output.writeUTF(selectedCategory);  // 카테고리도 서버로 전송
            	                System.out.println("[MainPanel] 방이름 전송: " + roomName + ", 카테고리: " + selectedCategory);
            	            } catch (IOException ex) {
            	                ex.printStackTrace();
            	            }
            	        }
            	    }
            	});
             add(createRoomBtn, BorderLayout.SOUTH);
			}
    	
    	//updateRoomList함수	
    	private void updateRoomList(String roomList) {
    		   if (roomList.trim().isEmpty()) {
    			   roomListPanel.removeAll();
    		   }
    		   System.out.println("업데이트된 방: " +roomList);//방에 대한 정보 출력
    	       
    	       rooms.clear();
    	       String[] roomsData = roomList.split(";");
    	       
    	       SwingUtilities.invokeLater(() -> {
    	           roomListPanel.removeAll();
    	           
    	           for(String roomData : roomsData) {
    	               if(roomData.isEmpty()) continue;
    	               
    	               String[] parts = roomData.split(",");
    	               if(parts.length < 5) continue; // 데이터 유효성 체크 추가
    	               
    	               RoomInfo room = new RoomInfo(
    	                   parts[0], 
    	                   Integer.parseInt(parts[1]),
    	                   Integer.parseInt(parts[3]),
    	                   Integer.parseInt(parts[2]),
    	                   parts[4]
    	               );
    	               rooms.add(room);
    	               
    	            
    	           
    	               // roomPanel 크기 설정
    	               JPanel roomPanel = new JPanel(new BorderLayout());
    	               roomPanel.setBackground(Color.WHITE);
    	               roomPanel.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), 80));
    	               roomPanel.setMinimumSize(new Dimension(scrollPane.getViewport().getWidth(), 80));
    	               roomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

    	               // 방 정보 레이아웃
    	               JPanel infoPanel = new JPanel(new BorderLayout(5, 2));
    	               infoPanel.setBackground(Color.WHITE);

    	               JLabel nameLabel = new JLabel(room.roomName+" ".repeat(10)+"카테고리 : "+room.category);
    	               nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));

    	               JLabel countLabel = new JLabel(room.currentUsers + "/" + room.maxUsers + " 명");
    	               countLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
    	               countLabel.setForeground(new Color(95, 99, 104));

    	               infoPanel.add(nameLabel, BorderLayout.NORTH);
    	               infoPanel.add(countLabel, BorderLayout.CENTER);
    	               roomPanel.add(infoPanel, BorderLayout.CENTER);
    	               
    	               // 입장 버튼 스타일링
    	               JButton enterBtn = new JButton("입장");
    	               enterBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
    	               enterBtn.setPreferredSize(new Dimension(70, 30));
    	               enterBtn.setBackground(new Color(66, 133, 244));
    	               enterBtn.setForeground(Color.WHITE);
    	               enterBtn.setBorder(BorderFactory.createEmptyBorder());
    	               enterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    	               enterBtn.setFocusPainted(false);
    	               
    	               // 마우스 오버 효과
    	               enterBtn.addMouseListener(new MouseAdapter() {
    	                   public void mouseEntered(MouseEvent e) {
    	                	   if(room.currentUsers!=room.maxUsers)
    	                	   {
    	                       enterBtn.setBackground(new Color(57, 118, 220));
    	                	   }
    	                   }
    	                   public void mouseExited(MouseEvent e) {
    	                	   if(room.currentUsers!=room.maxUsers) {
    	                       enterBtn.setBackground(new Color(79, 143, 247));
    	                   }}
    	               });
    	               
    	               //가득 찼을때 버튼 기능 x
    	               if(room.currentUsers==room.maxUsers) {
    	            	   enterBtn.setEnabled(false);
    	            	   enterBtn.setBackground(new Color(200, 200, 200));
    	               }
    	               
    	               roomPanel.add(enterBtn, BorderLayout.EAST);
    	               
    	            
    	               JPanel wrapper = new JPanel(new BorderLayout());
    	               wrapper.setBackground(new Color(245, 247, 250));
    	               wrapper.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5)); // 상하 여백 10픽셀
    	               wrapper.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), 100));
    	               wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
    	               
    	               wrapper.add(roomPanel);
    	               
    	               roomListPanel.add(wrapper);
    	               
    	               
    	               enterBtn.addActionListener(e -> {
    	            	    try {
    	            	    	
    	            	    	
    	            	        
    	            	        Container c = mainFrame.getContentPane();
    	            	        c.remove(this);
    	            	        //방을이동한다.
    	            	        MainPanelWrapper.this.gamePanel = new GamePanel(room.roomKey);//이때 새로운!! GamePanel를 만드는 것이다.
    	            	        c.add(gamePanel);
    	            	        c.revalidate();
    	            	        c.repaint();
    	            	        
    	            	      //어떤 방에 입장할지 보내주기.
    	            	        output.writeUTF("/enterRoom");
    	            	        output.writeInt(room.roomKey);
    	            	        System.out.println("/enterRoom 입장");
    	            	        
    	            	    } catch(IOException ex) {
    	            	        ex.printStackTrace();
    	            	    }
    	            	});       
    	           }
    	           // 방 목록이 많을 경우 스크롤이 작동하도록 roomListPanel 크기 재설정
    	           int panelHeight = rooms.size() * 100; // 각 방의 높이(100) * 방 개수
    	           roomListPanel.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), panelHeight));
    	           
    	           roomListPanel.revalidate();
    	           roomListPanel.repaint();
    	       });
    	   }
    }
    
    public class GamePanel extends JPanel{
    	private int roomKey;
    	private Webcam webcam;
    	
    	private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    	private final BlockingQueue<byte[]> imageQueue = new LinkedBlockingQueue<>(5); // 최대 5개 이미지 버퍼
    	private volatile boolean isProcessingImage = false;
    	
    	
    	private JTextArea textArea; 
    	private JPanel submitPanel;
    	
 	   	  
 	    private JButton backButton;
 	    private JButton startButton;
 	    
 	    private JLabel categoryLabel;//카테고리 변경
 	    private JLabel descriptionLabel;//질문자라면 제시어 아니면 맞춰보세요 
 	    private List<JLabel> scoreLabels;
 	    private JLabel timerLabel;
 	    private JTextField submitLabel;
 	    
 	    //메인 
 	    private JLabel mainVideoLabel;  // 메인 비디오 레이블
 	    private JLabel mainNickName;//메인 닉네임 
 	    
 	    //나머지
 	    private List<JLabel> smallVideoLabels;  // 작은 비디오 레이블들 몸으로말해요 맞추는사람들
	    private List<JLabel>nickNameLabels;
	    
	    
	    //이제 게임페널에서 주기적으로 사진을 보내는!!스레드를 만들것이다.
	    private Thread imageSendThread;
	    private Boolean imageSendThreadFlag=true;
	  
	    
	    private volatile boolean webcamFlag = true;  // 웹캠 상태 플래그
	    private BufferedImage cameraOffImage; // cameraoff.jpg 이미지 저장용
	    
	    private JLabel itemLabel; //아이템 아이콘
	    private String currentItem = "아이템"; // 현재 아이템 저장
	    
	    private int originalX;
	    private int originalY;
	    
	    
	    private Timer currentEffectTimer = null;
	    private Timer rotationTimer;
	    private Timer fadeTimer;
	    private Timer mosaicTimer;
	    private Timer mirrorTimer;
	    private Timer shakeTimer;

	    

	    
    	public GamePanel(int roomKey) {
    		
    		
    			this.roomKey=roomKey;
    		 	setLayout(new BorderLayout(10, 10));
	            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	            setBackground(new Color(249, 250, 251));
	            
	            try {
	                cameraOffImage = ImageIO.read(new File("src/cameraoff.jpg"));
	                System.out.println("카메라오프 이미지 학보");
	            } catch (IOException e) {
	                e.printStackTrace();
	            }


	            // 메인 컨텐츠 패널
	            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
	            contentPanel.setBackground(new Color(249, 250, 251));

	            // 상단 정보 패널
	            JPanel infoPanel = new JPanel(new BorderLayout());
	            infoPanel.setBackground(Color.WHITE);
	            infoPanel.setBorder(BorderFactory.createCompoundBorder(
	                BorderFactory.createLineBorder(new Color(233, 236, 239)),
	                BorderFactory.createEmptyBorder(15, 20, 15, 20)
	            ));

	            categoryLabel = new JLabel("카테고리 : 영화");
	            categoryLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
	            descriptionLabel = new JLabel("제시어 : 국민참여 or 정답을 맞춰보세요!");
	            descriptionLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
	            
	            JPanel labelPanel = new JPanel(new GridLayout(2, 1, 0, 5));
	            labelPanel.setBackground(Color.WHITE);
	            labelPanel.add(categoryLabel);
	            labelPanel.add(descriptionLabel);
	            
	            timerLabel = new JLabel("타이머");
	            timerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
	            
	            infoPanel.add(labelPanel, BorderLayout.WEST);
	            infoPanel.add(timerLabel, BorderLayout.EAST);

	            // 메인 컨텐츠 wrapper (메인 비디오와 텍스트 영역)
	            JPanel mainContentWrapper = new JPanel(new BorderLayout(10, 0));

	            // 메인 비디오 패널
	            JPanel mainVideoPanel = new JPanel(new BorderLayout());
	            mainVideoPanel.setBackground(Color.WHITE);
	            mainVideoPanel.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));
	            mainVideoPanel.setPreferredSize(new Dimension(400, 360));
	            
	            mainNickName=new JLabel("닉네임");
	            JPanel mainNickNameWrapper=new JPanel(new BorderLayout());
	            mainNickName.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
	          
	            
	            mainNickNameWrapper.setBackground(new Color(249, 250, 251));
	            mainNickNameWrapper.add(mainNickName);
	            
	            mainVideoLabel = new JLabel();
	            mainVideoLabel.setBackground(new Color(245, 247, 250));
	            mainVideoLabel.setOpaque(true);
	            mainVideoLabel.setHorizontalAlignment(JLabel.CENTER);
	            mainVideoLabel.setPreferredSize(new Dimension(550, 340));
	            mainVideoLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                    	System.out.println("마우스 클릭");
                        // 클릭된 비디오가 현재 사용자의 것인지 확인
                        if (mainNickName.getText().equals(MainPanelWrapper.this.userName)) {
                            webcamFlag = !webcamFlag;  // 플래그 토글
                        }
                    }
                });
	            
	            mainVideoLabel.addComponentListener(new java.awt.event.ComponentAdapter() {
	                @Override
	                public void componentMoved(java.awt.event.ComponentEvent evt) {
	                    originalX = mainVideoLabel.getX();
	                    originalY = mainVideoLabel.getY();
	                }
	            });
	            mainVideoPanel.add(mainVideoLabel, BorderLayout.CENTER);
	            mainVideoPanel.add(mainNickNameWrapper, BorderLayout.NORTH);
	            

	            // 텍스트 영역 추가 부분을 다음과 같이 수정
	            JPanel textAreaPanel = new JPanel(new BorderLayout(0, 10));
	            textAreaPanel.setPreferredSize(new Dimension(280, 360));
	            textAreaPanel.setBackground(new Color(249, 250, 251));
	            textAreaPanel.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));
	            
	            // 점수 표시 패널
	            JPanel scorePanel = new JPanel();
	            scorePanel.setBackground(Color.LIGHT_GRAY);
	            scorePanel.setPreferredSize(new Dimension(280, 120));
	            scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));

	            scoreLabels= new ArrayList<>();
	            
	            
	            // 점수 항목 추가
	            for (int i = 0; i < 4; i++) {
	                JLabel label = new JLabel("닉네임" + i + " : 점수");
	                label.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
	                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
	                scorePanel.add(label);
	                scoreLabels.add(label);
	            }
	            // 답안 입력 영역
	            JPanel answerPanel = new JPanel(new BorderLayout(0, 10));
	            answerPanel.setBackground(Color.LIGHT_GRAY);
	            
	            // 텍스트 입력 영역
	            JPanel textPanel = new JPanel(new BorderLayout());
	            textPanel.setBackground(Color.LIGHT_GRAY);
	            
	            
	            textArea = new JTextArea();
	            textArea.setEditable(false);  // 읽기 전용으로 설정
	            textArea.setLineWrap(true);   // 자동 줄바꿈
	            textArea.setWrapStyleWord(true);  // 단어 단위로 줄바꿈
	            textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
	            
	            JScrollPane scrollPane = new JScrollPane(textArea);
	            scrollPane.setPreferredSize(new Dimension(268, 150));
	            
	            textPanel.add(scrollPane, BorderLayout.CENTER);

	            // 답안 작성란
	            submitPanel = new JPanel(new BorderLayout());
	            submitPanel.setBackground(Color.LIGHT_GRAY);
	            submitLabel = new JTextField("");
	            submitLabel.setPreferredSize(new Dimension(280, 30));
	            submitLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
	            submitLabel.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						try {
							String answer=submitLabel.getText();
							output.writeUTF("/answerInput");
							System.out.println("answerInput호출");
							output.writeInt(roomKey);			
							output.writeUTF(answer);
							output.flush();
							submitLabel.setText("");
						} catch (IOException e1) {
							
						}
						
					}
				});
	            
	            submitPanel.add(submitLabel,BorderLayout.CENTER);
	            
	            answerPanel.add(textPanel, BorderLayout.CENTER);
	            answerPanel.add(submitPanel, BorderLayout.SOUTH);
	            
	            textAreaPanel.add(scorePanel, BorderLayout.NORTH);
	            textAreaPanel.add(answerPanel, BorderLayout.CENTER);
	            
	            // wrapper에 추가
	            mainContentWrapper.add(mainVideoPanel, BorderLayout.CENTER);
	            mainContentWrapper.add(textAreaPanel, BorderLayout.EAST);

	         // 작은 비디오들 패널
	            JPanel smallVideosPanel = new JPanel(new GridLayout(1, 3, 5, 5)); // 세로로 3개 배치
	            smallVideosPanel.setBackground(new Color(249, 250, 251));

	            smallVideoLabels = new ArrayList<>();
	            nickNameLabels = new ArrayList<>();
	            
	            for (int i = 0; i < 3; i++) {
	            	final int index = i;
	                // 각 비디오 섹션을 위한 패널
	                JPanel videoSection = new JPanel(new BorderLayout());
	                videoSection.setBackground(new Color(249, 250, 251));
	                
	                // 닉네임 패널
	                JPanel nickNamePanel = new JPanel(new BorderLayout());
	                nickNamePanel.setBackground(new Color(249, 250, 251));
	                nickNamePanel.setPreferredSize(new Dimension(160, 20)); // 높이 20으로 고정
	                
	                JLabel nickName = new JLabel("닉네임" + i);
	                nickName.setHorizontalAlignment(JLabel.LEFT);
	                nickNameLabels.add(nickName);
	                nickNamePanel.add(nickName);
	                
	                // 비디오 레이블
	                JLabel smallVideo = new JLabel();
	                smallVideo.setBackground(new Color(245, 247, 250));
	                smallVideo.setOpaque(true);
	                smallVideo.setPreferredSize(new Dimension(275, 120));
	                smallVideo.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));
	                
	                smallVideo.addMouseListener(new MouseAdapter() {
	                    @Override
	                    public void mouseClicked(MouseEvent e) {
	                    	System.out.println("마우스 클릭");
	                        // 클릭된 비디오가 현재 사용자의 것인지 확인
	                        if (nickNameLabels.get(index).getText().equals(MainPanelWrapper.this.userName)) {
	                            webcamFlag = !webcamFlag;  // 플래그 토글
	                            
	                            // 웹캠을 끄는 경우 cameraoff 이미지 전송
	                            if (!webcamFlag && cameraOffImage != null) {
	                                processAndSendImage(cameraOffImage);
	                            }
	                        }
	                    }
	                });
	                
	                smallVideoLabels.add(smallVideo);
	                
	                // 비디오 섹션에 닉네임과 비디오 추가
	                videoSection.add(nickNamePanel, BorderLayout.NORTH);
	                videoSection.add(smallVideo, BorderLayout.CENTER);
	                
	                smallVideosPanel.add(videoSection);
	            }
	            // 하단 패널 (버튼과 아이템)
	            JPanel bottomPanel = new JPanel(new BorderLayout(20, 0));
	            bottomPanel.setBackground(new Color(249, 250, 251));

	            // 버튼 패널 (왼쪽)
	            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
	            buttonPanel.setBackground(new Color(249, 250, 251));

	            backButton = new JButton("뒤로가기");
	            startButton = new JButton("시작하기");
	            
	         // GamePanel 클래스의 생성자에 startButton 액션 리스너 추가
	            startButton.addActionListener(e -> {
	                try {
	                	System.out.println("/startGame호출");
	                    output.writeUTF("/startGame");
	                    output.writeInt(roomKey);
	                    output.flush();
	                    
	                } catch (IOException ex) {
	                    ex.printStackTrace();
	                }
	            });

	            // 버튼 스타일링
	            for (JButton button : new JButton[]{backButton, startButton}) {
	                button.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
	                button.setPreferredSize(new Dimension(120, 35));
	                button.setBackground(new Color(66, 133, 244));
	                button.setForeground(Color.WHITE);
	                button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
	                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
	                button.setFocusPainted(false);
	            }

	            buttonPanel.add(backButton);
	            buttonPanel.add(startButton);

	            // 아이템 패널 (오른쪽)
	            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	            itemPanel.setBackground(new Color(249, 250, 251));

	            JLabel itemTitleLabel = new JLabel("아이템 : ");
	            itemPanel.add(itemTitleLabel);
	            itemLabel = new JLabel("아이콘");
	            itemLabel.setPreferredSize(new Dimension(50, 35));
                itemLabel.setHorizontalAlignment(JLabel.CENTER);
                itemLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                
                itemLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // 메인이 아니고, 아이템을 사용하지 않은 상태일 때만 클릭 가능
                        if (!mainNickName.getText().equals(MainPanelWrapper.this.userName) 
                            && !currentItem.equals("아이템사용") && !currentItem.equals("아이템")) {
                            try {
                                output.writeUTF("/useItem");
                                output.writeInt(roomKey);
                                output.writeUTF(currentItem);
                                output.flush();
                                
                                
                                
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                
                itemPanel.add(itemLabel);

	            

	            bottomPanel.add(buttonPanel, BorderLayout.WEST);
	            bottomPanel.add(itemPanel, BorderLayout.EAST);

	            backButton.addActionListener(e -> {
	                try {
	                    System.out.println("뒤로가기 버튼 클릭");
	                    cleanup(); 
	                    output.writeUTF("/exitRoom");
	                    output.writeInt(roomKey);
	                    output.flush();
	                    
	                } catch (Exception ex) {
	                    System.out.println("[GamePanel] 에러: " + ex.getMessage());
	                    ex.printStackTrace();
	                }
	            });
	            // 컴포넌트 배치
	            contentPanel.add(infoPanel, BorderLayout.NORTH);
	            contentPanel.add(mainContentWrapper, BorderLayout.CENTER);
	            contentPanel.add(smallVideosPanel, BorderLayout.SOUTH);

	            add(contentPanel, BorderLayout.CENTER);
	            add(bottomPanel, BorderLayout.SOUTH);
	            
	            
	            //하나의 컴퓨터에서 여러개를 테스트할 수 없으니 이름이 홍혜창
	            //이라면 웹캠을띄우고 아니면 사진을 전송한다.
	            
	            if(MainPanelWrapper.this.userName.equals("홍혜창") || MainPanelWrapper.this.userName.equals("손주완")) {
	            	webcam = Webcam.getDefault();
	                webcam.setViewSize(new Dimension(320, 240));
	                webcam.open();
	            }
	            
	            
	            imageSendThread = new Thread(() -> {
	                while (imageSendThreadFlag) {
	                    try {
	                        if (!webcamFlag) {
	                            if (cameraOffImage != null) {
	                                processAndSendImage(cameraOffImage);
	                                Thread.sleep(3000);  // 카메라 오프 상태일 때는 3초마다 한 번씩 전송
	                            } else {
	                                Thread.sleep(100);
	                            }
	                            continue;
	                        }

	                    	
	                    	
	                    	
	                        if (MainPanelWrapper.this.userName.equals("홍혜창") || MainPanelWrapper.this.userName.equals("손주완")) {
	                            BufferedImage image = webcam.getImage();
	                            if (image != null) {
	                                processAndSendImage(image);
	                                Thread.sleep(500);
	                            }
	                        } else {
	                            BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
	                            Graphics2D g = image.createGraphics();
	                            g.setColor(new Color((int)(Math.random() * 255),
	                                    (int)(Math.random() * 255),
	                                    (int)(Math.random() * 255)));
	                            g.fillRect(0, 0, 320, 240);
	                            g.dispose();
	                            processAndSendImage(image);
	                            Thread.sleep(5000);
	                        }
	                        
	                    } catch (InterruptedException e) {
	                        Thread.currentThread().interrupt();
	                        break;
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	            });
	            imageSendThread.start();
	            
	            
    	}
    	
    	private void updateItemImage(String itemName) {
    	    ImageIcon icon = null;
    	    currentItem = itemName;
    	    
    	    System.out.println(MainPanelWrapper.this.userName+"updateItemImage : "+itemName);
    	    if (itemName.equals("아이템사용")) {
    	        itemLabel.setIcon(null);
    	        itemLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    	        return;
    	    }
    	    
    	    switch(itemName) {
    	        case "아이템":
    	            icon = new ImageIcon("src/아이템.jpg");
    	            break;
    	        case "회전":
    	            icon = new ImageIcon("src/회전.jpg");
    	            break;
    	        case "먹물":
    	            icon = new ImageIcon("src/먹물.jpg");
    	            break;
    	        case "모자이크":
    	            icon = new ImageIcon("src/모자이크.jpg");
    	            break;
    	        case "거울":
    	            icon = new ImageIcon("src/거울.jpg");
    	            break;
    	        case "흔들림":
    	            icon = new ImageIcon("src/흔들림.jpg");
    	            break;
    	    }
    	    
    	    
    	    if(icon != null) {
    	        // 원본 이미지 가져오기
    	        Image img = icon.getImage();
    	        
    	        // itemLabel의 크기 가져오기
    	        int labelWidth = itemLabel.getWidth();
    	        int labelHeight = itemLabel.getHeight();
    	        
    	        // 레이블 크기가 0이면 PreferredSize 사용
    	        if (labelWidth == 0) labelWidth = (int)itemLabel.getPreferredSize().getWidth();
    	        if (labelHeight == 0) labelHeight = (int)itemLabel.getPreferredSize().getHeight();
    	        
    	        // 이미지를 레이블 크기에 맞게 조정 (높은 품질의 크기 조정 사용)
    	        Image resizedImg = img.getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);
    	        
    	        // 조정된 이미지로 새 ImageIcon 생성하여 설정
    	        itemLabel.setIcon(new ImageIcon(resizedImg));
    	    }
    	}
    	
    	private void applyItemEffect(String itemType) {
    	    switch(itemType) {
    	        case "회전":
    	            startRotationEffect();
    	            break;
    	        case "먹물":
    	            startInkEffect();
    	            break;
    	        case "모자이크":
    	            startMosaicEffect();
    	            break;
    	        case "거울":
    	            startMirrorEffect();
    	            break;
    	        case "흔들림":
    	            startShakeEffect();
    	            break;
    	    }
    	}
    	

private void startRotationEffect() {
	
    Timer effectTimer = new Timer(5000, e -> {
        resetVideoEffect();
        if (rotationTimer != null) {
            rotationTimer.stop();
            rotationTimer = null;
        }
        ((Timer)e.getSource()).stop();
    });
    effectTimer.setRepeats(false);
    effectTimer.start();

    final double[] angle = {0};
     rotationTimer = new Timer(50, e -> {
        if (mainVideoLabel.getIcon() != null) {
            ImageIcon icon = (ImageIcon) mainVideoLabel.getIcon();
            BufferedImage original = new BufferedImage(
                icon.getIconWidth(), 
                icon.getIconHeight(), 
                BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = original.createGraphics();
            g2d.drawImage(icon.getImage(), 0, 0, null);
            g2d.dispose();

            BufferedImage rotated = new BufferedImage(
                original.getWidth(), 
                original.getHeight(), 
                BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2dRotated = rotated.createGraphics();
            double centerX = original.getWidth() / 2.0;
            double centerY = original.getHeight() / 2.0;
            
            g2dRotated.translate(centerX, centerY);
            g2dRotated.rotate(Math.toRadians(angle[0]));
            g2dRotated.translate(-centerX, -centerY);
            g2dRotated.drawImage(original, 0, 0, null);
            g2dRotated.dispose();

            mainVideoLabel.setIcon(new ImageIcon(rotated));
            angle[0] += 10;
        }
    });
    rotationTimer.start();
}

private void startInkEffect() {
	
    Timer effectTimer = new Timer(5000, e -> {
        resetVideoEffect();
        if (fadeTimer != null) {
            fadeTimer.stop();
            fadeTimer = null;
        }
        ((Timer)e.getSource()).stop();
    });
    effectTimer.setRepeats(false);
    effectTimer.start();

    JPanel overlayPanel = new JPanel() {
        float alpha = 1.0f;
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // 먹물이 퍼지는 효과를 위한 원형 그라데이션
            int numCircles = 5;
            for (int i = 0; i < numCircles; i++) {
                float circleAlpha = alpha * (1.0f - (float)i/numCircles);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, circleAlpha));
                int size = (i + 1) * 50;
                g2d.fillOval(getWidth()/2 - size/2, getHeight()/2 - size/2, size, size);
            }
        }
        
        public void setAlpha(float newAlpha) {
            this.alpha = newAlpha;
            repaint();
        }
    };
    
    overlayPanel.setOpaque(false);
    mainVideoLabel.add(overlayPanel);
    overlayPanel.setBounds(0, 0, mainVideoLabel.getWidth(), mainVideoLabel.getHeight());
    
    fadeTimer = new Timer(50, new ActionListener() {
        float alpha = 1.0f;
        @Override
        public void actionPerformed(ActionEvent e) {
            alpha -= 0.02f;
            if (alpha <= 0) {
                alpha = 0;
                ((Timer)e.getSource()).stop();
                mainVideoLabel.remove(overlayPanel);
                mainVideoLabel.revalidate();
                mainVideoLabel.repaint();
            }
            ((JPanel)overlayPanel).repaint();
        }
    });
    fadeTimer.start();
}

		private void startMosaicEffect() {
			 mosaicTimer = new Timer(100, e -> {
		        if (mainVideoLabel.getIcon() != null) {
		            ImageIcon icon = (ImageIcon) mainVideoLabel.getIcon();
		            BufferedImage original = new BufferedImage(
		                icon.getIconWidth(), 
		                icon.getIconHeight(), 
		                BufferedImage.TYPE_INT_ARGB);
		            
		            Graphics2D g2d = original.createGraphics();
		            g2d.drawImage(icon.getImage(), 0, 0, null);
		            g2d.dispose();
		
		            int pixelSize = 8 + (int)(Math.random() * 8);
		            
		            BufferedImage mosaic = new BufferedImage(
		                original.getWidth()/pixelSize,
		                original.getHeight()/pixelSize,
		                BufferedImage.TYPE_INT_ARGB);
		                
		            Graphics2D g2dMosaic = mosaic.createGraphics();
		            g2dMosaic.drawImage(original, 0, 0, 
		                mosaic.getWidth(), mosaic.getHeight(), null);
		            g2dMosaic.dispose();
		            
		            BufferedImage scaled = new BufferedImage(
		                original.getWidth(),
		                original.getHeight(),
		                BufferedImage.TYPE_INT_ARGB);
		                
		            Graphics2D g2dScaled = scaled.createGraphics();
		            g2dScaled.drawImage(mosaic, 0, 0,
		                scaled.getWidth(), scaled.getHeight(), null);
		            g2dScaled.dispose();
		            
		            mainVideoLabel.setIcon(new ImageIcon(scaled));
		        }
		    });
		    Timer effectTimer = new Timer(5000, e -> {
		        resetVideoEffect();
		        if (mosaicTimer != null) {
		            mosaicTimer.stop();
		            mosaicTimer = null;
		        }
		        ((Timer)e.getSource()).stop();
		    });
		    effectTimer.setRepeats(false);
		    effectTimer.start();
		
		    
		    mosaicTimer.start();
		}
		
		private void startMirrorEffect() {
			
		    Timer effectTimer = new Timer(5000, e -> {
		        resetVideoEffect();
		        if (mirrorTimer != null) {
		            mirrorTimer.stop();
		            mirrorTimer = null;
		        }
		        ((Timer)e.getSource()).stop();
		    });
		    effectTimer.setRepeats(false);
		    effectTimer.start();
		
		    mirrorTimer = new Timer(500, e -> {
		        if (mainVideoLabel.getIcon() != null) {
		            ImageIcon icon = (ImageIcon) mainVideoLabel.getIcon();
		            BufferedImage original = new BufferedImage(
		                icon.getIconWidth(), 
		                icon.getIconHeight(), 
		                BufferedImage.TYPE_INT_ARGB);
		            
		            Graphics2D g2d = original.createGraphics();
		            g2d.drawImage(icon.getImage(), 0, 0, null);
		            g2d.dispose();
		
		            BufferedImage mirrored = new BufferedImage(
		                original.getWidth(),
		                original.getHeight(),
		                BufferedImage.TYPE_INT_ARGB);
		            
		            Graphics2D g2dMirrored = mirrored.createGraphics();
		            
		            if (Math.random() < 0.5) {
		                g2dMirrored.scale(-1, 1);
		                g2dMirrored.translate(-original.getWidth(), 0);
		            } else {
		                g2dMirrored.scale(1, -1);
		                g2dMirrored.translate(0, -original.getHeight());
		            }
		            
		            g2dMirrored.drawImage(original, 0, 0, null);
		            g2dMirrored.dispose();
		            
		            mainVideoLabel.setIcon(new ImageIcon(mirrored));
		        }
		    });
		    mirrorTimer.start();
		}
		
		private void startShakeEffect() {
			
		    Timer effectTimer = new Timer(5000, e -> {
		        resetVideoEffect();
		        if (shakeTimer != null) {
		            shakeTimer.stop();
		            shakeTimer = null;
		        }
		        ((Timer)e.getSource()).stop();
		    });
		    effectTimer.setRepeats(false);
		    effectTimer.start();
		
		    Random random = new Random();
		    shakeTimer = new Timer(50, e -> {
		        if (mainVideoLabel.getIcon() != null) {
		            int intensity = 10;
		            int offsetX = random.nextInt(intensity * 2) - intensity;
		            int offsetY = random.nextInt(intensity * 2) - intensity;
		            
		            mainVideoLabel.setLocation(
		                originalX + offsetX,
		                originalY + offsetY
		            );
		        }
		    });
		    shakeTimer.start();
		}
		
		private void resetVideoEffect() {
		    // 부모 컨테이너 내에서의 상대적 위치로 복원
		    mainVideoLabel.setBounds(originalX, originalY, 
		                           mainVideoLabel.getWidth(), 
		                           mainVideoLabel.getHeight());
		    mainVideoLabel.setOpaque(true);
		    mainVideoLabel.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));
		    mainVideoLabel.revalidate();
		    mainVideoLabel.repaint();
		}	
    	
    	private void processAndSendImage(BufferedImage image) {
    	    if (!isProcessingImage) {
    	        try {
    	            isProcessingImage = true;
    	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	            ImageIO.write(image, "jpg", baos);
    	            byte[] imageData = baos.toByteArray();
    	            
    	            synchronized(output) {
    	                output.writeUTF("/sendWebcam");
    	                output.writeInt(roomKey);
    	                output.writeInt(imageData.length);
    	                output.write(imageData);
    	                output.flush();
    	            }
    	            
    	            baos.close();
    	        } catch (Exception e) {
    	            e.printStackTrace();
    	        } finally {
    	            isProcessingImage = false;
    	        }
    	    }
    	}
    	
      public void cleanup() {
    	  		//이미지 자리에 빈화면으로 
    	  		try {
    	  			output.writeUTF("/exitImage");
    	  			output.writeInt(roomKey);
        	  		output.flush();
    	  		}catch (Exception e) {
				
				}
    	  		
    	        imageSendThreadFlag = false;
    	        if (imageSendThread != null) {
    	            imageSendThread.interrupt();
    	        }
    	        if (webcam != null && webcam.isOpen()) {
    	            webcam.close();
    	        }
    	        imageExecutor.shutdown();
    	        try {
    	            if (!imageExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
    	                imageExecutor.shutdownNow();
    	            }
    	        } catch (InterruptedException e) {
    	            imageExecutor.shutdownNow();
    	        }
    	    }
    	
    	
    	public void updateRoomInfo(String category, String description, String timer, 
                String textAreaContent, List<String> names, 
                List<String> scores, String myUserName,boolean startButton,String winnerName) {
				
    		
    			System.out.println(MainPanelWrapper.this.userName+"updateRoomInfo됌");
    			
    			if(!winnerName.equals("")) {
    				 JOptionPane.showMessageDialog(null, winnerName+"님이 우승하셨습니다!!!");
    					
    			}
    			// 카테고리, 설명, 타이머 업데이트
				categoryLabel.setText(category);
				descriptionLabel.setText(description);
				timerLabel.setText(timer);
				textArea.setText(textAreaContent);
				GamePanel.this.startButton.setEnabled(startButton);
				
		
				
				// 점수판 업데이트
				for(int i=0; i<scoreLabels.size(); i++) {
				if(i < scores.size()) {
				  scoreLabels.get(i).setText(scores.get(i));
				}
				}
				
				// 메인 화면은 본인으로 설정
				int myIndex = names.indexOf(myUserName);//4개중에서 본인이름찾기
				if(myIndex != -1) {
				mainNickName.setText(names.get(myIndex));
				}
				System.out.println(names);
				// 나머지 화면 설정
				int smallLabelIndex = 0;
				for(int i=0; i<names.size(); i++) {
				if(i != myIndex && smallLabelIndex < nickNameLabels.size()) {
				  nickNameLabels.get(smallLabelIndex).setText(names.get(i));
				  smallLabelIndex++;
				}
				}
			// 남은 자리는 빈자리로
			while(smallLabelIndex < nickNameLabels.size()) {
			nickNameLabels.get(smallLabelIndex).setText("빈자리");
			smallLabelIndex++;
			}
			}
		    }
    
    
    
    class MessageThread extends Thread{
    	public void run() {
    		try {
                while(isRunning) {
                    String command = input.readUTF();//command에 따라 다르게 대응
                    if(command.equals("/roomList")) {
                    	processRoomListCommand();
                    }
                    else if(command.equals("/createRoomOK")) {
                    	processCreateRoomResponseCommand();
                    }
                    else if(command.equals("/exitRoomOK")) {
                    	processExitRoomOKCommand();
                    }
                    else if(command.equals("/updateRoomInfo")) {
                        processUpdateRoomInfoCommand();
                    }
                    else if(command.equals("/timerUpdate")) {  // 타이머 업데이트 명령 추가
                        processTimerUpdateCommand();
                    }
                    else if(command.equals("/gameUpdate")) {
                    	System.out.println("/gameUpdate호출");
                        processGameUpdateCommand();
                    }
                    else if(command.equals("/checkAnswer")) {
                        processCheckAnswerCommand();
                    }
                    else if(command.equals("/gamefinish")) {
                    	processGamefinishCommand();
                    }
                    else if(command.equals("/userWebCam")) {
                    	processUserWebCamCommand();
                    }
                    else if(command.equals("/exitImageChange")) {
                    	processExitImageChangeCamCommand();
                    }
                    else if(command.equals("/attack")) {
                    	processAttackCommand();
                    }
                    else if(command.equals("/successItemUse")) {
                    	processSuccessItemUseCommand();
                    }
                    else if(command.equals("/useItemText")) {
                    	processUseItemTextCommand();
                    }
                }
            } catch(Exception e) {
                
            }
    	}
    	

    	private synchronized void processUseItemTextCommand() {
    		try {
    			
    			String textAreaContent =input.readUTF();
    			
    	    	SwingUtilities.invokeLater(() -> {
   			 
   			 gamePanel.textArea.setText(textAreaContent);
   			 gamePanel.textArea.setCaretPosition(gamePanel.textArea.getDocument().getLength());
   			 
    	    	});
    			
    		}catch (Exception e) {
				// TODO: handle exception
			}
    	}
    	
    	
    	private synchronized void processSuccessItemUseCommand() {
    		try {
    			SwingUtilities.invokeLater(() -> {
    				
    				gamePanel.currentItem="아이템사용";
    				gamePanel.itemLabel.setIcon(null);
    				gamePanel.itemLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    				
    			});
    			
    		}catch (Exception e) {
				// TODO: handle exception
			}
    	}
    	
    	
    	private synchronized void processAttackCommand() {
    		try {
    			String itemType = input.readUTF();
    		    gamePanel.applyItemEffect(itemType);
    			
    		}catch (Exception e) {
				// TODO: handle exception
			}
    	}
    	
    	
    	private synchronized void processUserWebCamCommand() {
    	    try {
    	        String userName = input.readUTF();
    	        int length = input.readInt();
    	        byte[] imageData = new byte[length];
    	        input.readFully(imageData);

    	        ByteArrayInputStream bais = null;
    	        try {
    	            bais = new ByteArrayInputStream(imageData);
    	            BufferedImage receivedImage = ImageIO.read(bais);

    	            if (receivedImage != null) {
    	                SwingUtilities.invokeLater(() -> {
    	                    if (gamePanel.mainNickName.getText().equals(userName)) {
    	                        // 메인 비디오 레이블의 크기에 맞춤
    	                        Dimension mainLabelSize = gamePanel.mainVideoLabel.getPreferredSize();
    	                        Image scaledImage = getScaledImage(receivedImage, mainLabelSize);
    	                        ImageIcon icon = new ImageIcon(scaledImage);
    	                        gamePanel.mainVideoLabel.setIcon(icon);
    	                    } else {
    	                        for (int i = 0; i < gamePanel.nickNameLabels.size(); i++) {
    	                            if (gamePanel.nickNameLabels.get(i).getText().equals(userName)) {
    	                                // 작은 비디오 레이블의 크기에 맞춤
    	                                Dimension smallLabelSize = gamePanel.smallVideoLabels.get(i).getPreferredSize();
    	                                Image scaledImage = getScaledImage(receivedImage, smallLabelSize);
    	                                ImageIcon icon = new ImageIcon(scaledImage);
    	                                gamePanel.smallVideoLabels.get(i).setIcon(icon);
    	                                break;
    	                            }
    	                        }
    	                    }
    	                });
    	            }
    	        } finally {
    	            if (bais != null) {
    	                try {
    	                    bais.close();
    	                } catch (IOException e) {
    	                    e.printStackTrace();
    	                }
    	            }
    	        }
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    }
    	}
    	
    	private synchronized Image getScaledImage(BufferedImage srcImg, Dimension labelSize) {
    	    // 레이블 크기에 정확히 맞추기
    	    int targetWidth = (int) labelSize.getWidth();
    	    int targetHeight = (int) labelSize.getHeight();
    	    
    	    // 고품질 이미지 스케일링을 위한 BufferedImage 생성
    	    BufferedImage resizedImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    	    Graphics2D g2 = resizedImg.createGraphics();
    	    
    	    // 렌더링 품질 설정
    	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    	    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	    
    	    // 이미지를 레이블 크기에 맞게 그리기
    	    g2.drawImage(srcImg, 0, 0, targetWidth, targetHeight, null);
    	    g2.dispose();
    	    
    	    return resizedImg;
    	}
    	private synchronized void processExitImageChangeCamCommand() {
    		try {
    			
    			String exitName = input.readUTF();
    	        SwingUtilities.invokeLater(() -> {
    	            if (gamePanel.mainNickName.getText().equals(exitName)) {
    	                // 메인 비디오 초기화
    	                gamePanel.mainVideoLabel.setIcon(null);
    	                gamePanel.mainVideoLabel.setBackground(new Color(245, 247, 250));
    	                gamePanel.mainVideoLabel.setOpaque(true);
    	            } else {
    	                // 작은 비디오 초기화
    	                for (int i = 0; i < gamePanel.nickNameLabels.size(); i++) {
    	                    if (gamePanel.nickNameLabels.get(i).getText().equals(exitName)) {
    	                        JLabel smallVideo = gamePanel.smallVideoLabels.get(i);
    	                        smallVideo.setIcon(null);
    	                        smallVideo.setBackground(new Color(245, 247, 250));
    	                        smallVideo.setOpaque(true);
    	                        smallVideo.setPreferredSize(new Dimension(160, 120));
    	                        smallVideo.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));
    	                        break;
    	                    }
    	                }
    	            }
    	        });
    		}catch (Exception e) {
				// TODO: handle exception
			}
    	}
    	
    	
        
    
    	
    	private synchronized void processGamefinishCommand() {
    		try {
    			Boolean finish=input.readBoolean();
    			gamePanel.submitLabel.setEditable(true);
    			gamePanel.backButton.setEnabled(true);
    		}catch (Exception e) {
				// TODO: handle exception
			}
    	}
    	private synchronized void processCheckAnswerCommand() {
    		try {
    			String textAreaContent = input.readUTF();
    			System.out.println("텍스트에리어:"+textAreaContent);
    			 SwingUtilities.invokeLater(() -> {
    				 
    				 gamePanel.textArea.setText(textAreaContent);
    				 gamePanel.textArea.setCaretPosition(gamePanel.textArea.getDocument().getLength());
    				 
    			 });
    			
    		}catch (Exception e) {
				
			}
    	
    	}
    	//처음시작될 때 + 다음스테이지로 넘어갈때
    	private synchronized void processGameUpdateCommand() {
    	    try {
    	        String mainNickName = input.readUTF();
    	        String description = input.readUTF();
    	        String category= input.readUTF();
    	        Boolean gameRunning=input.readBoolean();
    	        System.out.println("mainNickName"+mainNickName);
    	        System.out.println("description"+description);
    	        System.out.println("category"+category);
    	        System.out.println("gameRunning"+gameRunning);
    	        
    	        String itemName = input.readUTF();
    	        System.out.println(itemName);
    	    
    	       
    	        
    	        if(gameRunning) {
    	        	gamePanel.backButton.setEnabled(false);
    	        	gamePanel.startButton.setEnabled(false);
    	        }
    	       
    	        if(mainNickName.equals(MainPanelWrapper.this.userName)) {
    	        	gamePanel.submitLabel.setEditable(false);
    	        }else {
    	        	gamePanel.submitLabel.setEditable(true);
    	        }
    	        
    	        // 점수 업데이트
    	        int scoreLabelSize = input.readInt();
    	        List<String> scoreLabels = new ArrayList<>();
    	        for(int i=0; i<scoreLabelSize; i++) {
    	            scoreLabels.add(input.readUTF());
    	        }
    	        
    	        boolean mainChanged = input.readBoolean();
    	        
    	        // UI 업데이트를 EDT에서 실행
    	        SwingUtilities.invokeLater(() -> {
    	        	gamePanel.updateItemImage(itemName);
    	            // 점수 업데이트
    	            for(int i=0; i<gamePanel.scoreLabels.size(); i++) {
    	                if(i < scoreLabels.size()) {
    	                    gamePanel.scoreLabels.get(i).setText(scoreLabels.get(i));
    	                }
    	            }
    	            
    	            // 설명 업데이트
    	            gamePanel.descriptionLabel.setText(description);
    	            gamePanel.categoryLabel.setText(category);
    	            
    	            if(mainChanged) {
    	                // mainNickName과 일치하는 smallVideo를 mainVideo와 교체
    	                int mainIndex = -1;
    	                for(int i=0; i<gamePanel.nickNameLabels.size(); i++) {
    	                    if(gamePanel.nickNameLabels.get(i).getText().equals(mainNickName)) {
    	                        mainIndex = i;
    	                        break;
    	                    }
    	                }
    	                
    	                if(mainIndex != -1) {
    	                    // 메인 비디오와 작은 비디오의 위치 교환
    	                    String tempNick = gamePanel.mainNickName.getText();
    	                    gamePanel.mainNickName.setText(mainNickName);
    	                    gamePanel.nickNameLabels.get(mainIndex).setText(tempNick);
    	                }
    	            }
    	        });
    	    } catch(IOException e) {
    	        e.printStackTrace();
    	    }
    	}
    	private synchronized void processTimerUpdateCommand() {
    	    try {
    	        String time = input.readUTF();
    	        SwingUtilities.invokeLater(() -> {
    	            if(gamePanel != null) {
    	                gamePanel.timerLabel.setText(time);
    	            }
    	        });
    	    } catch(IOException e) {
    	        e.printStackTrace();
    	    }
    	}    	
    	
    	private synchronized void processUpdateRoomInfoCommand() {
		    try {
		    	String category = input.readUTF();
		        String description = input.readUTF();
		        String timer = input.readUTF();
		        String textAreaContent = input.readUTF();
		        boolean startButton=input.readBoolean();
		        String winnerName=input.readUTF();
		        
		        String itemName = input.readUTF();
		        System.out.println("받은 아이템 : "+itemName);
		      
		       
		        // nameLabels 수신
		        int nameLabelSize = input.readInt();
		        List<String> nameLabels = new ArrayList<>();
		        for(int i=0; i<nameLabelSize; i++) {
		            nameLabels.add(input.readUTF());
		        }
		        
		        // scoreLabels 수신
		        int scoreLabelSize = input.readInt();
		        List<String> scoreLabels = new ArrayList<>();
		        for(int i=0; i<scoreLabelSize; i++) {
		            scoreLabels.add(input.readUTF());
		        }
		        
		        // UI 업데이트를 EDT에서 실행
		        SwingUtilities.invokeLater(() -> {
		        	gamePanel.updateItemImage(itemName);	
		            gamePanel.updateRoomInfo(category, description, timer, textAreaContent, 
		                                   nameLabels, scoreLabels, userName,startButton,winnerName);
		        });
		    } catch(IOException e) {
		        e.printStackTrace();
		    }
		}
    	 private synchronized void processRoomListCommand() {
    		 try {
    			 String roomList = input.readUTF();
    			 
                 mainPanel.updateRoomList(roomList); 
    		 }catch (Exception e) {
				
			}
    		 
    	 }
    	 private synchronized void processCreateRoomResponseCommand() {
    		 try {
    		        int roomKey = input.readInt();
    		        
    		        SwingUtilities.invokeLater(() -> {
    		            try {
    		                Container c = mainFrame.getContentPane();
    		                c.remove(mainPanel);
    		                MainPanelWrapper.this.gamePanel = new GamePanel(roomKey);
    		                c.add(gamePanel);
    		                c.revalidate();
    		                c.repaint();
    		            } catch (Exception e) {
    		                e.printStackTrace();
    		            }
    		        });
    		    } catch (Exception e) {
    		        
    		    } 
    		}
    	 private synchronized void processExitRoomOKCommand() {
    		 try {
    			 SwingUtilities.invokeLater(() -> {
                     try {
                        //방을 만들어졌으면, 그 방으로 바로 이동하게 만든다.
                         Container c = mainFrame.getContentPane();
                         c.remove(gamePanel);
                         c.add(mainPanel);
                         c.revalidate();
                         c.repaint();
                         gamePanel=null;
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 });
    		 }catch (Exception e) {
				
			} 
    	 }
    }
}

// Room 정보 저장 클래스
class RoomInfo {
    String roomName;
    int currentUsers;
    int maxUsers;
    int roomKey;
    String category;
    
    public RoomInfo(String roomName, int currentUsers, int maxUsers, int roomKey,String category) {
        this.roomName = roomName;
        this.currentUsers = currentUsers;
        this.maxUsers = maxUsers;
        this.roomKey = roomKey;
        this.category=category;
    }
}
