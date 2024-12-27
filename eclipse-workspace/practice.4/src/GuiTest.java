import javax.swing.JButton;
import javax.swing.JFrame;

public class GuiTest {
	public static void main(String[] args) {
		JFrame f = new JFrame("Frame Test");
		f.setTitle("Myname");
		
		JButton button = new JButton("버튼");
		
		f.add(button);
		
		f.setSize(300,200);
		f.setLocation(400,200);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
