package ws.mia.ninetales.mojang;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Primary
public class CachedMojangAPI extends RemoteMojangAPI {

	private static final long CACHE_DURATION_MINUTES = 30;

	private final Map<String, CachedPlayer> nameCache = new ConcurrentHashMap<>();
	private final Map<UUID, CachedPlayer> uuidCache = new ConcurrentHashMap<>();

	public CachedMojangAPI(HttpClient httpClient, ObjectMapper objectMapper) {
		super(httpClient, objectMapper);
	}

	@Override
	public UUID getUuid(String name) {
		String normalizedName = name.toLowerCase();
		CachedPlayer cached = nameCache.get(normalizedName);
		if (cached != null && !cached.isExpired()) {
			return cached.uuid;
		}

		UUID uuid = super.getUuid(name);
		if (uuid != null) {
			cachePlayer(name, uuid);
		}
		return uuid;
	}

	@Override
	public String getUsername(UUID uuid) {
		CachedPlayer cached = uuidCache.get(uuid);
		if (cached != null && !cached.isExpired()) {
			return cached.username;
		}

		String username = super.getUsername(uuid);
		if (username != null) {
			cachePlayer(username, uuid);
		}
		return username;
	}

	public UUID retrieveUuid(String name) {
		UUID uuid = super.getUuid(name);
		if (uuid != null) {
			cachePlayer(name, uuid);
		}
		return uuid;
	}

	public String retrieveUsername(UUID uuid) {
		String username = super.getUsername(uuid);
		if (username != null) {
			cachePlayer(username, uuid);
		}
		return username;
	}

	private void cachePlayer(String username, UUID uuid) {
		CachedPlayer cachedPlayer = new CachedPlayer(username, uuid, Instant.now());

		// remove stale cached name
		CachedPlayer oldEntry = uuidCache.get(uuid);
		if (oldEntry != null && !oldEntry.username.equalsIgnoreCase(username)) {
			nameCache.remove(oldEntry.username.toLowerCase());
		}

		nameCache.put(username.toLowerCase(), cachedPlayer);
		uuidCache.put(uuid, cachedPlayer);
	}

	private static class CachedPlayer {
		final String username;
		final UUID uuid;
		final Instant cachedAt;

		CachedPlayer(String username, UUID uuid, Instant cachedAt) {
			this.username = username;
			this.uuid = uuid;
			this.cachedAt = cachedAt;
		}

		boolean isExpired() {
			return Instant.now().isAfter(cachedAt.plusSeconds(CACHE_DURATION_MINUTES * 60));
		}
	}
}