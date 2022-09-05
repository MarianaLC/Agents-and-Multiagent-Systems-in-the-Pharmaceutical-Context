package MainContainer;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MainContainer {
	
	Runtime rt;
	ContainerController container;

	public ContainerController initContainerInPlatform(String host, String port, String containerName) {
		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();

		// Create a Profile, where the launch arguments are stored
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.MAIN_HOST, host);
		profile.setParameter(Profile.MAIN_PORT, port);
		// create a non-main agent container
		ContainerController container = rt.createAgentContainer(profile);
		return container;
	}

	public void initMainContainerInPlatform(String host, String port, String containerName) {

		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();

		// Create a Profile, where the launch arguments are stored
		Profile prof = new ProfileImpl();
		prof.setParameter(Profile.CONTAINER_NAME, containerName);
		prof.setParameter(Profile.MAIN_HOST, host);
		prof.setParameter(Profile.MAIN_PORT, port);
		prof.setParameter(Profile.MAIN, "true");
		prof.setParameter(Profile.GUI, "true");

		// create a main agent container
		this.container = rt.createMainContainer(prof);
		rt.setCloseVM(true);

	}

	public void startAgentInPlatform(String name, String classpath) {
		try {
			AgentController ac = container.createNewAgent(name, classpath, new Object[0]);
			ac.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		MainContainer a = new MainContainer();

		try {

			a.initMainContainerInPlatform("localhost", "9888", "MainContainer");
			
			// First Open Central Agent - Manager and Interface
			a.startAgentInPlatform("Gestor", "Agents.Gestor");
			a.startAgentInPlatform("Interface", "Agents.Interface");

			// Provide some time for Agent to register in services

			Thread.sleep(100);

			int limit_fornecedores = 2; // Limit number of Fornecedores
			int limit_farmacias = 2; // Limit number of Farmacias
			int limit_cidadaos = 2; // Limit number of Cidadaos

			// Start agents Farmacias!

			for (int n = 0; n < limit_farmacias; n++) {
							
				// Sleep 1 second for each x farmacias agents added
				if (n % 10 == 0) {
						Thread.sleep(100);
				}
				a.startAgentInPlatform("Farmacia" + n, "Agents.Farmacia");
			}
						
			// Provide some time for Agents to register in services
			Thread.sleep(100);
						
			// Start agents Fornecedores!
			for (int n = 0; n < limit_fornecedores; n++) {
				a.startAgentInPlatform("Fornecedor" + n, "Agents.Fornecedor");
			}

			// Provide some time for Agents to register in services
			Thread.sleep(100);

			// Start agents Cidadaos!

			for (int n = 0; n < limit_cidadaos; n++) {
							
				// Sleep 1 second for each x cidadaos agents added
				if (n % 10 == 0) {
					Thread.sleep(100);
				}
				a.startAgentInPlatform("Cidadao" + n, "Agents.Cidadao");
			}
			
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
