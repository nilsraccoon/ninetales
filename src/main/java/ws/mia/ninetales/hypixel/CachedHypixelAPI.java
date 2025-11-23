package ws.mia.ninetales.hypixel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.EnvironmentService;

import java.net.http.HttpClient;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Primary
public class CachedHypixelAPI extends RemoteHypixelAPI {

	private static final long CACHE_DURATION_MINUTES = 5;

	private final Map<UUID, CachedDiscord> discordCache = new ConcurrentHashMap<>();
	private CachedGuildRanks guildRanksCache;

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
	public Map<UUID, HypixelGuildRank> getGuildRanks() {
		if (guildRanksCache != null && !guildRanksCache.isExpired()) {
			return guildRanksCache.ranks;
		}

		Map<UUID, HypixelGuildRank> ranks = super.getGuildRanks();
		if (ranks != null) {
			guildRanksCache = new CachedGuildRanks(ranks, Instant.now());
		}
		return ranks;
	}

	public String retrieveDiscord(UUID uuid) {
		String discord = super.getDiscord(uuid);
		if (discord != null) {
			discordCache.put(uuid, new CachedDiscord(discord, Instant.now()));
		}
		return discord;
	}

	public Map<UUID, HypixelGuildRank> retrieveGuildRanks() {
		Map<UUID, HypixelGuildRank> ranks = super.getGuildRanks();
		if (ranks != null) {
			guildRanksCache = new CachedGuildRanks(ranks, Instant.now());
		}
		return ranks;
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

	private static class CachedGuildRanks {
		final Map<UUID, HypixelGuildRank> ranks;
		final Instant cachedAt;

		CachedGuildRanks(Map<UUID, HypixelGuildRank> ranks, Instant cachedAt) {
			this.ranks = ranks;
			this.cachedAt = cachedAt;
		}

		boolean isExpired() {
			return Instant.now().isAfter(cachedAt.plusSeconds(CACHE_DURATION_MINUTES * 60));
		}
	}
}