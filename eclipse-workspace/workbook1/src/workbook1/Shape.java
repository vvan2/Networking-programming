package workbook1;

public class Shape {
	abstract void print();
	
	abstract void area(double a, double b);

}
public class ShapeEx{
	public static void main(String[] args) {
		Rectangle r= new Rectangle();
		r.print();
		r.area(5.6, 8.4);
		
		Triangle t = new Triangle();
		t.print();
		t.area(24.3, 40.9);
	}
}

public class Rectangle{
	float a;
	float b;
	void Rectangle(float a,float b) {
		float res a*b;
		return res;
	}
}

public class Triangle{
	float a;
	float b;
	void Triangle(float a,float b) {
		float res a*b;
		return res;
	}
}