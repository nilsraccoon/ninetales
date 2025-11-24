package ws.mia.ninetales;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.discord.DiscordLogService;

@Service
public class RuntimeService {

	private final DiscordLogService discordLogService;

	public RuntimeService(DiscordLogService discordLogService) {
		this.discordLogService = discordLogService;
	}

	@PostConstruct
	private void init() {
		discordLogService.warn("Started", "The bot is now **up**");
	}

}
