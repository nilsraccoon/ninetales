package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.hypixel.HypixelAPI;
import ws.mia.ninetales.hypixel.HypixelGuildRank;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;
import ws.mia.ninetales.mongo.UserStatus;

import java.util.*;

@Service
public class GuildRankService {

	private static final Logger log = LoggerFactory.getLogger(GuildRankService.class);
	private final HypixelAPI hypixelAPI;
	private final JDA jda;
	private final EnvironmentService environmentService;
	private final MongoUserService mongoUserService;

	public GuildRankService(HypixelAPI hypixelAPI, JDA jda, EnvironmentService environmentService, MongoUserService mongoUserService) {
		this.hypixelAPI = hypixelAPI;
		this.jda = jda;
		this.environmentService = environmentService;
		this.mongoUserService = mongoUserService;
	}

	@Scheduled(fixedRate = 1000 * 60 * 10L) //10mins
	public void syncRoles() {
		log.info("Performing role sync");

		Guild guild = jda.getGuildById(environmentService.getDiscordGuildId());
		if (guild == null) {
			log.warn("Discord guild not found ({})", environmentService.getDiscordGuildId());
			return;
		}

		Map<UUID, HypixelGuildRank> ranks = hypixelAPI.getGuildRanks();
		if (ranks == null) return;

		List<NinetalesUser> allNtUsers = mongoUserService.getAllUsers();


		guild.retrieveMembersByIds(false, allNtUsers.stream().map(NinetalesUser::getDiscordId).toList())
				.onSuccess(members -> {
					members.forEach(dcMember -> {
						NinetalesUser ntUser = allNtUsers.stream().filter(a -> a.getDiscordId() == dcMember.getIdLong()).findFirst().orElse(null);
						if(ntUser == null) return;
						if(ntUser.getMinecraftUuid() == null) return;
						HypixelGuildRank rank = ranks.get(ntUser.getMinecraftUuid());
						Role gMemberRole = Objects.requireNonNull(guild.getRoleById(environmentService.getGuildMemberRoleId()));
						List<Role> allRoles = new ArrayList<>(List.of(HypixelGuildRank.EGG.getRole(guild),
								HypixelGuildRank.VULPIX.getRole(guild),
								HypixelGuildRank.TAIL.getRole(guild),
								HypixelGuildRank.GUILD_MASTER.getRole(guild),
								gMemberRole));

						if(rank == null) {
							if(ntUser.getStatus() == UserStatus.GUILD_MEMBER) {
								mongoUserService.setStatus(ntUser.getDiscordId(), UserStatus.DISCORD_MEMBER);
							}

							List<Role> toAdd = new ArrayList<>();
							if(ntUser.getStatus() == UserStatus.DISCORD_MEMBER) {
								toAdd.add(guild.getRoleById(environmentService.getVisitorRoleId()));
							}
							// no rank, remove if they have
							guild.modifyMemberRoles(dcMember, toAdd, allRoles).queue();
						}

						if(rank != null) { // is a guild member
							allRoles.removeIf(r -> r.getId().equals(rank.getDiscordRoleId()));
							allRoles.remove(gMemberRole);
							allRoles.add(guild.getRoleById(environmentService.getVisitorRoleId()));

							guild.modifyMemberRoles(dcMember,
									List.of(rank.getRole(guild), gMemberRole),
									allRoles).queue();

							mongoUserService.setStatus(ntUser.getDiscordId(), UserStatus.GUILD_MEMBER);
						}
					});

				})
				.onError((t) -> {
					log.warn("Failed to retrieve members", t);
				});

	}

}
