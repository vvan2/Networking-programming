package practic.swing;


import java.awt.EventQueue;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MyFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MyFrame frame = new MyFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	


	public MyFrame() {
		
		setBounds(100, 100, 450, 300);
		setTitle("계산기");
		
		contentPane = new JPanel();
		
		JTextArea textArea = new JTextArea();
		contentPane.add(textArea);
		contentPane.setLayout(new GridLayout(4,3,5,5)); 
		for(int i=0; i<10; i++) { // 10개의 버튼 부착
			String text = Integer.toString(i); // i를 문자열로 변환
			JButton button = new JButton(text);
			contentPane.add(button); // 컨텐트팬에 버튼 부착
			}


		setContentPane(contentPane);
		
		contentPane.add(textArea);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
