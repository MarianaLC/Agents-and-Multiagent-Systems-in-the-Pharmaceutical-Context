package Agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import Classes.FazerPedido;
import Classes.Medicamento;
import Classes.Posicao;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Cidadao extends Agent {
	
	private Posicao posicao_cidadao;
	private ArrayList<String> nomes_meds;
	private String random_medicamento;
	private int uni_meds;

	protected void setup() {
		super.setup();
		
		//Incializações 
		posicao_cidadao = new Posicao();
		nomes_meds = new ArrayList<String>();

		// As coordenadas do Cidadão são escolhidas de forma aleatória
		Random rand = new Random();
		posicao_cidadao.setX(rand.nextInt(100));
		posicao_cidadao.setY(rand.nextInt(100));
		
		//Lista dos nomes dos medicamentos disponíveis nas farmácias
		nomes_meds.add("brufen");
		nomes_meds.add("benuron");
		nomes_meds.add("aspirina");
		nomes_meds.add("xanax");
		nomes_meds.add("valium");
		nomes_meds.add("fenistil");
		nomes_meds.add("voltaren");
		nomes_meds.add("buscopan");
		nomes_meds.add("fucidine");
		nomes_meds.add("kompensan");
		nomes_meds.add("rennie");
		nomes_meds.add("bissolvon");
		nomes_meds.add("strepfen");
		
		// Gerar pedido aleatório 
		Random randomizer = new Random();
					
		//Nome de medicamento aleatório a adquirir
		random_medicamento = (String) nomes_meds.get(randomizer.nextInt(nomes_meds.size()));
					
		//quantidade aleatória (entre 1 e 6) de unidades de medicamento a pedir 
		uni_meds = randomizer.nextInt(6)+1;
			
		System.out.print("Starting "+ this.getAID().getLocalName()+ ".\n	-Coordenadas: x = " + posicao_cidadao.getX() + ", y = " + posicao_cidadao.getY() +".\n"+
		"--------------------------------------------------------------------------------------------------------------------------------------------------------------------\n"); 
		
		//Behaviours
		this.addBehaviour(new Enviar_pedido_gestor(this, 800));
		this.addBehaviour(new Receber_msg());
	}

	protected void takeDown() {
		super.takeDown();
	}
	
	//Envia um pedido para o gestor a cada 800 ms
	private class Enviar_pedido_gestor extends TickerBehaviour {
		
		public Enviar_pedido_gestor(Agent a, long period) {
			super(a, period);
		}
		
		@Override
		protected void onTick() {
			
			//Medicamento selecionado
			Medicamento med_pedido = new Medicamento(random_medicamento, uni_meds);

			//Constituição do pedido
			FazerPedido ped = new FazerPedido(myAgent.getAID(), posicao_cidadao, med_pedido);			 
			
			System.out.println(myAgent.getAID().getLocalName() + " estou a enviar-lhe o meu pedido: " + med_pedido);
			
			// Envia mensagem ao Gestor com o pedido
			ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);
			AID receiver = new AID();
			receiver.setLocalName("Gestor");
			mensagem.addReceiver(receiver);
			
			try {
				mensagem.setContentObject(ped);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myAgent.send(mensagem);
			
		}
	}
	
	//Receber mensagens e processar pedidos
	private class Receber_msg extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
						
				//Receber Lista de Farmacias selecionadas
				if (msg.getPerformative() == ACLMessage.PROPOSE) {

					try {

						HashMap<AID, Double> farmacias_selecionadas = (HashMap<AID, Double>) msg.getContentObject();
						AID farmacia_escolhida = null;
						
						System.out.println("\n" + myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
								+ " recebi a lista de farmácias que tem o meu pedido disponível e vou escolher uma!" + farmacias_selecionadas);
						
						if (farmacias_selecionadas.size() == 1) {
							farmacia_escolhida = farmacias_selecionadas.keySet().iterator().next();
						}
						else {
						
							//Obter a Farmacia cujo custo total é o menor
							double menor = 1000.0f;
							AID farm_mais_barata = new AID();
							
							for (AID fa : farmacias_selecionadas.keySet()) {
								if (farmacias_selecionadas.get(fa) < menor) {
									menor = farmacias_selecionadas.get(fa);
									farm_mais_barata = fa;
								}
							}
											
							//Adicionar a Farmacia mais barata à lista de escolha de farmacia random
							ArrayList<AID> lista_farmacias_random = new ArrayList<AID>();
							lista_farmacias_random.add(farm_mais_barata);
							
							
							//Decidir a que Farmacia comprar
							// Gerar pedido aleatório 
							Random randomizer1 = new Random();
							
							//Colocar as farmacias numa lista para poder escolher aleatoriamente, exceto a mais barata
							ArrayList<AID> lista_farmacias = new ArrayList<AID>();
							
							for (AID fa : farmacias_selecionadas.keySet()) {
								if (fa != farm_mais_barata) {
									lista_farmacias.add(fa);
								}
							}
													
							//Escolha de forma aleatória de uma Farmacia da lista_farmacias para por na lista_farmacias_random (não inclui a mais barata, pois essa já esta nessa lista)
							if (! lista_farmacias.isEmpty()) {
								AID random_farmacia = (AID) lista_farmacias.get(randomizer1.nextInt(lista_farmacias.size()));
								lista_farmacias_random.add(random_farmacia);
							}
										
							//Escolher uma Farmacia (50% probabilidade de escolher a mais barato e 50% de probabilidade de escolher uma outra)
							Random randomizer2 = new Random();
							farmacia_escolhida = (AID) lista_farmacias_random.get(randomizer2.nextInt(lista_farmacias_random.size()));
						}
						
						//Enviar ao cidadao a escolha
						ACLMessage mensagem1 = new ACLMessage(ACLMessage.AGREE);
						AID receiver = new AID();
						receiver.setLocalName("Gestor");
						mensagem1.addReceiver(receiver);
						mensagem1.setContentObject(farmacia_escolhida);
						myAgent.send(mensagem1);
						
						System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
								+ " escolhi efetuar a compra na : " + farmacia_escolhida.getLocalName());
						
					} catch (UnreadableException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				//Receber a compra do Gestor
				} else if (msg.getPerformative() == ACLMessage.INFORM) {
						
					//Enviar a confirmação de entrega ao Gestor
					ACLMessage mensagem2 = new ACLMessage(ACLMessage.CONFIRM);
					AID receiver = new AID();
					receiver.setLocalName("Gestor");
					mensagem2.addReceiver(receiver);
					myAgent.send(mensagem2);
					
					System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
							+ " obrigada por me enviar o meu pedido!");
				}
			}
		}
	}									
}
		