package Agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import Classes.InformaPosicao;
import Classes.Medicamento;
import Classes.Posicao;
import Classes.FazerPedido;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Gestor extends Agent {

	private Posicao posicao_gestor;
	private Posicao posicao_cidadao;
	private FazerPedido pedido_cidadao;
	private HashMap<AID, FazerPedido> pedidos_cidadaos;
	private ArrayList<InformaPosicao> pos_farmacias;
	private HashMap<AID, Double> farmacias_disponiveis;
	private HashMap<AID, Double> farmacias_selecionadas;
	private AID farmacia_escolhida;
	private int farmacias_nao_selecionadas;
	private HashMap <AID, AID> cidadao_farmacia;

	protected void setup() {
		super.setup();
		
		posicao_gestor = new Posicao();
		pedidos_cidadaos = new HashMap<AID,FazerPedido>();
		pos_farmacias = new ArrayList<InformaPosicao>();
		farmacias_disponiveis = new HashMap<AID, Double>();
		farmacias_selecionadas = new HashMap<AID, Double>();
		farmacia_escolhida = new AID();
		farmacias_nao_selecionadas = 0;
		cidadao_farmacia = new HashMap<AID, AID>();
		
		System.out.println("Starting Gestor.");
		System.out.println("********************************************************************************************************************************************************************\n");
		
		// As coordenadas do Gestor são escolhidas de forma aleatória
		Random rand = new Random();
		posicao_gestor.setX(rand.nextInt(100));
		posicao_gestor.setY(rand.nextInt(100));
		
		// O Gestor regista-se nas páginas amarelas
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("central");
		sd.setName(getLocalName());
		dfd.addServices(sd);
		
		//Behaviours
		this.addBehaviour(new Receber_msg());
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	protected void takeDown() {

		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		super.takeDown();
	}
	
	//Receber mensagens e processar pedidos
	private class Receber_msg extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
					
				//Receber Registos de Farmacias
				if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {

					try {
						System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
								+ " registada!");

						InformaPosicao content = (InformaPosicao) msg.getContentObject();
						pos_farmacias.add(content);
							
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				//Recebe pedido de informação de medicamentos do Cidadao, enviando uma mensagem a cada Farmacia para saber se vendem ou não o Medicamento pedido
				}else if (msg.getPerformative() == ACLMessage.REQUEST) {		
					try {
							
						FazerPedido ped = (FazerPedido) msg.getContentObject();
				
						System.out.println("\n" + myAgent.getLocalName() + ": Recebi pedido de informação de medicamentos do "+ msg.getSender().getLocalName() + " -> pedido = " + ped + ".");
						
						//Completar o pedido_cidadao
						posicao_cidadao = new Posicao(ped.getPos().getX(), ped.getPos().getY());
						pedido_cidadao = new FazerPedido(msg.getSender(), posicao_cidadao, ped.getMed());
					
						//Adicionar o pedido feito à lista de pedidos atuais do Gestor
						pedidos_cidadaos.put(pedido_cidadao.getAgent(), pedido_cidadao);
						
						// Averigua quais as farmácias que tem o medicamento que o cidadão pediu
						if (pos_farmacias.size() >0) {
							
							for (InformaPosicao inf_pos : pos_farmacias) {
								ACLMessage sms = new ACLMessage(ACLMessage.REQUEST);
								sms.addReceiver(inf_pos.getAgent());
								sms.setContentObject(ped.getMed());
								myAgent.send(sms);
							}
						}			
					}  catch (UnreadableException | IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
				//Recebe mensagem de Farmacias que não tem o pedido disponível
				}else if (msg.getPerformative() == ACLMessage.REFUSE) {		
					try {
						
						Medicamento pedido_nao_atendido = (Medicamento) msg.getContentObject();
						
						farmacias_nao_selecionadas ++;
						
						// Calcular o preço total do pedido e obter as farmacias selecionadas	
						if (pos_farmacias.size() == (farmacias_disponiveis.size() + farmacias_nao_selecionadas)) { 
							
							//Se não existir nenhuma farmácia disponível, a compra não se vai efetuar
							if (pos_farmacias.size() == farmacias_nao_selecionadas) { 
								System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
										+ " Não existe nenhuma farmácia disponível para compra dos medicamentos. Lamento mas o pedido não poderá ser realizado :( ");
								
								
								// Reiniciam-se as variaveis
								farmacias_nao_selecionadas = 0;
								farmacias_disponiveis.clear();
								farmacias_selecionadas.clear();
							}
							else {
								
								//Se os pedidos forem iguais, obtenho o cidadao correspondente a esse pedido
								for (FazerPedido ped2 : pedidos_cidadaos.values()) {
									
									if (ped2.getMed().getNome().equals(pedido_nao_atendido.getNome())) {
									
										Posicao pos_cid = ped2.getPos();
										AID cidadao = ped2.getAgent();
							
										//Calcular o preço da ditância do gestor ao cidadão (0.02 UM por unidade de distância) - preço constante comum a todas as farmácias
										double distance1 = (int) Math.sqrt(((Math.pow(
												(pos_cid.getX() - posicao_gestor.getX()), 2))
										+ (Math.pow((pos_cid.getY() - posicao_gestor.getY()),2))));
								
										double preco_cid_gest = distance1 * 0.02;
									
										//Obter a posição das farmacias selecionadas para calcular a distância entre farmácia e gestor
										for (AID farm : farmacias_disponiveis.keySet()) {
																		
											for (InformaPosicao inf : pos_farmacias) {
												
												if (farm.equals(inf.getAgent())) {
													
													//Calcular o preço da distância das farmácias ao gestor (0.03 UM por unidade de distância)
													double distance = (int) Math.sqrt(((Math.pow(
															(inf.getPosicao().getX() - posicao_gestor.getX()), 2))
													+ (Math.pow((inf.getPosicao().getY() - posicao_gestor.getY()),2))));
											
													double preco_far_gest = distance * 0.03;
													
													//Preço total do pedido indicado pela respetiva farmácia
													double preco_ped = farmacias_disponiveis.get(farm);
													
													//Custo total do pedido
													double custo_total = preco_far_gest + preco_cid_gest + preco_ped;
													
													//Adicionar à lista de farmacias selecionadas
													farmacias_selecionadas.put(farm, custo_total);
												}
											}
										}
											
										ACLMessage sms1 = new ACLMessage(ACLMessage.PROPOSE);
										sms1.addReceiver(cidadao);
										sms1.setContentObject(farmacias_selecionadas);
										myAgent.send(sms1);
											
										// Reiniciam-se as variaveis
										farmacias_nao_selecionadas = 0;
										farmacias_disponiveis.clear();
										farmacias_selecionadas.clear();
									}
								}
							}
						}
					}  catch (IOException | UnreadableException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				
				//Recebe mensagem da Farmácia a informar que tem o medicamento na quantidade pedida e com o preço total
				//E envia ao cliente a lista das farmácias selecionadas
				}else if (msg.getPerformative() == ACLMessage.INFORM) {		
					try {
						
						Medicamento pedido_atendido = (Medicamento) msg.getContentObject();
						Double preco_pedido = pedido_atendido.getPreco();
						
						//Farmacias que tem o pedido disponível
						farmacias_disponiveis.put(msg.getSender(), preco_pedido);
						
						// Calcular o preço total do pedido e obter as farmacias selecionadas
						if (pos_farmacias.size() == (farmacias_disponiveis.size() + farmacias_nao_selecionadas)) { 
							
							for (FazerPedido ped2 : pedidos_cidadaos.values()) {
								if (ped2.getMed().getNome().equals(pedido_atendido.getNome()) && ped2.getMed().getUnidades() == pedido_atendido.getUnidades()) {
								
									Posicao pos_cid = ped2.getPos();
									AID cidadao = ped2.getAgent();
							
									// Calcular o preço total do pedido e obter as farmacias selecionadas
								    
								    //Calcular o preço da ditância do gestor ao cidadão (0.02 UM por unidade de distância) - preço constante comum a todas as farmácias
									double distance1 = (int) Math.sqrt(((Math.pow(
											(pos_cid.getX() - posicao_gestor.getX()), 2))
									+ (Math.pow((pos_cid.getY() - posicao_gestor.getY()),2))));
							
									double preco_cid_gest = distance1 * 0.02;
										
									//Obter a posição das farmacias selecionadas para calcular a distância entre farmácia e gestor
									for (AID farm : farmacias_disponiveis.keySet()) {
																
										for (InformaPosicao inf : pos_farmacias) {
											
											if (farm.equals(inf.getAgent())) {
												
												//Calcular o preço da distância das farmácias ao gestor (0.03 UM por unidade de distância)
												double distance = (int) Math.sqrt(((Math.pow(
														(inf.getPosicao().getX() - posicao_gestor.getX()), 2))
												+ (Math.pow((inf.getPosicao().getY() - posicao_gestor.getY()),2))));
										
												double preco_far_gest = distance * 0.03;
												
												//Preço total do pedido indicado pela respetiva farmácia
												double preco_ped = farmacias_disponiveis.get(farm);
												
												//Custo total do pedido
												double custo_total = preco_far_gest + preco_cid_gest + preco_ped;
												
												//Adicionar à lista de farmacias selecionadas
												farmacias_selecionadas.put(farm, custo_total);
											}
										}
									}
															
									ACLMessage sms1 = new ACLMessage(ACLMessage.PROPOSE);
									sms1.addReceiver(cidadao);
									sms1.setContentObject(farmacias_selecionadas);
									myAgent.send(sms1);
									
									// Reiniciam-se as variaveis
									farmacias_nao_selecionadas = 0;
									farmacias_disponiveis.clear();
									farmacias_selecionadas.clear();
								}
							}
						}
					
					}  catch (UnreadableException | IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				
				//Recebe mensagem do cidadão com a farmácia em que escolheu adquirir o medicamento
				}else if (msg.getPerformative() == ACLMessage.AGREE) {		
					try {
						Medicamento med_escolhido = new Medicamento();
						farmacia_escolhida = (AID) msg.getContentObject();
				
						for (AID cid: pedidos_cidadaos.keySet()) {
							if (msg.getSender().equals(cid)) {
								med_escolhido = pedidos_cidadaos.get(cid).getMed();
							}
						}
						
						cidadao_farmacia.put(msg.getSender(), farmacia_escolhida);
						
						System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
								+ " vai comprar o  " + med_escolhido + " na " + farmacia_escolhida.getLocalName());
				
						//Envia mensagem à farmácia para proceder à compra
						ACLMessage sms2 = new ACLMessage(ACLMessage.PROPOSE);
						sms2.addReceiver(farmacia_escolhida);
						sms2.setContentObject(med_escolhido);
						myAgent.send(sms2);
						
					}  catch (UnreadableException | IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				
				//Recebe mensagem da farmácia com o pedido do cidadão
				}else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
					
					try {
					
						AID cid_escolhido = new AID();
						Medicamento med = (Medicamento) msg.getContentObject();
						
						System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
								+ " vai enviar o pedido: " + med);
						
						for (FazerPedido ped: pedidos_cidadaos.values()) {
						
							if (ped.getMed().getNome().equals(med.getNome()) && ped.getMed().getUnidades() == med.getUnidades()) {
								cid_escolhido = ped.getAgent();
							}
						}
						
						System.out.println(myAgent.getAID().getLocalName() + ": Obrigada " + msg.getSender().getLocalName()
								+ " vou enviar o " + med +  " que me enviou para o " + cid_escolhido.getLocalName());
						
						//Envia mensagem ao cidadão com o seu pedido
						ACLMessage sms3 = new ACLMessage(ACLMessage.INFORM);
						sms3.addReceiver(cid_escolhido);
						sms3.setContentObject(med);
						myAgent.send(sms3);
						
											
					}  catch (UnreadableException | IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
				//Recebe mensagem de confirmação da entrega do cidadão
				}else if (msg.getPerformative() == ACLMessage.CONFIRM) {
					
					System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
							+ " confirma que recebeu o pedido!");
					
					AID farm_escolhida = new AID();
					
					for (AID cid : cidadao_farmacia.keySet()) {
						if (cid.equals(msg.getSender())) {
							farm_escolhida = cidadao_farmacia.get(cid);
						}
					}
				
					//Envia mensagem à Farmacia a confirmar a entrega do pedido
					ACLMessage sms4 = new ACLMessage(ACLMessage.CONFIRM);
					sms4.addReceiver(farm_escolhida);
					sms4.setContent("O cidadão recebeu o seu pedido!");
					myAgent.send(sms4);
					
				}
			}
		}	
	}
}
