package Classes;

import java.util.ArrayList;
import java.util.HashMap;
import jade.core.AID;

public class Relatorio implements java.io.Serializable {
	
	private AID agent;
	private ArrayList<Medicamento> stock;
	private HashMap<Medicamento, Integer> historico_vendas_mensal;
	private HashMap<Medicamento, Integer> historico_vendas_total;
	private HashMap<Medicamento, Double> lucro_vendas_mensal = new HashMap<Medicamento, Double>();
	private HashMap<Medicamento, Double> lucro_vendas_total = new HashMap<Medicamento, Double>();

	public Relatorio(AID agent, ArrayList<Medicamento> stock,
			HashMap<Medicamento, Integer> historico_vendas_mensal, HashMap<Medicamento, Integer> historico_vendas_total,
			HashMap<Medicamento, Double> lucro_vendas_mensal, HashMap<Medicamento, Double> lucro_vendas_total) {
		super();
		this.agent = agent;
		this.stock = stock;
		this.historico_vendas_mensal = historico_vendas_mensal;
		this.historico_vendas_total = historico_vendas_total;
		this.lucro_vendas_mensal = lucro_vendas_mensal;
		this.lucro_vendas_total = lucro_vendas_total;
	}
	
	public Relatorio() {
	}

	public AID getagent() {
		return agent;
	}
	
	public void setagent(AID agent) {
		this.agent = agent;
	}
	
	public ArrayList<Medicamento> getStock() {
		return stock;
	}

	public void setStock(ArrayList<Medicamento> stock) {
		this.stock = stock;
	}

	public HashMap<Medicamento, Integer> getHistorico_vendas_mensal() {
		return historico_vendas_mensal;
	}

	public void setHistorico_vendas_mensal(HashMap<Medicamento, Integer> historico_vendas_mensal) {
		this.historico_vendas_mensal = historico_vendas_mensal;
	}

	public HashMap<Medicamento, Integer> getHistorico_vendas_total() {
		return historico_vendas_total;
	}

	public void setHistorico_vendas_total(HashMap<Medicamento, Integer> historico_vendas_total) {
		this.historico_vendas_total = historico_vendas_total;
	}
	
	
	public HashMap<Medicamento, Double> getLucro_vendas_mensal() {
		return lucro_vendas_mensal;
	}

	public void setLucro_vendas_mensal(HashMap<Medicamento, Double> lucro_vendas_mensal) {
		this.lucro_vendas_mensal = lucro_vendas_mensal;
	}

	public HashMap<Medicamento, Double> getLucro_vendas_total() {
		return lucro_vendas_total;
	}

	public void setLucro_vendas_total(HashMap<Medicamento, Double> lucro_vendas_total) {
		this.lucro_vendas_total = lucro_vendas_total;
	}

	@Override
	public String toString() {
		return "Relatorio [agent=" + agent + ", stock=" + stock + ", historico_vendas_mensal=" + historico_vendas_mensal
				+ ", historico_vendas_total=" + historico_vendas_total + ", lucro_vendas_mensal=" + lucro_vendas_mensal
				+ ", lucro_vendas_total=" + lucro_vendas_total + "]";
	}
}
