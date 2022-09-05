package Classes;

public class Medicamento implements java.io.Serializable {
	
	private String nome;
	private int unidades;
	private double preco;
	
	public Medicamento(String nome, int unidades, double preco) {
		super();
		this.nome = nome;
		this.unidades = unidades;
		this.preco = preco;
	}
	
	public Medicamento(String nome, int unidades) {
		super();
		this.nome = nome;
		this.unidades = unidades;
	}
	
	public Medicamento() {
		super();
	}
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public int getUnidades() {
		return unidades;
	}
	public void setUnidades(int unidades) {
		this.unidades = unidades;
	}
	
	public double getPreco() {
		return preco;
	}

	public void setPreco(double preco) {
		this.preco = preco;
	}

	@Override
	public String toString() {
		return "Medicamento [nome=" + nome + ", unidades=" + unidades + ", preco=" + preco + "]";
	}

}
