
//Server.java
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
 private List<ClientHandler> clients = new ArrayList<>();//현재 서버에 연결된 사용자들
 private List<Room> rooms = new ArrayList<>(); //여러 방에 대한 정보들
 private int roomKey=0;
 private ServerSocket serverSocket;
 
 // 카테고리별 단어 데이터 - Server 클래스의 맨 위에 선언
 private static final Map<String, List<String>> CATEGORY_WORDS = new HashMap<>();
 
 static {
	// 영화 카테고리 (유명 영화)
	CATEGORY_WORDS.put("영화", Arrays.asList(
	   "겨울왕국", "아이언맨", "기생충", "스파이더맨",
	   "범죄도시", "극한직업", "괴물", "아바타", "알라딘",
	   "토이스토리", "타이타닉", "해리포터", "쏘우", "신과함께",
	   "택시운전사", "부산행", "마녀", "도둑들","명량","해운대","인터스텔라",
	   "추격자", "수상한그녀", "변호인", "베테랑", "늑대소년",
	   "친구", "말죽거리잔혹사", "올드보이", "건축학개론", "엽기적인그녀",
	   "내부자들", "관상", "써니", "화려한휴가", "과속스캔들",
	   "웰컴투동막골", "미녀는괴로워", "좋은놈나쁜놈이상한놈", "완벽한타인", "수리남"
	));

	// 드라마 카테고리 (유명 한국드라마)
	CATEGORY_WORDS.put("드라마", Arrays.asList(
			   "오징어게임", "도깨비", "펜트하우스", "더글로리", "빈센조",
			   "대장금", "슬기로운의사생활", "이상한변호사우영우", "사랑의불시착", "부부의세계",
			   "스카이캐슬", "별에서온그대", "응답하라1988", "태양의후예", "이태원클래스",
			   "지금우리학교는", "멜로가체질", 
			   "추노", "미스터션샤인", "내아이디는강남미인",
			   "시그널", "미생", "유미의세포들", "스위트홈",
			    "재벌집막내아들", "모범택시", "구르미그린달빛",
			    "킹더랜드",  "무빙", "경이로운소문",
			    "술꾼도시여자들","공부의신"
			));
	// 사물 카테고리 (표현하기 재미있는 사물)
	CATEGORY_WORDS.put("사물", Arrays.asList(
	   "청소기", "드라이기", "세탁기", "전자레인지", "선풍기",
	   "밥솥", "믹서기", "건조기", "냉장고", "온풍기",
	   "칫솔", "화장실", "변기", "거울", "샤워기",
	   "물걸레", "빗자루", "걸레", "망치", "톱",
	   "돋보기", "못", "가위", "붕대", "목발",
	   "면도기", "안경", "마스크", "모자", "아령",
	   "줄넘기", "낚싯대", "스케이트", "요요", "훌라후프",
	   "팽이", "연", "비누", "젓가락", "숟가락"
	));

	// 스포츠 카테고리 (유명 선수)
	CATEGORY_WORDS.put("스포츠", Arrays.asList(
	   "손흥민", "메시", "호날두", "조던", "김연아",
	   "박지성", "차범근", "박찬호", "류현진", 
	   "김연경", "박태환", "허재", "서장훈", "우상혁",
	   "타이거우즈", "우사인볼트",  "네이마르", "베컴",
	   "마이클펠프스", "헐크", "음바페", "반다이크", "반니",
	   "레반도프스키", "즐라탄", "포그바", "살라", "홀란드",
	   "클롭", "캉테", "아자르", "브루노", "케인",
	   "피케", "펠레", "마라도나", "지단", "피구","안정환"
	));

	// 음식 카테고리 (표현하기 재미있는 음식)
	CATEGORY_WORDS.put("음식", Arrays.asList(
	   "떡볶이", "라면", "햄버거", "핫도그", "치킨",
	   "피자", "짜장면", "짬뽕", "탕수육", "김밥",
	   "아이스크림", "와플", "케이크", "팝콘", "솜사탕",
	   "순대", "어묵", "닭꼬치", "붕어빵", "호떡",
	   "츄러스", "샌드위치", "도넛", "빙수", 
	    "감자튀김", "군고구마", "군밤",
	   "돈까스", "냉면", "우동",  "만두",
	   "치즈볼", "타코야키", "닭발", "곱창", "순대"
	));
	}

 
 
 public Server(int port) throws IOException {
     serverSocket = new ServerSocket(port);
     System.out.println("서버 시작됨: " + port);
     
     while (true) {
         Socket clientSocket = serverSocket.accept();
         DataInputStream input = new DataInputStream(clientSocket.getInputStream());
         String nickName=input.readUTF();
         ClientHandler client = new ClientHandler(clientSocket,nickName);
         clients.add(client);
         sendRoomListToAll();//들어오면, 방에대한 정보를 보내준다.
         new Thread(client).start();
     }
 }
 
 //방 클래스
 //방 이름과, 현재 유저만 보여준다. maxUsers는 쉽게 4로 통일한다.
 public class Room {
	     String roomName;
	     int currentUsers;
	     int maxUsers;
	     int roomKey;
	     
	     List<String> gameWords=new ArrayList<String>();
	     
	     List<ClientHandler> roomClients;//ClientHandler.nickName으로 접근가능하다.
 		private String textArea=new String(); 		    
	    private String categoryLabel=new String();//카테고리 변경
	    private String descriptionLabelnew =new String();//질문자라면 제시어 아니면 맞춰보세요 
	    private List<String> scoreLabels=new ArrayList<String>();;
	    private String timerLabel=new String();;   
	    //메인 
	    private String mainNickName=new String("");//메인 닉네임
	    //나머지
	    private List<String>nameLabels; //메인닉네임 + 나머지애들이다!!
	    
	    private Map<String, String> itemMap = new HashMap<>(); // 닉네임:아이템이름 매핑
	    
	    
	    //시작하기 버튼
	    private boolean startButton;
	    private String category;
	    private String word=" ";//제시어
	    
	    private Thread timerThraed;
	    private Boolean timerFlag=true;
	    private String winnerName="";
	    private boolean gameRunning=false;
    
     public Room(String roomName,int roomKey,String category) {
    	 this.roomName = roomName;
         this.roomKey = roomKey;
         this.currentUsers = 0;
         this.maxUsers = 4;
         this.roomClients = new ArrayList<>();
         this.nameLabels = new ArrayList<>();
         startButton=false;
         this.category=category;
         itemMap = new HashMap<>();
         
         
         for(int i=0; i<maxUsers; i++) {
             nameLabels.add("빈자리");
         }
         
         gameReset();
     }
     private void sendGameUpdate(Boolean mainChanged) {
    	 System.out.println("sendGameUpdate호출");
    	    for(ClientHandler client : roomClients) {
    	        try {
    	            DataOutputStream out = client.getOutput();
    	            out.writeUTF("/gameUpdate");
    	            out.writeUTF(mainNickName);
    	            out.writeUTF(client.nickName.equals(mainNickName) ? 
    	                "제시어: " + word : "정답을 맞춰보세요!");
    	            out.writeUTF(categoryLabel);
    	            out.writeBoolean(gameRunning);
    	            out.writeUTF(itemMap.get(client.nickName));     
    	            // 점수 정보 전송
    	            out.writeInt(scoreLabels.size());
    	            for(String score : scoreLabels) {
    	                out.writeUTF(score);
    	            }
    	            
    	            // 메인 플레이어 변경 여부
    	            out.writeBoolean(mainChanged);
    	            
    	            out.flush();
    	        } catch (IOException e) {
    	            e.printStackTrace();
    	        }
    	    }
    	}

     public String getRandomWord(String category) {
         List<String> words = CATEGORY_WORDS.get(category);
         if (words != null) {
             Random random = new Random();
             String newWord=words.get(random.nextInt(words.size()));
             while(gameWords.contains(newWord)) {
            	 newWord=words.get(random.nextInt(words.size()));
             }
             
             return newWord;
         }
         return null;
     }
     
     
     public void handleItemUse(ClientHandler sender, String itemType) {
    	    
    	 try {
    		 itemMap.put(sender.nickName, "아이템사용");
    		sender.output.writeUTF("/successItemUse");
    		sender.output.flush();
     	    
     	    // 메인과 사용자를 제외한 클라이언트들에게 효과 전달
     	    for (ClientHandler client : roomClients) {
     	        if (!client.nickName.equals(mainNickName) && !client.nickName.equals(sender.nickName)) {
     	            try {
     	                client.output.writeUTF("/attack");
     	                client.output.writeUTF(itemType);
     	                client.output.flush();
     	            } catch (IOException e) {
     	                e.printStackTrace();
     	            }
     	        }
     	    }
    	 }catch (Exception e) {
			// TODO: handle exception
		}
    	 	// 아이템 사용 처리
    	    
    	}
     
     
     
     public void gameReset() {
    	
    	 mainNickName="";
    	 textArea="";
    	 categoryLabel=roomName+"		[카테고리 : "+category+"]";
    	 descriptionLabelnew="5점을 얻는 사람이 게임 승리! ※4명이 되면 게임 시작이 가능합니다!!";
    	 //mainNickName은 잘 기억하자
    	 //처음에 게임시작할때는 각자 본인 얼굴이 띄워져야하니까 본인이 메인이 되면된다. 즉, 따로 설정할필요없다.
    	 scoreLabels.clear();
    	 gameWords.clear();
    	 
         // 현재 참가자들의 점수 초기화
         for(int i=0; i<maxUsers; i++) {
             if(!nameLabels.get(i).equals("빈자리")) {
                 scoreLabels.add(nameLabels.get(i) + " : 0");
             } else {
                 scoreLabels.add("빈자리 : 0");
             }
         }
    	 timerLabel="타이머";	 
     }
     
     public void answerCheck(String answer,String nickName) {
    	 
    	 textArea+=nickName+" : "+answer+"\n";
    	 if(answer.equals(word)) {
    		 System.out.println(nickName+"님이 정답을 맞춤");
    		 textArea+=nickName+"님이 정답을 맞췄습니다.\n";
    		 nextStage(true,nickName);
    		 timerLabel="30";
    	}
    	 
    	 for(ClientHandler client : roomClients) {
 	        try {
 	            DataOutputStream out = client.getOutput();
 	            out.writeUTF("/checkAnswer");
 	            out.writeUTF(textArea);
 	            out.flush();
 	        } catch (IOException e) {
 	            e.printStackTrace();
 	        }
 	    }
    	 
     }
     
     //1초마다 데이터를 전송해주기.
     public void gameStart() {
    	 System.out.println("gameStart호출");
    	 String[] items = {"거울", "먹물", "흔들림", "모자이크","회전"};
    	 categoryLabel="카테고리 : "+category;
    	 System.out.println("categoryLabel: " + categoryLabel);
    	 word=getRandomWord(category);
    	 gameWords.add(word);//게임 단어 추가
    	 descriptionLabelnew="제시어: "+word+"/정답을 맞춰보세요!";
    	 timerLabel="30";
    	 Random random = new Random();
    	 mainNickName=nameLabels.get(random.nextInt(4));//랜덤으로 뽑기
    	 gameRunning=true;
    	 
    	 for (String nickname : itemMap.keySet()) {
    	        itemMap.put(nickname, items[random.nextInt(items.length)]);
    	    }
    	 
    	 System.out.println("현재 방 상태:");
    	 
    	 System.out.println("word: " + word);
    	 System.out.println("mainNickName: " + mainNickName);
    	 System.out.println("gameRunning: " + gameRunning);
    	 
    	 
    	 sendGameUpdate(true);
    	 

    	 timerThraed = new Thread(new Runnable() {
    	        @Override
    	        public void run() {
    	            while(timerFlag) {
    	                try {
    	                    Thread.sleep(1000);
    	                    int time = Integer.parseInt(timerLabel);
    	                    time--;
    	                    
    	                    if(time <= 0) {
    	                        nextStage(false, null);
    	                        time = 30;  
    	                    }
    	                    
    	                    timerLabel = String.valueOf(time);
    	                    timerUpdate();
    	                } catch (InterruptedException e) {
    	                    break;
    	                }
    	            }
    	        }
    	    });
    	timerThraed.start();
     }
     //정답을 맞춰서 다음 스테이지로 넘어가는 경우 guess를 true
     //시간초과로 넘어가는 경우 guess가 false
    public void nextStage(Boolean guess, String guessNickName) {
    int score = -1;
    String oldMain = mainNickName;
    
    if(guess) {
        for(int i=0; i<scoreLabels.size(); i++) {
            String str = scoreLabels.get(i);
            if(str.contains(guessNickName)) {
                String numberOnly = str.replaceAll("[^0-9]", "");
                score = Integer.parseInt(numberOnly);
                score++;
                scoreLabels.set(i, guessNickName + " : " + score);
                break;
            }
        }
        mainNickName = guessNickName;
    } else {
        Random random = new Random();
        do {
            mainNickName = nameLabels.get(random.nextInt(4));
        } while(mainNickName.equals(oldMain) || mainNickName.equals("빈자리"));
    }
    
    word = getRandomWord(category);
    gameWords.add(word);
    descriptionLabelnew = "제시어: " + word + "/정답을 맞춰보세요!";
    
    if(score >= 5) {
        timerFlag = false;
        winnerName = guessNickName;
        gameReset();
        for (String nickname : itemMap.keySet()) {
            itemMap.put(nickname, "아이템"); // 기본 아이템으로 리셋
        }
        for(ClientHandler client : roomClients) {
            try {
                client.sendRoomInfoToClient(this, client);
                client.getOutput().writeUTF("/gamefinish");
                gameRunning=false;
                client.getOutput().writeBoolean(gameRunning);
                
                client.getOutput().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        winnerName = "";
        
        
        
    } else {
        sendGameUpdate(!oldMain.equals(mainNickName));
    }
}
     public synchronized void timerUpdate() {
    	    // 모든 클라이언트에게 동시에 업데이트하기 전에 timerLabel 복사
    	    String currentTime = timerLabel;
    	    for(ClientHandler client : roomClients) {
    	        try {
    	            client.output.writeUTF("/timerUpdate");
    	            client.output.writeUTF(currentTime); // 복사본 사용
    	            client.output.flush(); // flush 추가
    	        } catch (IOException e) {
    	            System.out.println("Timer update failed for client: " + client.nickName);
    	        }
    	    }
    	}
     
     public synchronized  boolean addClient(ClientHandler client) {
    	 System.out.println(client.nickName+" addClient추가");
    	 if (currentUsers < maxUsers) {
             roomClients.add(client);
             // 빈자리를 찾아서 거기에 새로운 클라이언트 닉네임 추가
             for(int i=0; i<maxUsers; i++) {
                 if(nameLabels.get(i).equals("빈자리")) {
                     nameLabels.set(i, client.nickName);
                     break;
                 }
             }
             for(int i=0; i<maxUsers; i++) {
                 if(!nameLabels.get(i).equals("빈자리")) {
                     scoreLabels.set(i,nameLabels.get(i) + " : 0");
                 } else {
                     scoreLabels.set(i,"빈자리 : 0");
                 }
             }
             System.out.println(scoreLabels);
             currentUsers++;
             
             if(currentUsers==maxUsers) {
            	 startButton=true;
             }
             System.out.println("현재 "+roomKey+"방 현재 인원" + currentUsers);
             itemMap.put(client.nickName, "아이템"); // 기본 아이템으로 초기화
             System.out.println(client.nickName+itemMap.get(client.nickName));
             return true;
         }
         return false;
     }

     public synchronized void removeClient(ClientHandler client) {
   
    	 int index = -1;
    	 itemMap.remove(client.nickName);
         // 나가는 클라이언트의 nameLabels 인덱스 찾기
         for(int i=0; i<maxUsers; i++) {
             if(nameLabels.get(i).equals(client.nickName)) {
                 index = i;
                 break;
             }
         }
         
         if(index != -1) {
             nameLabels.set(index, "빈자리"); // 해당 위치를 빈자리로 변경
         }
         for(int i=0; i<maxUsers; i++) {
             if(!nameLabels.get(i).equals("빈자리")) {
                 scoreLabels.set(i,nameLabels.get(i) + " : 0");
             } else {
                 scoreLabels.set(i,"빈자리 : 0");
             }
         }
         System.out.println(scoreLabels);
         roomClients.remove(client);
         currentUsers--;
         System.out.println("현재 "+roomKey+"방 현재 인원"+currentUsers);
         
         if(currentUsers == 0) {
             rooms.remove(this);
             System.out.println("방 제거됨: " + roomKey);
         }
         
         if(currentUsers!=maxUsers) {
        	 startButton=false;
         }
         
    	}
 }
 
 
 
 
 
 class ClientHandler implements Runnable {
     private Socket socket;
     private DataInputStream input;
     private DataOutputStream output;
     private String nickName;
     
    
     
     public ClientHandler(Socket socket,String nickName) throws IOException {
         this.socket = socket;
         this.input = new DataInputStream(socket.getInputStream());
         this.output = new DataOutputStream(socket.getOutputStream());
         this.nickName=nickName;
        
     }
     
     public void run() {
         try {
             while (true) {
            	 String command = input.readUTF(); //이게 어떤 요청인지 구분해준다.
            	 if (command.startsWith("/createRoom")) { // /createRoom으로 시작하면,
            		 processCreateRoomCommand(nickName);
                 }
            	 else if (command.startsWith("/enterRoom")) { //입장하면 룸에 해당하는 클라이언트를 enter
            		 processEnterRoomCommand(nickName);
                 }
            	// Server.java의 ClientHandler 클래스
            	 else if (command.startsWith("/exitRoom")) {
            		 processExitRoomCommand();
            	}
            	 else if (command.startsWith("/startGame")) {
            		 	System.out.println("서버에서 startGame받음");
            		    processStartGameCommand();
            	}
            	 else if (command.startsWith("/answerInput")) {
            		 processAnswerInputCommand();
            	 }
            	 else if (command.startsWith("/sendWebcam")) {
            		 processSendWebcamCommand();
            	 }
            	 else if (command.startsWith("/exitImage")) {
            		 processExitImageCommand();
            	 }
            	 else if (command.startsWith("/useItem")) {
            		 processUseItemCommand();
            	 }
            	 
             }
         } catch (IOException e) {
        	  // 클라이언트 연결 종료 처리
        	    clients.remove(this);
        	    for (Room room : rooms) {
        	        room.removeClient(this);}
         }
     }
     
     private synchronized void processUseItemCommand() {
  	    try {
  	       int roomKey = input.readInt();
  	       String itemType = input.readUTF();
  	       for (Room room : rooms) {
  	           if (room.roomKey == roomKey) {
  	               room.handleItemUse(this, itemType);
  	               
  	               room.textArea+=nickName+"님이 "+itemType+" 아이템을 사용하였습니다.\n";
  	               for(ClientHandler client : room.roomClients) {
	                  
	                        client.output.writeUTF("/useItemText");
	                        client.output.writeUTF(room.textArea);
	                        client.output.flush();
  	               }
  	             break;
  	           }
  	           
  	       	}
  	       
  	}catch (Exception e) {
	        e.printStackTrace();
  		}
     }
   
     
     
     private synchronized void processExitImageCommand() {
 	    try {
 	        int roomKey = input.readInt();
 	     
 	        for (Room room : rooms) {
 	            if (room.roomKey == roomKey) {
 	                for(ClientHandler client : room.roomClients) {
 	                    synchronized(client.output) {
 	                        client.output.writeUTF("/exitImageChange");
 	                        client.output.writeUTF(nickName);
 	                        client.output.flush();
 	                    }
 	                }
 	                break;
 	            }
 	        }
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
  
     
  
     private synchronized void processSendWebcamCommand() {
    	    try {
    	        int roomKey = input.readInt();
    	        int dataLength = input.readInt();
    	        byte[] imageData = new byte[dataLength];
    	        input.readFully(imageData);
    	        
    	        for (Room room : rooms) {
    	            if (room.roomKey == roomKey) {
    	                for(ClientHandler client : room.roomClients) {
    	                    synchronized(client.output) {
    	                        client.output.writeUTF("/userWebCam");
    	                        client.output.writeUTF(nickName);
    	                        client.output.writeInt(dataLength);
    	                        client.output.write(imageData);
    	                        client.output.flush();
    	                    }
    	                }
    	                break;
    	            }
    	        }
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    }
    	}
     
     private synchronized void processAnswerInputCommand() {
 	    try {
 	        int roomKey = input.readInt();
 	        String answer=input.readUTF();
 	        System.out.println("정답받은 것 : "+answer);
 	        for (Room room : rooms) {
 	            if (room.roomKey == roomKey) {
 	                room.answerCheck(answer,nickName);
 	                break;
 	            }
 	        }
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
  
     
     
     private synchronized void processStartGameCommand() {
    	    try {
    	    	System.out.println("서버에서 startGame받아서processStartGameCommand실행 ");
    	        int roomKey = input.readInt();
    	        for (Room room : rooms) {
    	            if (room.roomKey == roomKey) {
    	            	System.out.println("room찾음");
    	                room.gameStart();
    	                break;
    	            }
    	        }
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    }
    	}
     
     private synchronized void processCreateRoomCommand(String nickName) {
    	    try {
    	    	 String roomName = input.readUTF();
    	    	 String category = input.readUTF();
    	         Room creRoom = new Room(roomName, Server.this.roomKey,category);
    	         rooms.add(creRoom);
    	         creRoom.addClient(this);
    	         
    	         // 먼저 방 생성 성공 메시지와 roomKey만 전송
    	         output.writeUTF("/createRoomOK");
    	         output.writeInt(roomKey);
    	         output.flush();

    	         // 약간의 딜레이를 주고 방 정보 전송
    	         Thread.sleep(100);
    	         
    	         // 그 다음 방 정보 업데이트 메시지 전송
    	         sendRoomInfoToClient(creRoom, this);
    	         
    	         sendRoomListToAll();
    	         Server.this.roomKey++;
    	    } catch (Exception e) {        
    	    } 
    	}
     private synchronized void processEnterRoomCommand(String nickName) {
    	 try {
    		 int roomKey = input.readInt();
    	        for (Room room : rooms) {
    	            if (room.roomKey==roomKey) {
    	                if (room.addClient(this)) {
    	                    sendRoomListToAll();
    	                    // 방에 입장한 모든 클라이언트에게 방 정보 전송
    	                    for(ClientHandler client : room.roomClients) {
    	                        sendRoomInfoToClient(room, client);
    	                    }
    	                }
    	                break;
    	            }
              }
    	 }catch (Exception e) {		
		} 
     }
     
     private synchronized void sendRoomInfoToClient(Room room, ClientHandler client) throws IOException {
    	    synchronized(client.output) {
    	        DataOutputStream out = client.getOutput();
    	        out.writeUTF("/updateRoomInfo");
    	        out.writeUTF(room.categoryLabel);
    	        out.writeUTF(room.descriptionLabelnew);
    	        out.writeUTF(room.timerLabel);
    	        out.writeUTF(room.textArea);
    	        out.writeBoolean(room.startButton);
    	        out.writeUTF(room.winnerName);
    	        out.writeUTF(room.itemMap.get(client.nickName));
    	        
    	        // nameLabels 전송
    	        out.writeInt(room.nameLabels.size());
    	        for(String name : room.nameLabels) {
    	            out.writeUTF(name);
    	        }
    	        
    	        // scoreLabels 전송
    	        out.writeInt(room.scoreLabels.size());
    	        for(String score : room.scoreLabels) {
    	            out.writeUTF(score);
    	        }
    	        out.flush();
    	    }
    	}
     
     private synchronized void processExitRoomCommand() {
    	 try {
    		 int roomKey = input.readInt();
    		 for (Room room : rooms) {
 		        if (room.roomKey == roomKey) {
 		            room.removeClient(this);
 		            output.writeUTF("/exitRoomOK");
 		            output.flush();
 		            sendRoomListToAll();  // 방 목록 업데이트를 모든 클라이언트에게 전송
 		            for(ClientHandler client : room.roomClients) {
                       sendRoomInfoToClient(room, client);
                    }
 		            break;
 		        }
 		    }
    	 }catch (Exception e) {		
		} 
     }
     
     
     
     public DataOutputStream getOutput() {
         return output;
     }
 }
 
 
 
	 private synchronized  void sendRoomListToAll() {
	     String roomListMsg = makeRoomListMessage();
	     for (ClientHandler client : clients) {
	         try {
	             DataOutputStream out = client.getOutput();
	             out.writeUTF("/roomList");
	             out.writeUTF(roomListMsg);
	             out.flush();
	         } catch (IOException e) {
	             e.printStackTrace();
	         }
	     }
	 }
	 //방에대한 정보를 문자열로 보여주는 함수
	  private synchronized  String makeRoomListMessage() {
	       StringBuilder sb = new StringBuilder();
	       for (Room room : rooms) {
	           sb.append(room.roomName).append(",")
	             .append(room.currentUsers).append(",")
	             .append(room.roomKey).append(",")
	             .append(room.maxUsers).append(",")
	             .append(room.category).append(";");
	             
	       }// 클라이언트는 #으로 구분해서 받는다.
	       return sb.toString();
	   }
 
 
 public static void main(String[] args) {
     try {
         new Server(5000);
     } catch (IOException e) {
         e.printStackTrace();
     }
 }
}