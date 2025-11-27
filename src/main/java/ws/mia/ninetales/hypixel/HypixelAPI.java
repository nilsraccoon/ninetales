package ws.mia.ninetales.hypixel;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public interface HypixelAPI {

	@Nullable
		// Null on invalid HTTP response
	String getDiscord(UUID uuid);

	@Nullable // Null on invalid HTTP response
	Map<UUID, GuildPlayer> getGuildPlayers();

	class GuildPlayer {
		private final UUID playerUuid;
		private final HypixelGuildRank rank;
		private final long joinTimestamp;

		public GuildPlayer(UUID playerUuid, HypixelGuildRank rank, long joinTimestamp) {
			this.playerUuid = playerUuid;
			this.rank = rank;
			this.joinTimestamp = joinTimestamp;
		}

		public UUID getPlayerUuid() {
			return playerUuid;
		}

		public HypixelGuildRank getRank() {
			return rank;
		}

		public long getJoinTimestamp() {
			return joinTimestamp;
		}

		@Override
		public String toString() {
			return "GuildPlayer{" +
					"playerUuid=" + playerUuid +
					", rank=" + rank +
					", joinTimestamp=" + joinTimestamp +
					'}';
		}
	}
}
