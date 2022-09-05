package Agents;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import Classes.Medicamento;
import Classes.Relatorio;
import org.jfree.data.category.DefaultCategoryDataset;
import JFreeChart.BarChart_Stock;
import JFreeChart.BarChart_Historico_Mensal;
import JFreeChart.BarChart_Historico_Total;
import JFreeChart.BarChart_Lucro_Mensal;
import JFreeChart.BarChart_Lucro_Total;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;


public class Interface extends Agent {
	
	private HashMap<String, Relatorio> estatistica;


	public void setEstatistica(HashMap<String, Relatorio> estatistica) {
		this.estatistica = estatistica;
	}

	protected void setup( ) {
			
		System.out.println("Starting Interface.");
		System.out.println("********************************************************************************************************************************************************************\n");
		
		estatistica = new HashMap<String, Relatorio>();
		
		//Behaviours
		this.addBehaviour(new Receber());
		this.addBehaviour(new MostrarEstatisticas(this,1300));
	}
	
	protected void takeDown() {
		super.takeDown();
	}
	
	//Behaviour para receber os relatórios da Farmacia
	private class Receber extends CyclicBehaviour { 
		HashMap<String, Relatorio> estatistica = new HashMap<String, Relatorio>();	
				
		public void action() {
			ACLMessage msg = receive();
			//Recebe relatórios da farmácia
			if ((msg != null) && msg.getPerformative() == ACLMessage.INFORM) {
				Relatorio relatorio = new Relatorio();
				
				try {
					relatorio = (Relatorio) msg.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				
				//Adiciona os relatórios às estatísticas
				estatistica.put(msg.getSender().getLocalName(), relatorio);
				setEstatistica(estatistica);
			}
		}
	}
	
	// Ticker behaviour para imprimir estatisticas
	public class MostrarEstatisticas extends TickerBehaviour {

		public MostrarEstatisticas(Agent a, long period) {
			super(a, period);
		}
		
		@Override
		protected void onTick() {
			
			System.out.println("\n********************************************************************************************************************************************************************");
			System.out.println(myAgent.getAID().getLocalName() + ": RELATÓRIO MENSAL.");
			

			//FARMÁCIA COM MAIOR NÚMERO DE VENDAS NO ÚLTIMO MÊS
			System.out.println("\nFARMÁCIA COM MAIOR NÚMERO DE VENDAS NO ÚLTIMO MÊS");
			int soma = 0;
			HashMap<String, Integer> vendas_farmacia = new HashMap<String, Integer>();
			
			//Para cada farmácias, somar a quantidade de produtos vendidos
			for (String farmacia : estatistica.keySet()) {
				
				HashMap<Medicamento, Integer> historico_vendas_mensal = estatistica.get(farmacia).getHistorico_vendas_mensal();

				for (Medicamento medicamento : historico_vendas_mensal.keySet()) {
					soma += historico_vendas_mensal.get(medicamento);
				}	
				vendas_farmacia.put(farmacia, soma);	
				soma = 0;
			}
			
			//Verificar a farmácia com mais produtos vendidos
			Map.Entry<String, Integer> maxEntry = null;
			for (Map.Entry<String, Integer> entry : vendas_farmacia.entrySet()) {
				
			    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
			        maxEntry = entry;
			    }
			}
			
			System.out.println(maxEntry + " unidades");
			
			//FARMÁCIA COM MAIOR LUCRO NO ÚLTIMO MÊS
			System.out.println("\nFARMÁCIA COM MAIOR LUCRO NO ÚLTIMO MÊS");
			double lucro = 0;
			HashMap<String, Double> lucro_farmacia = new HashMap<String, Double>();
			
			//Para cada farmácias, somar o lucro dos produtos vendidos
			for (String farmacia : estatistica.keySet()) {
				
				HashMap<Medicamento, Double> lucro_vendas_mensal = estatistica.get(farmacia).getLucro_vendas_mensal();

				for (Medicamento medicamento : lucro_vendas_mensal.keySet()) {
					lucro += lucro_vendas_mensal.get(medicamento);
				}	
				lucro_farmacia.put(farmacia, lucro);	
				lucro = 0.0;
			}
		
			//Verificar a farmácia com mais lucro
			Map.Entry<String, Double> maxEntry1 = null;
			for (Map.Entry<String, Double> entry1 : lucro_farmacia.entrySet()) {
				
			    if (maxEntry1 == null || entry1.getValue().compareTo(maxEntry1.getValue()) > 0) {
			        maxEntry1 = entry1;
			    }
			}
			
			System.out.println(maxEntry1 + " unidades monetárias");
			
			//HISTÓRICO DE VENDAS MENSAL POR FARMÁCIA
			System.out.println("\nHISTÓRICO DE VENDAS MENSAL POR FARMÁCIA");
			
			//para visualização gráfica
			//DefaultCategoryDataset historico_mensal = new DefaultCategoryDataset(); 
	
			//Imprimir no terminal o histórico mensal de cada farmácia
			for (String farmacia : estatistica.keySet()) {
				HashMap<Medicamento, Integer> historico_vendas_mensal = estatistica.get(farmacia).getHistorico_vendas_mensal();
				System.out.println(farmacia +": " + historico_vendas_mensal);
				
				/*for (Medicamento med: historico_vendas_mensal.keySet()) {
		            historico_mensal.addValue(historico_vendas_mensal.get(med), med.getNome(), farmacia);
		        }*/
			}
		    /*BarChart_Historico_Mensal grafico1 = new BarChart_Historico_Mensal("Histórico de Vendas Mensal das Farmácias", historico_mensal);
		    grafico1.pack();
		    grafico1.setVisible(true);*/
		    
			
			//HISTÓRICO DE VENDAS TOTAL POR FARMÁCIA
			System.out.println("\nHISTÓRICO DE VENDAS TOTAL POR FARMÁCIA");
			
			//para visualização gráfica
			//DefaultCategoryDataset historico_total = new DefaultCategoryDataset(); 
			
			//Imprimir no terminal o histórico total de cada farmácia
			for (String farmacia : estatistica.keySet()) {
				HashMap<Medicamento, Integer> historico_vendas_total = estatistica.get(farmacia).getHistorico_vendas_total();
				System.out.println(farmacia +": " + historico_vendas_total);
				
			/*	for (Medicamento med: historico_vendas_total.keySet()) {
		            historico_total.addValue(historico_vendas_total.get(med), med.getNome(), farmacia);
		        }*/
			}
		    /*BarChart_Historico_Total grafico2 = new BarChart_Historico_Total("Histórico de Vendas Total das Farmácias", historico_total);
		    grafico2.pack();
		    grafico2.setVisible(true);*/
			
			//HISTÓRICO DE LUCRO MENSAL POR FARMÁCIA
			System.out.println("\nHISTÓRICO DE LUCRO MENSAL POR FARMÁCIA");
			
			//para visualização gráfica
			//DefaultCategoryDataset lucro_mensal = new DefaultCategoryDataset(); 
			
			//Imprimir no terminal o lucro mensal de cada farmácia
			for (String farmacia : estatistica.keySet()) {
				HashMap<Medicamento, Double> lucro_vendas_mensal = estatistica.get(farmacia).getLucro_vendas_mensal();
				System.out.println(farmacia +": " + lucro_vendas_mensal);
				
				/*for (Medicamento med: lucro_vendas_mensal.keySet()) {
		            lucro_mensal.addValue(lucro_vendas_mensal.get(med), med.getNome(), farmacia);
		        }*/
			}
		    /*BarChart_Lucro_Mensal grafico3 = new BarChart_Lucro_Mensal("Lucro de Vendas Mensal das Farmácias", lucro_mensal);
		    grafico3.pack();
		    grafico3.setVisible(true);*/
			
			
			//HISTÓRICO DE LUCRO TOTAL POR FARMÁCIA
			System.out.println("\nHISTÓRICO DE LUCRO TOTAL POR FARMÁCIA");
			
			//para visualização gráfica
			//DefaultCategoryDataset lucro_total = new DefaultCategoryDataset(); 
			
			//Imprimir no terminal o lucro total de cada farmácia
			for (String farmacia : estatistica.keySet()) {
				HashMap<Medicamento, Double> lucro_vendas_total = estatistica.get(farmacia).getLucro_vendas_total();
				System.out.println(farmacia +": " + lucro_vendas_total);
			
				/*for (Medicamento med: lucro_vendas_total.keySet()) {
		            lucro_total.addValue(lucro_vendas_total.get(med), med.getNome(), farmacia);
		        }*/
			}
		    /*BarChart_Lucro_Total grafico4 = new BarChart_Lucro_Total("Lucro de Vendas Total das Farmácias", lucro_total);
		    grafico4.pack();
		    grafico4.setVisible(true);*/
			
			//TOP 3 MEDICAMENTOS MAIS VENDIDOS POR FARMACIA
			System.out.println("\nTOP 3 DE PRODUTOS MAIS VENDIDOS NO ÚLTIMO MÊS POR FARMACIA");
			// Para cada farmácia, encontrar os 3 medicamentos mais vendidos (maior número de unidades vendidas) 
			for (String farmacia : estatistica.keySet()) {
				
				HashMap<Medicamento, Integer> vendidos_farmacia = new HashMap<Medicamento, Integer>();
				vendidos_farmacia = estatistica.get(farmacia).getHistorico_vendas_mensal();  

				HashMap<Medicamento, Integer> top3 = new HashMap<Medicamento, Integer>();
				int max = 0;
				Medicamento medicamento = null;
								
				for (int i=0; i < 3; i++) {
					for (Medicamento p: vendidos_farmacia.keySet() ) {
						
						if (vendidos_farmacia.get(p) > max) {	
							max = vendidos_farmacia.get(p);
							medicamento = p;
						}
					}
					if (max>0) {
						top3.put(medicamento, max);
						vendidos_farmacia.remove(medicamento);
						max = 0;
					}
				}
				System.out.println(farmacia +": "+ top3);
			}
			
			
			//STOCK DAS FARMÁCIAS
			System.out.println("\nSTOCK DAS FARMÁCIAS");
			
			//para visualização gráfica
			DefaultCategoryDataset stocks = new DefaultCategoryDataset(); 
			
			//Imprimir no terminal o stock de cada farmácia
			for (String farmacia : estatistica.keySet()) {
				ArrayList<Medicamento> stock = estatistica.get(farmacia).getStock();
				System.out.println(farmacia +": "+ stock);
				
				for (Medicamento med: stock) {
		            stocks.addValue(med.getUnidades(), med.getNome(), farmacia);
		        }
		    }
			
			System.out.println("\n********************************************************************************************************************************************************************");
	
		    BarChart_Stock grafico = new BarChart_Stock("Stock das Farmácias", stocks);
		    grafico.pack();
		    grafico.setVisible(true);
		}
	}
}
