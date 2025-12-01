package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class SlashCommand extends ListenerAdapter {

	public abstract CommandData getCommand();

	public abstract void onCommand(SlashCommandInteractionEvent event);

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(event.getName().equals(getCommand().getName())) {
			List<String> reqRoles = roles();
			if(!reqRoles.isEmpty()) {
				Set<Role> mRoles;
				if(event.getMember() != null) {
					 mRoles = event.getMember().getUnsortedRoles();
				} else mRoles = Set.of();

				if(reqRoles.stream().noneMatch(roleId -> mRoles.stream().anyMatch(userRole -> userRole.getId().equals(roleId)))) {
					event.reply("You don't have permission to do that :c").setEphemeral(true).queue();
					return;
				}
			}

			onCommand(event);
		}
	}

	/**
	 * If non-empty, the user running this slash command must have at least one of the roles in the returned list.
	 * Each string within the list describes a role id.
	 */
	public List<String> roles() {
		return List.of();
	}

}
