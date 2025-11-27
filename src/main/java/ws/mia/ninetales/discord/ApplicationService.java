package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumPost;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.hypixel.HypixelAPI;
import ws.mia.ninetales.hypixel.HypixelGuildRank;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
public class ApplicationService {

	private static final List<String> GUILD_APPLICATION_PRE_PROCESS = List.of(
			"Thank you for considering Ninetales! To ensure you have the best application possible, try to answer in as much detail as you're comfortable with. Genuine reasons and experiences will go a long way; every little detail helps Ninetales staff accept you. However, If you don't know how to answer a question or need any clarification, please don't hesitate to ask :3"
	);

	private static final List<String> GUILD_APPLICATION_PROCESS = List.of(
			"1. Why Ninetales? (what stood out?)",
			"2. What do you bring? (Tell us about you!)",
			"3. Do you know anyone? (Any interactions with Guild Members)",
			"4. Anything else you want to add?"
	);

	private static final String GUILD_APPLICATION_POST_PROCESS = "Thank you for your application. Please give our <@&Tail>s some time to evaluate it. They'll be with you shortly :3";

	private static final List<String> DISCORD_APPLICATION_PRE_PROCESS = List.of(
			"Welcome to the Ninetales discord, before you can be verified, please answer the following questions to the best of your ability (the more the better)! Hopefully we'll see you around :3"
	);

	private static final List<String> DISCORD_APPLICATION_PROCESS = List.of(
			"1. Who are you? (Tell us anything you want to tell!)",
			"2. What brings you here? (Why do you want to be here?)"
	);

	private static final String DISCORD_APPLICATION_POST_PROCESS = "Thank you for your application. A <@&Tail> will be with you shortly :3";

	private final MongoUserService mongoUserService;
	private final EnvironmentService environmentService;
	private final MojangAPI mojangAPI;
	private final HypixelAPI hypixelAPI;
	private final DiscordLogService discordLogService;
	private final ApplicationArchiveService applicationArchiveService;

	public ApplicationService(MongoUserService mongoUserService, EnvironmentService environmentService, MojangAPI mojangAPI, HypixelAPI hypixelAPI, @Lazy DiscordLogService discordLogService, @Lazy ApplicationArchiveService applicationArchiveService) {
		this.mongoUserService = mongoUserService;
		this.environmentService = environmentService;
		this.mojangAPI = mojangAPI;
		this.hypixelAPI = hypixelAPI;
		this.discordLogService = discordLogService;
		this.applicationArchiveService = applicationArchiveService;
	}


	public boolean createDiscordApplicationChannel(User user, Guild guild, Runnable userNotLinkedFailure, Consumer<NinetalesUser> notOutsiderFailure,
												   Consumer<NinetalesUser> alreadyHasApplicationOpenFailure, BiConsumer<TextChannel, NinetalesUser> success) {
		if (!mongoUserService.isUserLinked(user.getIdLong())) {
			userNotLinkedFailure.run();
			return false;
		}

		NinetalesUser ntUser = mongoUserService.getUser(user.getIdLong());
		if (ntUser.isDiscordMember()) {
			notOutsiderFailure.accept(ntUser);
			return false;
		}

		if (ntUser.getDiscordApplicationChannelId() != null || ntUser.getGuildApplicationChannelId() != null) {
			alreadyHasApplicationOpenFailure.accept(ntUser);
			return false;
		}

		String mcUsername = mojangAPI.getUsername(ntUser.getMinecraftUuid());
		if (mcUsername == null) mcUsername = ntUser.getMinecraftUuid().toString(); // fallback (Mojang API issues)

		String finalMcUsername = mcUsername;
		prepareUserStaffChannel(guild, user, mcUsername, environmentService.getDiscordApplicationsCategoryId())
				.setTopic("Ninetales Visitor Application for **" + mcUsername + "**")
				.queue(tc -> {
					mongoUserService.setDiscordApplicationChannelId(user.getIdLong(), tc.getIdLong());
					DISCORD_APPLICATION_PRE_PROCESS.forEach(s -> tc.sendMessage(s).queue());
					attemptSendNextApplicationProcessMessage(tc);
					success.accept(tc, ntUser);

					// Create a private staff discussion channel
					prepareStaffChannel(guild, "tail-" + finalMcUsername, environmentService.getDiscordApplicationsCategoryId())
							.setTopic("Tail discussion channel for **" + finalMcUsername + "**'s Visitor application")
							.queue(tailTc -> {
								mongoUserService.setTailDiscussionChannelId(ntUser.getDiscordId(), tailTc.getIdLong());
								tailTc.sendMessage("Use this channel to discuss the visitor application in <#%s>".formatted(tc.getIdLong())).queue();

								discordLogService.info("Created Discord application channel", "For <@%s> `(%s)`\nUser channel: <#%s>\nTail Channel: <#%s>"
										.formatted(ntUser.getDiscordId(), finalMcUsername, tc.getId(), tailTc.getId()));
							});
				});


		return true;
	}

	public boolean createGuildApplicationChannel(User user, Guild guild, Runnable userNotLinkedFailure, Consumer<NinetalesUser> guildMemberFailure,
												 Consumer<NinetalesUser> alreadyHasApplicationOpenFailure, BiConsumer<TextChannel, NinetalesUser> success) {
		if (!mongoUserService.isUserLinked(user.getIdLong())) {
			userNotLinkedFailure.run();
			return false;
		}

		NinetalesUser ntUser = mongoUserService.getUser(user.getIdLong());
		Map<UUID, HypixelAPI.GuildPlayer> gPlayers = hypixelAPI.getGuildPlayers();

		if (gPlayers != null && gPlayers.containsKey(ntUser.getMinecraftUuid())) {
			guildMemberFailure.accept(ntUser);
			return false;
		}

		if (ntUser.getDiscordApplicationChannelId() != null || ntUser.getGuildApplicationChannelId() != null) {
			alreadyHasApplicationOpenFailure.accept(ntUser);
			return false;
		}

		String mcUsername = mojangAPI.getUsername(ntUser.getMinecraftUuid());
		if (mcUsername == null) mcUsername = ntUser.getMinecraftUuid().toString(); // fallback (Mojang API issues)

		String finalMcUsername = mcUsername;
		prepareUserStaffChannel(guild, user, mcUsername, environmentService.getGuildApplicationsCategoryId())
				.setTopic("Ninetales Guild Application for **" + mcUsername + "**")
				.queue(tc -> {
					mongoUserService.setGuildApplicationChannelId(user.getIdLong(), tc.getIdLong());
					GUILD_APPLICATION_PRE_PROCESS.forEach(s -> tc.sendMessage(s).queue());
					attemptSendNextApplicationProcessMessage(tc);

					// Create a private staff discussion channel
					prepareStaffChannel(guild, "tail-" + finalMcUsername, environmentService.getGuildApplicationsCategoryId())
							.setTopic("Tail discussion channel for **" + finalMcUsername + "**'s Guild application")
							.queue(tailTc -> {
								mongoUserService.setTailDiscussionChannelId(ntUser.getDiscordId(), tailTc.getIdLong());
								tailTc.sendMessage("Use this channel to discuss the guild application in <#%s>".formatted(tc.getIdLong())).queue();

								discordLogService.info("Created Guild application channel", "For <@%s> `(%s)`\nUser channel: <#%s>\nTail Channel: <#%s>"
										.formatted(ntUser.getDiscordId(), finalMcUsername, tc.getId(), tailTc.getId()));
							});

					success.accept(tc, ntUser);
				});

		return true;
	}

	public boolean createQuestionChannel(User user, Guild guild, Consumer<NinetalesUser> hasChannelFailure, BiConsumer<TextChannel, @Nullable NinetalesUser> success) {
		NinetalesUser ntUser = mongoUserService.getUser(user.getIdLong()); // may be null

		if (ntUser != null && ntUser.getQuestionChannelId() != null) {
			hasChannelFailure.accept(ntUser);
			return false;
		}

		prepareUserStaffChannel(guild, user, user.getName(), environmentService.getQuestionsCategoryId())
				.queue(tc -> {
					mongoUserService.setQuestionChannelId(user.getIdLong(), tc.getIdLong());
					success.accept(tc, ntUser);

					discordLogService.debug("Created Question application channel", "For <@%s> at <#%s>"
							.formatted(user.getId(), tc.getId()));
				});
		return true;
	}

	/**
	 * Provides common validation for whether a user has permission to Accept/Deny applications. <br>
	 * (only Tails can do this, in application channels) <br>
	 * This method also replies to the event in case of failed validation.
	 *
	 * @param event The deny or accept command
	 * @return Null and an event reply if validation fails, or a `NinetalesUser` object for the user whose application this is if validation is successful
	 */
	private NinetalesUser applicationAcceptDenyValidation(SlashCommandInteractionEvent event) {
		if (Objects.requireNonNull(event.getMember()).getUnsortedRoles().stream().noneMatch(r -> r.getId().equals(environmentService.getTailRoleId()))) {
			event.reply("Hey! You can't do that! :p").setEphemeral(true).queue();
			return null;
		}

		NinetalesUser ntUser = mongoUserService.getUserByApplicationChannelId(event.getChannelIdLong());
		if (ntUser == null) {
			event.reply("Run this in an application channel :3").setEphemeral(true).queue();
			return null;
		}

		return ntUser;
	}

	/**
	 * Common accept application helper for discord and guild applications
	 *
	 * @param event /accept-app command which this is run through
	 */
	public void acceptApplication(SlashCommandInteractionEvent event) {
		NinetalesUser ntUser = applicationAcceptDenyValidation(event);
		if (ntUser == null) return;

		boolean isGuildApp = ntUser.getGuildApplicationChannelId() != null; // if false, it's a discord app
		if (!isGuildApp && ntUser.getDiscordApplicationChannelId() == null) {
			throw new RuntimeException("uhh " + ntUser.toString());
		}

		OptionMapping optMessage = event.getOption("message");
		Optional<String> msg = optMessage != null ? Optional.of(optMessage.getAsString()) : Optional.empty();

		if (isGuildApp) {
			acceptGuildApplication(event.getUser(), ntUser, Objects.requireNonNull(event.getGuild()), msg, event.getChannel().asTextChannel());
			event.reply("Accepted :3\nOnce the player has successfully joined the guild, you can run /close-accepted-app here ^w^").setEphemeral(true).queue();
			discordLogService.info(event, "(for <@%s> `(%s)`)".formatted(ntUser.getDiscordId(), ntUser.getMinecraftUuid()));
		} else {
			acceptDiscordApplication(ntUser, Objects.requireNonNull(event.getGuild()), msg);
			event.reply(":3").setEphemeral(true).queue();
			discordLogService.info(event, "(for <@%s> `(%s)`)".formatted(ntUser.getDiscordId(), ntUser.getMinecraftUuid()));
		}
	}

	/**
	 * Common close helper for discord and guild applications
	 *
	 * @param event /close-app command which this is run through
	 */
	public void closeApplication(SlashCommandInteractionEvent event) {
		NinetalesUser ntUser = applicationAcceptDenyValidation(event);
		if (ntUser == null) return;

		boolean isGuildApp = ntUser.getGuildApplicationChannelId() != null; // if false, it's a discord app
		if (!isGuildApp && ntUser.getDiscordApplicationChannelId() == null) {
			throw new RuntimeException("uhh " + ntUser);
		}

		event.deferReply(true).queue();
		applicationArchiveService.archive(event.getChannel().asTextChannel());

		event.getChannel().asTextChannel().delete().queue();

		if (ntUser.getTailDiscussionChannelId() != null) {
			mongoUserService.setTailDiscussionChannelId(ntUser.getDiscordId(), null);
			event.getGuild().getTextChannelById(ntUser.getTailDiscussionChannelId()).delete().queue();
		}

		if (ntUser.isAwaitingHypixelInvite()) {
			sendJoinGuildMessage(event.getGuild(), ntUser.getDiscordId());

			event.getGuild().modifyMemberRoles(event.getGuild().retrieveMemberById(ntUser.getDiscordId()).complete(),
							List.of(event.getGuild().getRoleById(environmentService.getEggRoleId()), event.getGuild().getRoleById(environmentService.getGuildMemberRoleId())),
							List.of(event.getGuild().getRoleById(environmentService.getVisitorRoleId())))
					.queue();

		}

		if (ntUser.getGuildApplicationChannelId() != null) {
			String a = ntUser.isAwaitingHypixelInvite() ? "Finalised" : "Denied";
			discordLogService.info(event, "%s Hypixel application for <@%s> `(%s)`".formatted(a, ntUser.getDiscordId(), mojangAPI.getUsername(ntUser.getMinecraftUuid())));
			mongoUserService.setGuildApplicationChannelId(ntUser.getDiscordId(), null);
		}

		if (ntUser.getDiscordApplicationChannelId() != null) {
			discordLogService.info(event, "Denied Discord application for <@%s> `(%s)`".formatted(ntUser.getDiscordId(), mojangAPI.getUsername(ntUser.getMinecraftUuid())));
			mongoUserService.setDiscordApplicationChannelId(ntUser.getDiscordId(), null);
		}

		mongoUserService.setAwaitingHypixelInvite(ntUser.getDiscordId(), false);
	}

	private void acceptDiscordApplication(NinetalesUser ntApplicant, Guild guild, Optional<String> message) {
		sendJoinDiscordMessage(guild, ntApplicant.getDiscordId());

		guild.retrieveMemberById(ntApplicant.getDiscordId()).queue(member -> {
			member.getUser().openPrivateChannel().queue(p -> {
				p.sendMessage("After careful consideration by our staff team, your application to join the Ninetales discord has been **accepted**!\nWelcome :3")
						.queue();

				message.ifPresent(msg -> p.sendMessage("A message from our tails: " + msg).queue());

				mongoUserService.setDiscordApplicationChannelId(ntApplicant.getDiscordId(), null);
				mongoUserService.setDiscordMember(ntApplicant.getDiscordId(), true);

				// give visitor role
				guild.addRoleToMember(member, guild.getRoleById(environmentService.getVisitorRoleId())).queue();

				// delete channels
				guild.getTextChannelById(ntApplicant.getDiscordApplicationChannelId()).delete().queue();
				if (ntApplicant.getTailDiscussionChannelId() != null) {
					mongoUserService.setTailDiscussionChannelId(ntApplicant.getDiscordId(), null);
					guild.getTextChannelById(ntApplicant.getTailDiscussionChannelId()).delete().queue();
				}

			}, (t) -> {
				throw new RuntimeException(t);
			});
		});
	}

	private void acceptGuildApplication(User caller, NinetalesUser ntApplicant, Guild guild, Optional<String> message, TextChannel appChannel) {
		guild.retrieveMemberById(ntApplicant.getDiscordId()).queue(member -> {
			member.getUser().openPrivateChannel().queue(p -> {

				UUID gmUuid = hypixelAPI.getGuildPlayers().entrySet().stream().filter(entry -> {
					return entry.getValue().getRank().equals(HypixelGuildRank.GUILD_MASTER);
				}).map(Map.Entry::getKey).findAny().orElse(null);

				String gmUsername = gmUuid == null ? "lynyy" : mojangAPI.getUsername(gmUuid);

				appChannel.sendMessage("<@%s> Your application has been **accepted**!\nA Tail has sent you a guild invite.\nRun `/guild accept %s` on Hypixel to finalise your application :3".formatted(ntApplicant.getDiscordId(), gmUsername)).queue(aMsg -> {
					p.sendMessage("After careful consideration by our staff team, your application to join Ninetales has been **accepted**!\nPlease follow the instructions at %s to finalise your application.\nWelcome to the guild <3".formatted(aMsg.getJumpUrl()))
							.queue();
					message.ifPresent(msg -> p.sendMessage("A message from our tails: " + msg).queue());
				});


				mongoUserService.setAwaitingHypixelInvite(ntApplicant.getDiscordId(), true);
				mongoUserService.setDiscordMember(ntApplicant.getDiscordId(), true); // if they're not already

				// close tail discussion channel (from having new msgs)
				if (ntApplicant.getTailDiscussionChannelId() != null) {
					TextChannel tailChannel = guild.getTextChannelById(ntApplicant.getTailDiscussionChannelId());
					tailChannel.sendMessage("The guild application has been accepted by <@" + caller.getId() + ">. Waiting for a Hypixel invite before finalising application.").queue();
					tailChannel.getPermissionContainer()
							.upsertPermissionOverride(guild.getRoleById(environmentService.getTailRoleId()))
							.setAllowed(Permission.VIEW_CHANNEL)
							.setDenied(Permission.MESSAGE_SEND)
							.queue();
				}

				// do role stuff and global message stuff in #closeAcceptedGuildApplication
			}, (t) -> {
				throw new RuntimeException(t);
			});
		});

	}

	public void attemptSendNextApplicationProcessMessage(TextChannel channel) {
		NinetalesUser ntApplicant = mongoUserService.getUserByApplicationChannelId(channel.getIdLong());
		if (ntApplicant == null) return;

		boolean isGuildApp = ntApplicant.getGuildApplicationChannelId() != null;
		if (!isGuildApp && ntApplicant.getDiscordApplicationChannelId() == null) {
			throw new RuntimeException("huh " + ntApplicant);
		}

		// 100 past messages *should* be enough... (we *really* don't expect application channels to be more than a few msgs long)
		channel.getHistory().retrievePast(100).queue(messages -> {
			long botMessages = messages.stream()
					.filter(msg -> msg.getAuthor().equals(channel.getJDA().getSelfUser()))
					.count();

			botMessages -= (isGuildApp) ? GUILD_APPLICATION_PRE_PROCESS.size() : DISCORD_APPLICATION_PRE_PROCESS.size();
			List<String> process = isGuildApp ? GUILD_APPLICATION_PROCESS : DISCORD_APPLICATION_PROCESS;

			if (botMessages < 0)
				botMessages = 0; // fix async issues (sometimes trying to send this before the pre messages causing -1)

			if (botMessages < process.size()) {
				channel.sendMessage(process.get((int) botMessages)).queue();
			}
			if (botMessages == process.size()) {
				String postProcess = isGuildApp ? GUILD_APPLICATION_POST_PROCESS : DISCORD_APPLICATION_POST_PROCESS;
				postProcess = postProcess.replace("<@&Tail>", "<@&" + environmentService.getTailRoleId() + ">");
				channel.sendMessage(postProcess).setAllowedMentions(List.of()).queue(); // don't allow pinging tails here, we do that below.

				// ping in discussion channel
				channel.getGuild().getTextChannelById(ntApplicant.getTailDiscussionChannelId())
						.sendMessage("The application has been filled in. Feel free to take a look <@&" + environmentService.getTailRoleId() + "> :3")
						.queue();

			}

		});

	}

	private ChannelAction<TextChannel> prepareUserStaffChannel(Guild guild, User user, String channelName, String categoryId) {
		return Objects.requireNonNull(guild).createTextChannel(channelName, guild.getCategoryById(categoryId))
				.addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, List.of(Permission.VIEW_CHANNEL))
				.addRolePermissionOverride(guild.getRoleById(environmentService.getTailRoleId()).getIdLong(), List.of(Permission.VIEW_CHANNEL), null)
				.addMemberPermissionOverride(user.getIdLong(), List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null);
	}

	private ChannelAction<TextChannel> prepareStaffChannel(Guild guild, String channelName, String categoryId) {
		return Objects.requireNonNull(guild).createTextChannel(channelName, guild.getCategoryById(categoryId))
				.addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, List.of(Permission.VIEW_CHANNEL))
				.addRolePermissionOverride(guild.getRoleById(environmentService.getTailRoleId()).getIdLong(), List.of(Permission.VIEW_CHANNEL), null);
	}

	public void sendJoinGuildMessage(Guild guild, Long discordId) {
		NinetalesUser ntUser = mongoUserService.getUser(discordId);
		if(ntUser == null) return;
		if(ntUser.hasHadGuildJoinMessage()) return;

		if (environmentService.getGuildJoinMessageChannelId() != null) {
			guild.getTextChannelById(environmentService.getGuildJoinMessageChannelId())
					.sendMessage("<@%s> has joined the guild!".formatted(discordId))
					.queue();
			mongoUserService.setHasHadGuildJoinMessage(discordId, true);
		}
	}

	public void sendJoinDiscordMessage(Guild guild, Long discordId) {
		guild.getTextChannelById(environmentService.getDiscordJoinMessageChannelId())
				.sendMessage("Welcome to Ninetales <@%s>!".formatted(discordId))
				.queue();
	}

}
