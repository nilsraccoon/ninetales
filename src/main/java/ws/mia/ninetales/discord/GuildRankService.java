package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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

	@Scheduled(fixedRate = 1000 * 60 * 3L) // 3mins so we hit API cache
	public void syncRoles() {
		log.debug("Performing role sync");

		Guild guild = jda.getGuildById(environmentService.getDiscordGuildId());

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

						Role guildMemberRole = Objects.requireNonNull(guild.getRoleById(environmentService.getGuildMemberRoleId()));
						Role visitorRole = Objects.requireNonNull(guild.getRoleById(environmentService.getVisitorRoleId()));

						List<Role> allGuildRoles = new ArrayList<>(List.of(HypixelGuildRank.EGG.getRole(guild),
								HypixelGuildRank.VULPIX.getRole(guild),
								HypixelGuildRank.TAIL.getRole(guild),
								HypixelGuildRank.GUILD_MASTER.getRole(guild),
								guildMemberRole));

						if(rank == null) { // not in the guild
							List<Role> rolesToAdd = new ArrayList<>();
							if(ntUser.isDiscordMember()) {
								rolesToAdd.add(visitorRole);
							}

							guild.modifyMemberRoles(dcMember, rolesToAdd, allGuildRoles).queue();
							return;
						}

						// is a guild member
						List<Role> rolesToRemove = allGuildRoles;
						rolesToRemove.removeIf(r -> r.getId().equals(rank.getDiscordRoleId()));
						rolesToRemove.add(visitorRole);
						List<Role> rolesToAdd = List.of(rank.getRole(guild), guildMemberRole);
						rolesToRemove.removeAll(rolesToAdd); // We want to add the intersection. (since GM role is the same as Tail role)

						guild.modifyMemberRoles(dcMember,
								rolesToAdd,
								rolesToRemove).queue();
						mongoUserService.setDiscordMember(ntUser.getDiscordId(), true);

					});
				})
				.onError((t) -> log.warn("Failed to retrieve members", t));
	}

	// Doesn't remove any roles. Primarily for linking
	public void syncFirstRole(Member userToSync, Guild guild) {
		NinetalesUser user = mongoUserService.getUser(userToSync.getIdLong());
		if(user == null) {
			throw new RuntimeException(user.toString());
		}
		HypixelGuildRank rank = hypixelAPI.getGuildRanks().get(user.getMinecraftUuid());
		if(rank == null) {
			mongoUserService.setDiscordMember(user.getDiscordId(), false);
			return;
		}

		mongoUserService.setDiscordMember(user.getDiscordId(), true);
		guild.modifyMemberRoles(userToSync,
				List.of(guild.getRoleById(environmentService.getGuildMemberRoleId()), rank.getRole(guild)), List.of())
				.queue();
	}

}
