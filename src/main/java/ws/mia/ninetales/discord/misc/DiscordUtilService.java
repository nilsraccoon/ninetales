package ws.mia.ninetales.discord.misc;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;

import java.util.List;
import java.util.Objects;

@Component
public class DiscordUtilService {

	private final EnvironmentService environmentService;

	public DiscordUtilService(EnvironmentService environmentService) {
		this.environmentService = environmentService;
	}

	public ChannelAction<TextChannel> prepareUserStaffChannel(Guild guild, User user, String channelName, String categoryId) {
		return Objects.requireNonNull(guild).createTextChannel(channelName, guild.getCategoryById(categoryId))
				.addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, List.of(Permission.VIEW_CHANNEL))
				.addRolePermissionOverride(guild.getRoleById(environmentService.getTailRoleId()).getIdLong(), List.of(Permission.VIEW_CHANNEL), null)
				.addMemberPermissionOverride(user.getIdLong(), List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null);
	}

	public ChannelAction<TextChannel> prepareStaffChannel(Guild guild, String channelName, String categoryId) {
		return Objects.requireNonNull(guild).createTextChannel(channelName, guild.getCategoryById(categoryId))
				.addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, List.of(Permission.VIEW_CHANNEL))
				.addRolePermissionOverride(guild.getRoleById(environmentService.getTailRoleId()).getIdLong(), List.of(Permission.VIEW_CHANNEL), null);
	}

}
