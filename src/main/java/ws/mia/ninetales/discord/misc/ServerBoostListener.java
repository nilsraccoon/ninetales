package ws.mia.ninetales.discord.misc;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;

@Component
public class ServerBoostListener extends ListenerAdapter {

	private final EnvironmentService environmentService;

	public ServerBoostListener(EnvironmentService environmentService) {
		this.environmentService = environmentService;
	}

	@Override
	public void onGuildMemberUpdateBoostTime(GuildMemberUpdateBoostTimeEvent event) {
		if (event.getOldTimeBoosted() == null && event.getNewTimeBoosted() != null && environmentService.getServerBoostMessageChannelId() != null) {

			TextChannel announceChannel = event.getGuild().getTextChannelById(environmentService.getServerBoostMessageChannelId());
			if (announceChannel != null) {
				announceChannel.sendMessage("<@%s> boosted the server :3".formatted(event.getUser().getId())).queue();
			}

		}

	}

}
