package ws.mia.ninetales.discord.command.link;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.discord.misc.DiscordLogService;
import ws.mia.ninetales.discord.GuildRankService;
import ws.mia.ninetales.discord.command.SlashCommand;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;

import java.time.Instant;
import java.util.UUID;

@Component
public class ForceLinkCommand extends SlashCommand {
	private static final String COMMAND = "force-link";
	private final MongoUserService mongoUserService;
	private final MojangAPI mojangAPI;
	private final TaskScheduler taskScheduler;
	private final GuildRankService guildRankService;
	private final DiscordLogService discordLogService;

	public ForceLinkCommand(MongoUserService mongoUserService, MojangAPI mojangAPI, TaskScheduler taskScheduler, @Lazy GuildRankService guildRankService, @Lazy DiscordLogService discordLogService) {
		super();
		this.mongoUserService = mongoUserService;
		this.mojangAPI = mojangAPI;
		this.taskScheduler = taskScheduler;
		this.guildRankService = guildRankService;
		this.discordLogService = discordLogService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Manually create a link for a player who does not already have one")
				.addOption(OptionType.USER, "discord", "Discord ID (Long)", true)
				.addOption(OptionType.STRING, "minecraft", "Minecraft IGN", true)
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping userOpt = event.getOption("discord");
		OptionMapping minecraftIgnOpt = event.getOption("minecraft");
		if(userOpt == null || minecraftIgnOpt == null) {
			event.reply("missing args :(").setEphemeral(true).queue();
			return;
		}

		User user = userOpt.getAsUser();
		String minecraftIgn = minecraftIgnOpt.getAsString();

		if(mongoUserService.isUserLinked(user.getIdLong())) {
			event.reply("That discord user is already linked to `" + mongoUserService.getUser(user.getIdLong()).getMinecraftUuid().toString() + "`")
					.setEphemeral(true).queue();
			return;
		}

		UUID mcUuid = mojangAPI.getUuid(minecraftIgn);
		if(mcUuid == null) {
			event.reply("Could not retrieve UUID for IGN " + minecraftIgn)
					.setEphemeral(true).queue();
			return;
		}

		if(mongoUserService.isUserLinked(mcUuid)) {
			event.reply("The minecraft user `" + mcUuid + "` is already linked to `" + mongoUserService.getUser(mcUuid).getDiscordId() + "`")
					.setEphemeral(true).queue();
			return;
		}

		if(!mongoUserService.linkUser(user.getIdLong(), mcUuid)) {
			event.reply("Failed to link user (Mongo error?)")
					.setEphemeral(true).queue();
			return;
		}

		taskScheduler.schedule(() -> {
			event.getGuild().retrieveMemberById(user.getId()).queue(member -> {
				guildRankService.syncFirstRole(member, event.getGuild());
			});
		}, Instant.now().plusSeconds(2));

		String msg = "Successfully linked <@" + user.getId() + "> to **" + minecraftIgn + "** (`" + mcUuid + "`)";

		event.reply(msg)
				.setEphemeral(true).queue();

		discordLogService.info(event, msg);
		return;
	}
}
