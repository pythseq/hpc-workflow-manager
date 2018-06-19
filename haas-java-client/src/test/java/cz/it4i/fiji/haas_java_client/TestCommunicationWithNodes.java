package cz.it4i.fiji.haas_java_client;

import java.io.IOException;
import java.util.Collections;
import java.util.Scanner;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCommunicationWithNodes {

	public static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.TestCommunicationWithNodes.class);

	private static String[] predefined  = new String[2];
	
	public static void main(String[] args) throws ServiceException, IOException, InterruptedException {
		predefined[0] = "POST /modules/'command:net.imagej.ops.math.PrimitiveMath$IntegerAdd'?process=false HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Length: 13\r\n" +
                "\r\n" +
                "{\"a\":1,\"b\":3}";
		
		predefined[1] = //
				"GET /modules HTTP/1.1\r\n" + // 
				"Host: localhost:8080\r\n" + //
				"User-Agent: curl/7.47.0\r\n" + //
				"Accept: */*\r\n" + //
				"\r\n";
		
		Settings settings = SettingsProvider.getSettings(6l, 3600, 7l, "OPEN-12-20",
				TestingConstants.CONFIGURATION_FILE_NAME);
		HaaSClient client = new HaaSClient(settings);
		long id = 363;//startJob(client);
		String sessionID = client.getSessionID();
		log.info(id + " - " + client.obtainJobInfo(id).getState() + " - " + sessionID);
		if(client.obtainJobInfo(id).getState() != JobState.Running && client.obtainJobInfo(id).getState() != JobState.Queued) {
			client.submitJob(id);
		}
		
		while (client.obtainJobInfo(id).getState() == JobState.Queued) {
			log.info("" + client.obtainJobInfo(id).getState());
			Thread.sleep(5000);
		}

		log.info("adresess " + client.getNodesIps(id));
		try(MidlewareTunnel tunnel = new MidlewareTunnel(id, client.getNodesIps(id).get(0), sessionID)) {
			tunnel.open(8080);
			log.info("localhost:" + tunnel.getLocalPort());
			Scanner sc = new Scanner(System.in);
			System.out.println("Press enter");
			sc.nextLine();
		}
	}

	private static void logData(String direction, byte[] received) {
		log.info(direction + " - " + new String(received));
	}

	private static long startJob(HaaSClient client) {
		long id = client.createJob("Proof", 1, Collections.emptyList());
		client.submitJob(id);
		return id;
	}

}
