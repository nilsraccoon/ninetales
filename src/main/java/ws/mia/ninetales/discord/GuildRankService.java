package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.application.ApplicationArchiveService;
import ws.mia.ninetales.discord.application.ApplicationService;
import ws.mia.ninetales.discord.misc.DiscordLogService;
import ws.mia.ninetales.hypixel.CachedHypixelAPI;
import ws.mia.ninetales.hypixel.HypixelAPI;
import ws.mia.ninetales.hypixel.HypixelGuildRank;
import ws.mia.ninetales.mojang.MojangAPI;
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
	private final CachedHypixelAPI cachedHypixelAPI;
	private final DiscordLogService discordLogService;
	private final ApplicationService applicationService;
	private final MojangAPI mojangAPI;
	private final ApplicationArchiveService applicationArchiveService;

	private Map<UUID, HypixelAPI.GuildPlayer> lastGuildPlayers = null;

	public GuildRankService(HypixelAPI hypixelAPI, JDA jda, EnvironmentService environmentService, MongoUserService mongoUserService, CachedHypixelAPI cachedHypixelAPI, DiscordLogService discordLogService, ApplicationService applicationService, MojangAPI mojangAPI, ApplicationArchiveService applicationArchiveService) {
		this.hypixelAPI = hypixelAPI;
		this.jda = jda;
		this.environmentService = environmentService;
		this.mongoUserService = mongoUserService;
		this.cachedHypixelAPI = cachedHypixelAPI;
		this.discordLogService = discordLogService;
		this.applicationService = applicationService;
		this.mojangAPI = mojangAPI;
		this.applicationArchiveService = applicationArchiveService;
	}

	@Scheduled(fixedRate = 1000 * 60 * 6L) // 6mins so we guarantee new users are cached (Hypixel API cache is 5mins)
	public void syncRoles() {
		syncRoles(false);
	}

	public void syncRoles(boolean retrieve) {
		log.debug("Performing role sync");

		Guild guild = jda.getGuildById(environmentService.getDiscordGuildId());

		Map<UUID, HypixelAPI.GuildPlayer> players = (!retrieve) ? cachedHypixelAPI.getGuildPlayers() : cachedHypixelAPI.retrieveGuildPlayers();
		if (players == null) return;

		List<NinetalesUser> allNtUsers = mongoUserService.getAllUsers();

		guild.loadMembers().onSuccess(members -> {
					members.forEach(dcMember -> {
						NinetalesUser ntUser = allNtUsers.stream().filter(a -> a.getDiscordId() == dcMember.getIdLong()).findFirst().orElse(null);

						Role guildMemberRole = Objects.requireNonNull(guild.getRoleById(environmentService.getGuildMemberRoleId()));
						Role visitorRole = Objects.requireNonNull(guild.getRoleById(environmentService.getVisitorRoleId()));

						List<Role> allGuildRoles = new ArrayList<>(List.of(HypixelGuildRank.EGG.getRole(guild),
								HypixelGuildRank.VULPIX.getRole(guild),
								HypixelGuildRank.TAIL.getRole(guild),
								HypixelGuildRank.GUILD_MASTER.getRole(guild),
								guildMemberRole));

						if (ntUser == null) {
							// Likely part of migration. Just remove all roles managed by the bot from them.
							allGuildRoles.add(visitorRole);
							guild.modifyMemberRoles(dcMember, List.of(), allGuildRoles).queue();
							return;
						}

						if (ntUser.getMinecraftUuid() == null) {
							// Remove all guild roles since we don't know if they're in the guild
							guild.modifyMemberRoles(dcMember, List.of(), allGuildRoles).queue();
							return;
						}

						HypixelGuildRank rank = players.containsKey(ntUser.getMinecraftUuid()) ? players.get(ntUser.getMinecraftUuid()).getRank() : null;

						if (rank == null) { // not in the guild
							List<Role> rolesToAdd = new ArrayList<>();
							if (ntUser.isDiscordMember()) {
								rolesToAdd.add(visitorRole);
							}

							guild.modifyMemberRoles(dcMember, rolesToAdd, allGuildRoles).queue();
							return;
						}

						// is a guild member //

						// check if they joined recently; send a join message if they did (and one hasn't been sent already)
						// check 2 ways to be sure
						if (lastGuildPlayers != null && !lastGuildPlayers.containsKey(ntUser.getMinecraftUuid()) && !ntUser.hasHadGuildJoinMessage()) {
							applicationService.sendJoinGuildMessage(guild, ntUser.getDiscordId());
						}

						List<Role> rolesToRemove = allGuildRoles;
						rolesToRemove.removeIf(r -> r.getId().equals(rank.getDiscordRoleId()));
						rolesToRemove.add(visitorRole);
						List<Role> rolesToAdd = List.of(rank.getRole(guild), guildMemberRole);
						rolesToRemove.removeAll(rolesToAdd); // We want to add the intersection. (since GM role is the same as Tail role)

						guild.modifyMemberRoles(dcMember,
								rolesToAdd,
								rolesToRemove).queue();
						mongoUserService.setDiscordMember(ntUser.getDiscordId(), true);

						// check if they have open app channels (close them, it means they've joined the guild)
						if (ntUser.getGuildApplicationChannelId() != null) {
							TextChannel tc = guild.getTextChannelById(ntUser.getGuildApplicationChannelId());
							applicationArchiveService.archiveApplication(tc, () -> {
								tc.delete().queue();
								mongoUserService.setGuildApplicationChannelId(ntUser.getDiscordId(), null);

								if (ntUser.isAwaitingHypixelInvite()) {
									mongoUserService.setAwaitingHypixelInvite(ntUser.getDiscordId(), false);
								}
							});
						}

						if (ntUser.getTailDiscussionChannelId() != null) {
							guild.getTextChannelById(ntUser.getTailDiscussionChannelId()).delete().queue();
							mongoUserService.setTailDiscussionChannelId(ntUser.getDiscordId(), null);
						}

					});
					this.lastGuildPlayers = players;
				})
				.onError((t) -> {
					log.warn("Failed to retrieve members", t);
					discordLogService.warn("Failed to sync (retrieve) members", t.getMessage());
				});

	}

	// Doesn't remove any roles. Primarily for linking
	public void syncFirstRole(Member userToSync, Guild guild) {
		NinetalesUser user = mongoUserService.getUser(userToSync.getIdLong());
		if (user == null) {
			throw new RuntimeException(user.toString());
		}

		Map<UUID, HypixelAPI.GuildPlayer> players = hypixelAPI.getGuildPlayers();
		if (players == null) return; // fail

		HypixelAPI.GuildPlayer player = players.get(user.getMinecraftUuid());

		if (player == null) {
			mongoUserService.setDiscordMember(user.getDiscordId(), false);
			return;
		}

		mongoUserService.setDiscordMember(user.getDiscordId(), true);
		guild.modifyMemberRoles(userToSync,
						List.of(guild.getRoleById(environmentService.getGuildMemberRoleId()), player.getRank().getRole(guild)), List.of())
				.queue();
	}

}
