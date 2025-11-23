package ws.mia.ninetales.hypixel;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;

public enum HypixelGuildRank {
	GUILD_MASTER("Guild Master", EnvironmentServiceInjector.environmentService.getTailRoleId()),
	TAIL("Tails", EnvironmentServiceInjector.environmentService.getTailRoleId()),
	VULPIX("Vulpix", EnvironmentServiceInjector.environmentService.getVulpixRoleId()),
	EGG("Egg", EnvironmentServiceInjector.environmentService.getEggRoleId());


	private final String hypixelRankName;
	private final String discordRoleId;

	HypixelGuildRank(String hypixelRankName, String discordRoleId) {
		this.hypixelRankName = hypixelRankName;
		this.discordRoleId = discordRoleId;
	}

	public String getHypixelRankName() {
		return hypixelRankName;
	}

	public String getDiscordRoleId() {
		return discordRoleId;
	}

	public Role getRole(Guild guild){
		return guild.getRoleById(getDiscordRoleId());
	}

	public static HypixelGuildRank fromHypixel(String hypixelRankName) {
		for (HypixelGuildRank rank : values()) {
			if (rank.getHypixelRankName().equals(hypixelRankName)) {
				return rank;
			}
		}
		return null;
	}

	@Component
	public static class EnvironmentServiceInjector {
		private static EnvironmentService environmentService;

		public EnvironmentServiceInjector(EnvironmentService environmentService) {
			if(EnvironmentServiceInjector.environmentService == null) {
				EnvironmentServiceInjector.environmentService = environmentService;
			}
		}

	}

}
