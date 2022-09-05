package Classes;

import jade.core.AID;

public class FazerPedido implements java.io.Serializable {
	
	private AID agent;
	private Posicao pos;
	private Medicamento med;
	
	public FazerPedido(AID agent, Posicao pos, Medicamento med) {
		super();
		this.agent = agent;
		this.pos = pos;
		this.med = med;
	}
	
	public FazerPedido() {
		super();
	}

	public AID getAgent() {
		return agent;
	}

	public void setAgent(AID agent) {
		this.agent = agent;
	}

	public Posicao getPos() {
		return pos;
	}

	public void setPos(Posicao pos) {
		this.pos = pos;
	}

	public Medicamento getMed() {
		return med;
	}

	public void setMed(Medicamento med) {
		this.med = med;
	}

	@Override
	public String toString() {
		return "MakeRequest [agent=" + agent + ", pos=" + pos + ", med=" + med + "]";
	}

}