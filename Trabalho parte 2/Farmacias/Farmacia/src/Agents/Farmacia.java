package Agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random; 
import Classes.InformaPosicao;
import Classes.Medicamento;
import Classes.Posicao;
import Classes.Relatorio;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Farmacia extends Agent {
	private Posicao posicao_farmacia;
	private InformaPosicao dados_farmacia;
	private ArrayList<Medicamento> stock = new ArrayList<>();
	private HashMap<Medicamento, Integer> historico_vendas_mensal;
	private HashMap<Medicamento, Integer> historico_vendas_total;
	private HashMap<Medicamento, Double> lucro_vendas_mensal;
	private HashMap<Medicamento, Double> lucro_vendas_total;
	private ArrayList<InformaPosicao> pos_fornecedores;
	private AID closestFor;
	private int num_meds;
	private String nome_med;
	private HashMap<String, Integer> medicamento;

	protected void setup() {
		super.setup();
		
		//Inicialização das variáveis
		posicao_farmacia = new Posicao();
		dados_farmacia = new InformaPosicao();
		stock = new ArrayList<>();
		historico_vendas_mensal = new HashMap<Medicamento, Integer>();
		historico_vendas_total = new HashMap<Medicamento, Integer>();
		lucro_vendas_mensal = new HashMap<Medicamento, Double>();
		lucro_vendas_total = new HashMap<Medicamento, Double>();
		pos_fornecedores = new ArrayList<InformaPosicao>();
		closestFor = new AID();
		num_meds = 0;
		nome_med = "";
		medicamento = new HashMap<String, Integer>();
	
		// As coordenadas da farmácia são escolhidas de forma aleatória
		Random rand = new Random();
		posicao_farmacia.setX(rand.nextInt(100));
		posicao_farmacia.setY(rand.nextInt(100));
			
		//Lista de medicamentos vendidos pelas farmácias e inserir stock inicial
		Medicamento med0 = new Medicamento("brufen",10,1.7);
		Medicamento med1 = new Medicamento("benuron",10,2.0);
		Medicamento med2 = new Medicamento("aspirina",10,2.5);
		Medicamento med3 = new Medicamento("xanax",10,3.0);
		Medicamento med4 = new Medicamento("valium",10,4.5);
		Medicamento med5 = new Medicamento("fenistil",10,4.5);
		Medicamento med6 = new Medicamento("voltaren",10,5.6);
		Medicamento med7 = new Medicamento("buscopan",10,3.2);
		Medicamento med8 = new Medicamento("fucidine",10,20.5);
		Medicamento med9 = new Medicamento("kompensan",4,16.5);
		Medicamento med10 = new Medicamento("rennie",4,18.5);
		Medicamento med11 = new Medicamento("bissolvon",4,21.3);
		Medicamento med12 = new Medicamento("strepfen",4,10.2);
		
		stock.add(med0);
		stock.add(med1);
		stock.add(med2);
		stock.add(med3);
		stock.add(med4);
		stock.add(med5);
		stock.add(med6);
		stock.add(med7);
		stock.add(med8);
		stock.add(med9);
		stock.add(med10);
		stock.add(med11);
		stock.add(med12);
		
		//Remove 3 ao acaso, cada farmácia vende 10 medicamentos!		
		for (int i = 0; i <3; i++) {
			Random randomizer = new Random();
			Medicamento random = stock.get(randomizer.nextInt(stock.size())); 
			stock.remove(random);
		}
		
		//Inserir histórico de vendas inicial e lucro inicial
		for(Medicamento med : stock){
			historico_vendas_mensal.put(med, 0);
			historico_vendas_total.put(med, 0);
			lucro_vendas_mensal.put(med, 0.0);
			lucro_vendas_total.put(med, 0.0);
		}
		
		System.out.println("Starting " + this.getAID().getLocalName() + ": \n	- Stock: " + stock + 
				".\n 	- Coordenadas: x = " + posicao_farmacia.getX()+ " y = " + posicao_farmacia.getY()
				+"\n--------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		
		// Cada farmácia regista-se nas páginas amarelas
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("farmacia");
		sd.setName(getLocalName());
		dfd.addServices(sd);
		
		//Behaviours
		this.addBehaviour(new Registar_farmacia());
		this.addBehaviour(new InformarInterface());
		this.addBehaviour(new Receber_msg());
		this.addBehaviour(new RestockProativo(this, 1000));
		
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
	
	//Registo das Farmacias no Gestor
	private class Registar_farmacia extends OneShotBehaviour {
		public void action() {

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("central");
			template.addServices(sd);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);

				// Se o Gestor estiver disponível
				if (result.length > 0) {
					
					dados_farmacia.setAgent(myAgent.getAID());
					dados_farmacia.setPosicao(posicao_farmacia);
					ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
					msg.setContentObject(dados_farmacia);

					for (int i = 0; i < result.length; ++i) {
						msg.addReceiver(result[i].getName());
					}

					myAgent.send(msg);
					
				}
				//Gestor indisponível
				else {
					System.out.println(myAgent.getAID().getLocalName() + ": Gestor indisponível! Agente offline");
				}
				
			} catch (IOException | FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//Enviar relatorio à interface quando a Farmacia é inicializada
		public class InformarInterface extends OneShotBehaviour {
		
			public void action() {
		
				Relatorio relatorio = new Relatorio( myAgent.getAID(), stock, historico_vendas_mensal, historico_vendas_total, lucro_vendas_mensal, lucro_vendas_total);
						
				AID receiver = new AID();
				receiver.setLocalName("Interface");
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				
				try {
					msg.setContentObject(relatorio);
				} catch (IOException e) {
					e.printStackTrace();
				}
				msg.addReceiver(receiver);
				myAgent.send(msg);

			}	
		}
	
	//Receber mensagens e processar pedidos
	private class Receber_msg extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				
				//Receber Registos de Fornecedores e selecionar Fornecedor mais próximo
				if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {

					try {
						System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
								+ " registado!");

						InformaPosicao content = (InformaPosicao) msg.getContentObject();
						pos_fornecedores.add(content);
						
						// Procurar o Fornecedor mais próximo
						float dist_min = 1000.0f;
						int list_pos = -1;

						// Calcular a distancia de cada fornecedor à farmácia
						for (int i = 0; i < pos_fornecedores.size(); i++) {
	
							float distance = (int) Math.sqrt(((Math.pow(
									(pos_fornecedores.get(i).getPosicao().getX() - posicao_farmacia.getX()), 2))
									+ (Math.pow(
											(pos_fornecedores.get(i).getPosicao().getY() - posicao_farmacia.getY()),2))));

							if (dist_min > distance) {
								closestFor = pos_fornecedores.get(i).getAgent();
								list_pos = i;
								dist_min = distance;
							}
						}
														
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//Recebe pedido de informação de stock do Gestor, respondendo-lhe com o medicamento e com o preço total do pedido
				else if (msg.getPerformative() == ACLMessage.REQUEST) {
					try {
						
						Medicamento med = (Medicamento) msg.getContentObject();
			
						System.out.println("\n" + myAgent.getLocalName() + ": Recebi pedido de informação de stock do "+ msg.getSender().getLocalName() + " -> pedido = " + med + ".");
						
						ACLMessage resp = msg.createReply();
						
						// Averigua se tem o medicamento que o Gestor perguntou se tinha e respetiva quantidade
						for (Medicamento m1: stock) {
							
							if (m1.getNome().equals(med.getNome())) {
								
								if (m1.getUnidades() >= med.getUnidades()) {
									
									System.out.println("\n" + myAgent.getLocalName() + " : " + msg.getSender().getLocalName() + " Informo-lhe que tenho o " + med);
									resp.setPerformative(ACLMessage.INFORM);
									double preco_pedido = m1.getPreco() * med.getUnidades();
									med.setPreco(preco_pedido);
									resp.setContentObject(med);
									myAgent.send(resp);	
										
									break;
								}
								
								//tem o medicamento mas não em quantidade suficiente
								else {
									System.out.println("\n" + myAgent.getLocalName() + " : " + msg.getSender().getLocalName() + " informo-lhe não que tenho o " + med + " na quantidade que queria ");
									resp.setContent("\n" + myAgent.getLocalName() + ": Informo-lhe que não tenho os medicamentos dos quais queria informação na quantidade que pretendia");
									resp.setPerformative(ACLMessage.REFUSE);
									resp.setContentObject(med);
									myAgent.send(resp);
									
				
									//Pede ao Fornecedor para repor o medicamento naquela quantidade
									ACLMessage sms = new ACLMessage(ACLMessage.REQUEST_WHEN);
									sms.addReceiver(closestFor);
									sms.setContentObject(med);
									myAgent.send(sms);//envia o medicamento a repor
									
									//Atualizar o stock (porque foi reposto)
									for (Medicamento m2: stock) {
										if (m2.getNome().equals(med.getNome())) {
											int quant_atual = m2.getUnidades();								
											int quant_comprar = med.getUnidades();
											m2.setUnidades(quant_atual + quant_comprar);
										}
									}
									System.out.println("\n" + myAgent.getLocalName() + ": ReStock Por Insuficiência - Atualização -> " + stock);
									break;
								}	
							}
							else {
								//Se chegou ao fim do array e não encontrou o medicamento
								if (m1 == stock.get(stock.size() - 1)){
							
									System.out.println("\n" + myAgent.getLocalName() + " : " + msg.getSender().getLocalName() + " informo-lhe que não tenho o " + med + " em stock!");
									resp.setContent("\n" + myAgent.getLocalName() + ": Informo-lhe que não tenho os medicamentos dos quais queria informação " + msg.getSender().getLocalName());
									resp.setPerformative(ACLMessage.REFUSE);
									resp.setContentObject(med);
									myAgent.send(resp);
									
								}
							}
						}
						
					}catch (UnreadableException | IOException  e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
								
				//Recebe pedido de compra do Gestor
				else if (msg.getPerformative() == ACLMessage.PROPOSE) {
							
					try {
								
						Medicamento med = (Medicamento) msg.getContentObject();
				
						System.out.println("\n" + myAgent.getLocalName() + ": Recebi pedido de compra do "+ msg.getSender().getLocalName() + " -> pedido = " + med + ".");
									 					
						// Confirma pedido Gestor e envia-lhe o pedido
						ACLMessage resp = msg.createReply();
						resp.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						resp.setContentObject(med);
						myAgent.send(resp);
						
						num_meds = num_meds + med.getUnidades(); //unidades vendidas do medicamento
						nome_med = med.getNome(); //nome do medicamento a vender
						medicamento.put(nome_med, num_meds);
						
						//Esvaziar num_meds para próximos pedidos
						num_meds = 0;
														
						//Atualiza stock, historico_vendas_mensal, historico_vendas_total, lucro_vendas_mensal e lucro_vendas_total
						for (Medicamento med1 : stock) {
							
							//Esvaziar as variáveis para novos medicamentos
							Integer vendidos_novo = 0;
							Integer vendidos_total = 0;
							Double lucro_mensal = 0.0;
							Double lucro_total = 0.0;
							
							if (med1.getNome().equals(nome_med)) {
							
								int stock_meds = med1.getUnidades(); //quantidade em stock do medicamento

								//atualizar stock
								med1.setUnidades(stock_meds - medicamento.get(nome_med));
									
								// atualizar historico_vendas_mensal 
								vendidos_novo = historico_vendas_mensal.get(med1) + medicamento.get(nome_med);
								historico_vendas_mensal.put(med1, vendidos_novo);
									
								// Atualizar historico_vendas_total
								vendidos_total = historico_vendas_total.get(med1) + medicamento.get(nome_med);
								historico_vendas_total.put(med1, vendidos_total);
								
								// Atualizar lucro_vendas_mensal
								lucro_mensal = vendidos_novo * med1.getPreco(); 
								lucro_vendas_mensal.put(med1, lucro_mensal);

								// Atualizar lucro_vendas_total
								lucro_total = vendidos_total * med1.getPreco(); 
								lucro_vendas_total.put(med1, lucro_total);
																
							}
						}
						System.out.println("\n" + myAgent.getLocalName() + " A atualizar stocks, vendas e lucros...");
			
					} catch (IOException | UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//Recebe confirmação do Gestor
				else if ( msg.getPerformative() == ACLMessage.CONFIRM) {
								
					System.out.println("\n" + myAgent.getLocalName() + ": Recebi confirmação do "+ msg.getSender().getLocalName() + " de que os medicamentos foram entregues com sucesso ao cliente!");
					
				} else {
						block();
				}
			}
		}
	}
	
	// Behaviour Proativo de reposição de stock para antecipar as necessidades dos cidadãos a cada mês
	public class RestockProativo extends TickerBehaviour {
			
		public RestockProativo(Agent a, long period) {
			super(a, period);
		}
			
		protected void onTick() {
			
			try {
			
				// Calcular as necessidades de reposição de stock
				// O objetivo é assegurar um stock mínimo para o proximo mês que corresponde às vendas do mês anterior
					
				//Medicamentos a pedir ao fornecedor
				ArrayList<Medicamento> lista_reposicao = new ArrayList<>();
			
				for( Medicamento medicamento: historico_vendas_mensal.keySet()) {
						
					for (Medicamento med : stock) {
						
						if (med.getNome().equals(medicamento.getNome()))
						{
							//Se o stock for inferior ao histórico de vendas mensal, então encomenda-se a diferença ao fornecedor
							if ( med.getUnidades() < historico_vendas_mensal.get(medicamento)) {
								
								int quantidade = historico_vendas_mensal.get(medicamento) - med.getUnidades();
								String nome_med = medicamento.getNome();
								double preco_med = medicamento.getPreco();
								Medicamento med_reposicao = new Medicamento(nome_med, quantidade, preco_med); //medicamento com as unidades a adquirir
								lista_reposicao.add(med_reposicao);
							}
						}
					}	
				}
				
				//Se houver necessidade de reposição, pede ao Fornecedor mais próximo para reestabelecer o stock daquela farmácia
				if (lista_reposicao.size()>0) {
					ACLMessage sms = new ACLMessage(ACLMessage.REQUEST);
					sms.addReceiver(closestFor);
					sms.setContentObject(lista_reposicao);
					myAgent.send(sms);//envia a lista de medicamentos a repor
			            
			        //Atualizar o stock (porque foi reposto)
			        for (Medicamento m : lista_reposicao)  {
						for (Medicamento m1: stock) {
							if (m.getNome().equals(m1.getNome())) {
								int quant_atual = m1.getUnidades();
								int quant_comprar = m.getUnidades();
								m1.setUnidades(quant_atual + quant_comprar);
							}
						}
			        }
		        }
				System.out.println("\n" + myAgent.getLocalName() + " A proceder ao restock proativo");
				
				//-------------------------------------------------------------------------------------------------------------------------------
				//Enviar relatório mensal  à Interface
				
				// Informar interface das atualizações enviando um relatório
				Relatorio relatorio = new Relatorio(myAgent.getAID(), stock ,historico_vendas_mensal, historico_vendas_total, lucro_vendas_mensal, lucro_vendas_total);
				
				AID receiver = new AID();
				receiver.setLocalName("Interface");
					
				ACLMessage m = new ACLMessage(ACLMessage.INFORM);
				m.setContentObject(relatorio);
				m.addReceiver(receiver);
				myAgent.send(m);
				
			    //Fazer reset às variáveis "mensais"
				// reset ao historico de vendas mensal
				for (Medicamento m2: historico_vendas_mensal.keySet()) {
					historico_vendas_mensal.put(m2, 0);
				}
				
				// reset ao historico de vendas mensal
				for (Medicamento m3: lucro_vendas_mensal.keySet()) {
					lucro_vendas_mensal.put(m3, 0.0);
				}
				
				//reset ao num_meds e nome_meds
				num_meds = 0;
				nome_med = "";
							
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
