package ws.mia.ninetales;

import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.discord.DiscordLogService;
import ws.mia.poseidon.api.PoseidonClient;
import ws.mia.poseidon.api.PoseidonHttpClient;
import ws.mia.poseidon.api.model.PoseidonContainer;

@Service
public class RuntimeService {

	private final DiscordLogService discordLogService;

	public RuntimeService(DiscordLogService discordLogService) {
		this.discordLogService = discordLogService;
	}

	@PostConstruct
	private void init() {
		PoseidonClient poseidonClient = new PoseidonHttpClient("https://poseidon.mia.ws");

		StringBuilder msg = new StringBuilder("The bot is now **up**");

		try {
			String poseidonVersion = poseidonClient.getVersion();
			PoseidonContainer ninetalesContainer = poseidonClient.getContainers().stream().filter(pc -> {
				if (pc.getLabels() == null) return false;
				if (!pc.getLabels().containsKey("github.repository")) return false;
				return pc.getLabels().get("github.repository").endsWith("/ninetales");
			}).findAny().orElseThrow();

			msg.append("\n");
			msg.append("-# Deployed through Poseidon v").append(poseidonVersion);
			msg.append("\n");

			String[] ghImg = ninetalesContainer.getLabels().get("github.image").split("-");
			String commitId = ghImg[ghImg.length - 1];
			msg.append("\nCommit ").append("[").append(commitId).append("](https://gh.mia.ws/ninetales/commit/").append(commitId).append(")");

			@Nullable String updateNote = ninetalesContainer.getLabels().get("ninetales.update-note");
			if (updateNote != null) {
				msg.append("\n\n").append(updateNote);
			}

			@Nullable String ninetalesVersion = ninetalesContainer.getLabels().get("arachne.version");
			String startMsg = ninetalesVersion != null ? "Started (v**" + ninetalesVersion + "**)" : "Started";
			discordLogService.warn(startMsg, msg.toString());
		} catch (Exception e) {
			discordLogService.warn("Started", "The bot is now **up**");
			throw new RuntimeException("Unable to log Poseidon startup message", e);
		}

	}

}
