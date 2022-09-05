package Classes;

public class Posicao implements java.io.Serializable {
	
	private int x,y;
	
	public Posicao(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public Posicao() {
		super();
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "Posicao [x=" + x + ", y=" + y + "]";
	}
	
}
