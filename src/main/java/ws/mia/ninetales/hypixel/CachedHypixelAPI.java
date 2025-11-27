package ws.mia.ninetales.hypixel;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.EnvironmentService;

import java.net.http.HttpClient;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Primary
public class CachedHypixelAPI extends RemoteHypixelAPI {

	private static final long CACHE_DURATION_MINUTES = 5;

	private final Map<UUID, CachedDiscord> discordCache = new ConcurrentHashMap<>();
	private CachedGuildPlayers guildPlayerCache;

	public CachedHypixelAPI(ObjectMapper objectMapper, HttpClient httpClient, EnvironmentService environmentService) {
		super(objectMapper, httpClient, environmentService);
	}

	@Override
	public String getDiscord(UUID uuid) {
		CachedDiscord cached = discordCache.get(uuid);
		if (cached != null && !cached.isExpired()) {
			return cached.discord;
		}

		String discord = super.getDiscord(uuid);
		if (discord != null) {
			discordCache.put(uuid, new CachedDiscord(discord, Instant.now()));
		}
		return discord;
	}

	@Override
	public Map<UUID, GuildPlayer> getGuildPlayers() {
		if(guildPlayerCache != null && !guildPlayerCache.isExpired()) {
			return guildPlayerCache.players;
		}

		Map<UUID, GuildPlayer> players = super.getGuildPlayers();
		if(players != null) {
			guildPlayerCache = new CachedGuildPlayers(players, Instant.now());
		}
		return players;
	}

	@Nullable
	public String retrieveDiscord(UUID uuid) {
		String discord = super.getDiscord(uuid);
		if (discord != null) {
			discordCache.put(uuid, new CachedDiscord(discord, Instant.now()));
		}
		return discord;
	}

	@Nullable
	public Map<UUID, GuildPlayer> retrieveGuildPlayers() {
		Map<UUID, GuildPlayer> players = super.getGuildPlayers();
		if(players != null) {
			guildPlayerCache = new CachedGuildPlayers(players, Instant.now());
		}
		return players;
	}

	private static class CachedDiscord {
		final String discord;
		final Instant cachedAt;

		CachedDiscord(String discord, Instant cachedAt) {
			this.discord = discord;
			this.cachedAt = cachedAt;
		}

		boolean isExpired() {
			return Instant.now().isAfter(cachedAt.plusSeconds(CACHE_DURATION_MINUTES * 60));
		}
	}

	private static class CachedGuildPlayers {
		final Map<UUID, GuildPlayer> players;
		final Instant cachedAt;

		CachedGuildPlayers(Map<UUID, GuildPlayer> players, Instant cachedAt) {
			this.players = players;
			this.cachedAt = cachedAt;
		}

		boolean isExpired() {
			return Instant.now().isAfter(cachedAt.plusSeconds(CACHE_DURATION_MINUTES * 60));
		}
	}
}