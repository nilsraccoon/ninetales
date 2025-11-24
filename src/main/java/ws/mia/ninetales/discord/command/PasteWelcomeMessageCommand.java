package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.ApplicationService;
import ws.mia.ninetales.discord.DiscordLogService;

import java.awt.*;

@Component
public class PasteWelcomeMessageCommand extends SlashCommand {

	private static final String COMMAND = "paste-welcome-message";

	// id constants
	private static final String BUTTON_DISCORD_APPLY_ID = "bNtDiscordApply";
	private static final String BUTTON_GUILD_APPLY_ID = "bNtGuildApply";
	private static final String BUTTON_ASK_QUESTION_ID = "bNtAskQuestion";
	private final EnvironmentService environmentService;
	private final ApplicationService applicationService;
	private final DiscordLogService discordLogService;

	public PasteWelcomeMessageCommand(EnvironmentService environmentService, ApplicationService applicationService, @Lazy DiscordLogService discordLogService) {
		super();
		this.environmentService = environmentService;
		this.applicationService = applicationService;
		this.discordLogService = discordLogService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, ":3")
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		EmbedBuilder embed = new EmbedBuilder()
				.setTitle("Ninetales")
				.setDescription("Select any of the options below to continue :3")
				.setColor(new Color(215, 193, 248, 239));


		Button bDiscordApply = Button.of(ButtonStyle.SUCCESS, BUTTON_DISCORD_APPLY_ID, "Apply to join the Ninetales Discord", Emoji.fromCustom("vulpix", 1403199694489784410L, false));
		Button bGuildApply = Button.of(ButtonStyle.SUCCESS, BUTTON_GUILD_APPLY_ID, "Apply to join the Ninetales Guild", Emoji.fromCustom("shinyavulpix", 1403212590909362369L, false));
		Button bAskQuestion = Button.of(ButtonStyle.SECONDARY, BUTTON_ASK_QUESTION_ID, "Ask a Question", Emoji.fromCustom("miabox", 1406222247957368872L, false));

		event.getChannel().sendMessageEmbeds(embed.build())
				.addComponents(ActionRow.of(bDiscordApply), ActionRow.of(bGuildApply), ActionRow.of(bAskQuestion)).queue();

		event.reply(":3")
				.setEphemeral(true)
				.queue();

		discordLogService.debug(event);
	}


	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if (event.getComponentId().equals(BUTTON_DISCORD_APPLY_ID)) {
			applicationService.createDiscordApplicationChannel(event.getUser(), event.getGuild(),
					() -> {
						event.reply("You need to be linked to apply! Head over to <#%s> first.".formatted(environmentService.getLinkChannelId())).setEphemeral(true).queue();
					},
					(nt) -> {
						event.reply("What do you think you're doing here :p").setEphemeral(true).queue();
					},
					(nt) -> {
						event.reply("You already have an open application, you can't apply again you goober :p").setEphemeral(true).queue();
					},
					(tc, nt) -> {
						event.reply("Head over to <#%s> to fill in your application :3".formatted(tc.getIdLong())).setEphemeral(true).queue();
					});
		}


		if (event.getComponentId().equals(BUTTON_GUILD_APPLY_ID)) {
			applicationService.createGuildApplicationChannel(event.getUser(), event.getGuild(),
					() -> {
						event.reply("You need to be linked to apply! Head over to <#%s> first.".formatted(environmentService.getLinkChannelId())).setEphemeral(true).queue();
					},
					(nt) -> {
						event.reply("What do you think you're doing here :p").setEphemeral(true).queue();
					},
					(nt) -> {
						event.reply("You already have an open application, you can't apply again you goober :p").setEphemeral(true).queue();
					},
					(tc, nt) -> {
						event.reply("Head over to <#%s> to fill in your application :3".formatted(tc.getIdLong())).setEphemeral(true).queue();
					});
		}

		if (event.getComponentId().equals(BUTTON_ASK_QUESTION_ID)) {
			applicationService.createQuestionChannel(event.getUser(), event.getGuild(),
					(ntUser) -> {
						event.reply("You already have an open questions channel at <#%s>.\nIf you have any additional questions, ask them there :3"
										.formatted(ntUser.getQuestionChannelId()))
								.setEphemeral(true).queue();
					},
					(tc, ntUser) -> {
						tc.sendMessage("Ask any questions you have about Ninetales below, a Tail will be with you shortly to help :3").queue();

						event.reply("Head over to <#%s> to ask your questions :3".formatted(tc.getIdLong())).setEphemeral(true).queue();
					});

		}

	}


}
