package ws.mia.ninetales.discord.command.db;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.DiscordLogService;
import ws.mia.ninetales.discord.command.SlashCommand;
import ws.mia.ninetales.hypixel.HypixelAPI;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
public class StatusCommand extends SlashCommand {
	private static final String COMMAND = "status";
	private final EnvironmentService environmentService;
	private final MongoUserService mongoUserService;
	private final MojangAPI mojangAPI;
	private final HypixelAPI hypixelAPI;
	private final DiscordLogService discordLogService;

	public StatusCommand(EnvironmentService environmentService, MongoUserService mongoUserService, MojangAPI mojangAPI, HypixelAPI hypixelAPI, @Lazy DiscordLogService discordLogService) {
		super();
		this.environmentService = environmentService;
		this.mongoUserService = mongoUserService;
		this.hypixelAPI = hypixelAPI;
		this.mojangAPI = mojangAPI;
		this.discordLogService = discordLogService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "View the status of a user")
				.addOption(OptionType.USER, "user", "user", true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));
	}

	@Override
	public List<String> roles() {
		return List.of(environmentService.getTailRoleId());
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {

		OptionMapping opt = event.getOption("user");
		if(opt == null) {
			event.reply("Specify a user :3").setEphemeral(true).queue();
			return;
		}
		User user = opt.getAsUser();

		if(!mongoUserService.isUserLinked(user.getIdLong())) {
			event.reply("<@%s> is not linked.".formatted(user.getId())).setEphemeral(true).queue();
			return;
		}

		NinetalesUser ntUser = mongoUserService.getUser(user.getIdLong());

		String linkedMinecraftName = mojangAPI.getUsername(ntUser.getMinecraftUuid());
		if(linkedMinecraftName == null) {
			event.reply("Mojang API call failed :(").setEphemeral(true).queue();
			return;
		}

		StringBuilder response = new StringBuilder("<@%s> is linked as `".formatted(user.getId()) + linkedMinecraftName + "`");

		HypixelAPI.GuildPlayer player = Objects.requireNonNull(hypixelAPI.getGuildPlayers()).get(ntUser.getMinecraftUuid());

		if(player != null) {
			response.append("\nThey are in the Ninetales Hypixel guild, with rank: **").append(player.getRank().getHypixelRankName()).append("**")
					.append(" (joined ").append(Math.round(Duration.ofMillis(Instant.now().toEpochMilli() - player.getJoinTimestamp()).toDays())).append(" days ago)");
		} else {
			response.append("\nThey are **not** in the Ninetales Hypixel guild.");
		}

		boolean appChannel = false;
		if(ntUser.getGuildApplicationChannelId() != null) {
			appChannel = true;
			response.append("\nGuild application channel: <#").append(ntUser.getGuildApplicationChannelId()).append(">");
		}

		if(ntUser.getDiscordApplicationChannelId() != null) {
			appChannel = true;
			response.append("\nDiscord application channel: <#").append(ntUser.getDiscordApplicationChannelId()).append(">");
		}

		if(!appChannel && player == null) {
			response.append("\nThey do not have an open application.");
		}

		String reply = response.toString();
		event.reply(reply).setEphemeral(true).queue();
		discordLogService.debug(event, reply);
	}
}
