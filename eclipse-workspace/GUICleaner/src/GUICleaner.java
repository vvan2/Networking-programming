import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUICleaner extends JFrame {
    private JPanel contentPanel;

    public GUICleaner() {
        setTitle("GUI 예제");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        Container contentPane = getContentPane();
        
        contentPanel = new JPanel();
        contentPane.add(contentPanel);

        JButton addButton = new JButton("새로운 패널 추가");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	contentPane.removeAll(); 

                JPanel newPanel = new JPanel();
                newPanel.setBackground(Color.BLUE);
                contentPane.add(newPanel); 

                contentPane.revalidate();   // 레이아웃 매니저에게 컴포넌트의 추가/삭제 및 레이아웃 변경 사항을 반영하도록 요청
                contentPane.repaint();  // 화면을 다시 그려 새로 추가된 패널을 표시
            }
        });

        contentPanel.add(addButton);
    }

    public void run() {
        setVisible(true);
    }
}
