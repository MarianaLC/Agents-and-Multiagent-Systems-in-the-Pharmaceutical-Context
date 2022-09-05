package Agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import Classes.InformaPosicao;
import Classes.Medicamento;
import Classes.Posicao;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Fornecedor extends Agent {
	private InformaPosicao pos_atual;
	
	protected void setup() {
		super.setup();
		
		System.out.println("*********************************************************************************************************************************************************************");
		System.out.println("Starting Fornecedor.");

		this.addBehaviour(new Registar_fornecedor());
		this.addBehaviour(new Receber_e_responder_farmacia());
	}
	
	protected void takeDown() {
		super.takeDown();
	}
	
	//Registo dos Fornecedores nas Farmacias
	private class Registar_fornecedor extends OneShotBehaviour {
		public void action() {

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("farmacia");
			template.addServices(sd);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);

				// Se houverem Farmacias disponíveis
				if (result.length > 0) {
										
					Random rand = new Random();
					pos_atual = new InformaPosicao(myAgent.getAID(),
							new Posicao(rand.nextInt(100), rand.nextInt(100)));
					
					ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
					msg.setContentObject(pos_atual);

					for (int i = 0; i < result.length; ++i) {
						msg.addReceiver(result[i].getName());
					}

					myAgent.send(msg);
				}
				//Nenhuma Farmacia disponível
				else {
					System.out.println(myAgent.getAID().getLocalName() + ": Nenhuma Farmacia disponível! Agente offline");
				}
				
			} catch (IOException | FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//Receber pedidos de compra de medicamentos de Farmacias e enviar os pedidos
	private class Receber_e_responder_farmacia extends CyclicBehaviour {
		
		public void action() {
			ACLMessage msg = receive();
	
			if (msg != null) {
				
				//Receber pedido de restock no caso de falta de um medicamento específico
				if (msg.getPerformative() == ACLMessage.REQUEST_WHEN) {
					try {
		
						//O medicamento pedido é o conteúdo da mensagem 
						Medicamento medPedido = (Medicamento) msg.getContentObject();
						
						// Reestabelecer o stock na farmácia
		                System.out.println("\n" + myAgent.getLocalName() + ": Recebi pedido de restock. A reestabelecer o stock na " + 
						msg.getSender().getLocalName() + " para o " + medPedido + ".");
		                					
					} catch (UnreadableException  e1) {
						e1.printStackTrace();
					}
				}
				
				//Receber pedido de stock proativo
				else if (msg.getPerformative() == ACLMessage.REQUEST) {
					try {
						
						//O medicamento pedido é o conteúdo da mensagem 
						ArrayList<Medicamento> medsPedidos = (ArrayList<Medicamento>) msg.getContentObject();
						
						// Reestabelecer o stock na farmácia
		                System.out.println("\n" + myAgent.getLocalName() + ": Recebi pedido de restock. A reestabelecer o stock na " + 
						msg.getSender().getLocalName() + " para os " + medsPedidos + ".");
					
					} catch (UnreadableException  e1) {
						e1.printStackTrace();
					}
					
				} else {
					block();
				}
			}
		}
	}
}
