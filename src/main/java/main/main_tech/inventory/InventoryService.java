package main.main_tech.inventory;

import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class InventoryService {
	public Map<Server, Boolean> checkConnections(Set<Server> servers) {
		Map<Server, Boolean> result = new HashMap<>();
		for (Server server : servers) {
			if (server.address() == null) continue;
			if (server.sshPort() == null) continue;

			result.put(server, checkConnection(server.address(), server.sshPort()));
		}

		return result;
	}

	public boolean checkConnection(String ip, int port) {
		try (Socket s = new Socket()) {
			s.connect(new InetSocketAddress(ip, port), 1000);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
