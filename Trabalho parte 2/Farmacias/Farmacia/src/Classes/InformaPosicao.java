package Classes;

import jade.core.AID;

public class InformaPosicao implements java.io.Serializable {

	private AID agent;
	private Posicao posicao;

	public InformaPosicao(AID agent, Posicao posicao) {
		super();
		this.agent = agent;
		this.posicao = posicao;
	}
	
	public InformaPosicao(AID agent, int x, int y) {
		super();
		this.agent = agent;
		this.posicao = new Posicao(x,y);
	}
	
	public InformaPosicao() {
		super();
	}

	public AID getAgent() {
		return agent;
	}

	public void setAgent(AID agent) {
		this.agent = agent;
	}

	public Posicao getPosicao() {
		return posicao;
	}

	public void setPosicao(Posicao posicao) {
		this.posicao = posicao;
	}

	@Override
	public String toString() {
		return "InformaPosicao [agent=" + agent + ", posicao=" + posicao + "]";
	}
	
	

}
