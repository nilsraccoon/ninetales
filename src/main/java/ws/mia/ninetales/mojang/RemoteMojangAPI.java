package ws.mia.ninetales.mojang;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.util.HttpUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class RemoteMojangAPI implements MojangAPI {
	private static final Logger log = LoggerFactory.getLogger(RemoteMojangAPI.class);
	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;

	public RemoteMojangAPI(HttpClient httpClient, ObjectMapper objectMapper) {
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
	}

	@Nullable
	@Override
	public UUID getUuid(String name) {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.mojang.com/users/profiles/minecraft/"+name))
					.GET()
					.header("Content-Type", "application/json")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (HttpUtil.isSuccess(response.statusCode())) {
				// Mojang returns UUID's formatted without -'s
				String raw = objectMapper.readTree(response.body()).get("id").asText();
				long msb = Long.parseUnsignedLong(raw.substring(0, 16), 16);
				long lsb = Long.parseUnsignedLong(raw.substring(16, 32), 16);
				return new UUID(msb, lsb);
			}
			return null;
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Unable to get Mojang UUID for IGN {}", name, e);
			return null;
		}
	}

	@Nullable
	@Override
	public String getUsername(UUID uuid) {
		try {
			// this 3rd party seems to work reliably (vgwiki recommends it)
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/"+uuid.toString()))
					.GET()
					.header("Content-Type", "application/json")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (HttpUtil.isSuccess(response.statusCode())) {
				return objectMapper.readTree(response.body()).get("name").asText();
			}
			return null;
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Unable to get Mojang username for UUID {}", uuid.toString(), e);
			return null;
		}
	}
}
