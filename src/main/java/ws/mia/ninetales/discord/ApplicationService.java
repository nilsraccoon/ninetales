package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;
import ws.mia.ninetales.mongo.UserStatus;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
public class ApplicationService {

	private final MongoUserService mongoUserService;
	private final EnvironmentService environmentService;
	private final MojangAPI mojangAPI;

	public ApplicationService(MongoUserService mongoUserService, EnvironmentService environmentService, MojangAPI mojangAPI) {
		this.mongoUserService = mongoUserService;
		this.environmentService = environmentService;
		this.mojangAPI = mojangAPI;
	}


	public boolean createDiscordApplicationChannel(User user, Guild guild, Runnable userNotLinkedFailure, Consumer<NinetalesUser> notOutsiderFailure,
												   Consumer<NinetalesUser> alreadyHasApplicationOpenFailure, BiConsumer<TextChannel, NinetalesUser> success) {
		if (!mongoUserService.isUserLinked(user.getIdLong())) {
			userNotLinkedFailure.run();
			return false;
		}

		NinetalesUser ntUser = mongoUserService.getUser(user.getIdLong());
		if (ntUser.getStatus() != UserStatus.OUTSIDER) {
			notOutsiderFailure.accept(ntUser);
			return false;
		}

		if (ntUser.getDiscordApplicationChannelId() != null || ntUser.getGuildApplicationChannelId() != null) {
			alreadyHasApplicationOpenFailure.accept(ntUser);
			return false;
		}

		String mcUsername = mojangAPI.getUsername(ntUser.getMinecraftUuid());
		if (mcUsername == null) mcUsername = ntUser.getMinecraftUuid().toString();
		prepareUserStaffChannel(guild, user, mcUsername, environmentService.getDiscordApplicationsCategoryId())
				.setTopic("Ninetales Discord Application for " + mcUsername)
				.queue(tc -> {
					mongoUserService.setDiscordApplicationChannelId(user.getIdLong(), tc.getIdLong());
					success.accept(tc, ntUser);
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
		if (ntUser.getStatus() == UserStatus.GUILD_MEMBER) {
			guildMemberFailure.accept(ntUser);
			return false;
		}

		if (ntUser.getDiscordApplicationChannelId() != null || ntUser.getGuildApplicationChannelId() != null) {
			alreadyHasApplicationOpenFailure.accept(ntUser);
			return false;
		}

		String mcUsername = mojangAPI.getUsername(ntUser.getMinecraftUuid());
		if (mcUsername == null) mcUsername = ntUser.getMinecraftUuid().toString();
		prepareUserStaffChannel(guild, user, mcUsername, environmentService.getGuildApplicationsCategoryId())
				.setTopic("Ninetales Guild Application for " + mcUsername)
				.queue(tc -> {
					mongoUserService.setGuildApplicationChannelId(user.getIdLong(), tc.getIdLong());
					success.accept(tc, ntUser);
				});

		return true;
	}

	public boolean createQuestionChannel(User user, Guild guild, Consumer<NinetalesUser> hasChannelFailure, BiConsumer<TextChannel, NinetalesUser> success) {
		NinetalesUser ntUser = mongoUserService.getUser(user.getIdLong());

		if (ntUser != null && ntUser.getQuestionChannelId() != null) {
			hasChannelFailure.accept(ntUser);
			return false;
		}

		prepareUserStaffChannel(guild, user, "q-" + user.getIdLong(), environmentService.getQuestionsCategoryId())
				.queue(tc -> {
					mongoUserService.setQuestionChannelId(user.getIdLong(), tc.getIdLong());
					success.accept(tc, ntUser);
				});
		return true;
	}

	private NinetalesUser applicationAcceptDenyValidation(SlashCommandInteractionEvent event) {
		if(Objects.requireNonNull(event.getMember()).getUnsortedRoles().stream().noneMatch(r -> r.getId().equals(environmentService.getTailRoleId()))) {
			event.reply("Hey! You can't do that! :p").setEphemeral(true).queue();
			return null;
		}

		NinetalesUser ntUser = mongoUserService.getUserByApplicationChannelId(event.getChannelIdLong());
		if(ntUser == null) {
			event.reply("Run this in an application channel :3").setEphemeral(true).queue();
			return null;
		}
		return ntUser;
	}

	public void acceptApplication(SlashCommandInteractionEvent event) {
		NinetalesUser ntUser = applicationAcceptDenyValidation(event);
		if(ntUser == null) return;

		boolean isGuildApp = ntUser.getGuildApplicationChannelId() != null; // if false, it's a discord app
		if(!isGuildApp && ntUser.getDiscordApplicationChannelId() == null) throw new RuntimeException("uhh " + ntUser.toString());

		if(isGuildApp) {
			// TODO send embed in guild channel

		}

		OptionMapping optMessage = event.getOption("message");

		Objects.requireNonNull(event.getGuild()).retrieveMemberById(ntUser.getDiscordId()).queue(member -> {
			member.getUser().openPrivateChannel().queue(p -> {
				if(isGuildApp) {
					p.sendMessage("After careful consideration by our staff team, your application to join Ninetales has been **accepted**!\nWelcome to the guild <3")
							.queue();
					mongoUserService.setStatus(ntUser.getDiscordId(), UserStatus.GUILD_MEMBER);
					event.getChannel().asTextChannel().sendMessage("Your application has been accepted! Accept your Hypixel invite.").queue();
					event.reply("Accepted :3\nOnce the player has successfully joined the guild, you can run /close-accepted-app here ^w^").setEphemeral(true).queue();
					mongoUserService.setAwaitingHypixelInvite(ntUser.getDiscordId(), true);

					Objects.requireNonNull(
							event.getGuild()).removeRoleFromMember(UserSnowflake.fromId(event.getUser().getId()),
							Objects.requireNonNull(event.getGuild().getRoleById(environmentService.getVisitorRoleId()))
					).queue();


				} else {
					p.sendMessage("After careful consideration by our staff team, your application to join the Ninetales discord has been **accepted**!\nWelcome :3")
							.queue();
					mongoUserService.setStatus(ntUser.getDiscordId(), UserStatus.DISCORD_MEMBER);
					Objects.requireNonNull(
							event.getGuild()).addRoleToMember(UserSnowflake.fromId(event.getUser().getId()),
							Objects.requireNonNull(event.getGuild().getRoleById(environmentService.getVisitorRoleId()))
					).queue();
				}

				if(optMessage != null && !optMessage.getAsString().isBlank()) {
					p.sendMessage("A message from our tails: " + optMessage.getAsString()).queue();
				}
			}, (t)-> {
				event.reply("Failed to accept :(\n"+t.toString()).setEphemeral(true).queue();
			});
		});


	}

	public void denyApplication(SlashCommandInteractionEvent event) {
		NinetalesUser ntUser = applicationAcceptDenyValidation(event);
		if(ntUser == null) return;

		boolean isGuildApp = ntUser.getGuildApplicationChannelId() != null; // if false, it's a discord app
		if(!isGuildApp && ntUser.getDiscordApplicationChannelId() == null) throw new RuntimeException("uhh " + ntUser.toString());

		OptionMapping optMessage = event.getOption("reason");

		event.getGuild().retrieveMemberById(ntUser.getDiscordId()).queue(member -> {
			member.getUser().openPrivateChannel().queue(p -> {
				if(isGuildApp) {
					p.sendMessage("After careful consideration by our staff team, your application to join the Ninetales guild has been **denied**.")
							.queue();
				} else {
					p.sendMessage("After careful consideration by our staff team, your application to join the Ninetales discord has been **denied**.")
							.queue();
				}

				if(optMessage != null && !optMessage.getAsString().isBlank()) {
					p.sendMessage("Reason: " + optMessage.getAsString()).queue();
				}
				event.getChannel().asTextChannel().delete().queue();
			}, (t)-> {
				event.reply("Failed to deny :(\n"+t.toString()).setEphemeral(true).queue();
			});
		});

	}

	/**
	 * Close an already accepted application channel once the player has been successfully managed to join.
	 */
	public void closeAcceptedApplication(SlashCommandInteractionEvent event) {
		NinetalesUser ntUser = applicationAcceptDenyValidation(event);
		if(ntUser == null) return;

		if(!ntUser.isAwaitingHypixelInvite()) {
			event.reply("That user isn't awaiting an invite. Did you mean to /accept-app?").setEphemeral(true).queue();
			return;
		}

		mongoUserService.setAwaitingHypixelInvite(ntUser.getDiscordId(), false);
		event.getChannel().asTextChannel().delete().queue();

	}

	private ChannelAction<TextChannel> prepareUserStaffChannel(Guild guild, User user, String channelName, String categoryId) {
		return Objects.requireNonNull(guild).createTextChannel(channelName, guild.getCategoryById(categoryId))
				.addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, List.of(Permission.VIEW_CHANNEL))
				.addRolePermissionOverride(guild.getRoleById(environmentService.getTailRoleId()).getIdLong(), List.of(Permission.VIEW_CHANNEL), null)
				.addMemberPermissionOverride(user.getIdLong(), List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null);
	}


}
