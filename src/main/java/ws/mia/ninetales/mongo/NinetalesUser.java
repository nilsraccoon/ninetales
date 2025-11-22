package ws.mia.ninetales.mongo;

import java.util.UUID;

public class NinetalesUser {

	private long discordId;
	private UUID minecraftUuid;
	private Long discordApplicationChannelId;
	private Long guildApplicationChannelId;
	private Long questionChannelId;
	private UserStatus status;

	public NinetalesUser() {
	}

	public NinetalesUser(long discordId) {
		this.discordId = discordId;
		this.status = UserStatus.OUTSIDER;
	}

	public NinetalesUser(long discordId, UUID minecraftUuid) {
		this.discordId = discordId;
		this.minecraftUuid = minecraftUuid;
		this.status = UserStatus.OUTSIDER;
	}

	public long getDiscordId() {
		return discordId;
	}

	public UUID getMinecraftUuid() {
		return minecraftUuid;
	}

	public Long getDiscordApplicationChannelId() {
		return discordApplicationChannelId;
	}

	public Long getGuildApplicationChannelId() {
		return guildApplicationChannelId;
	}

	public UserStatus getStatus() {
		return status;
	}

	public Long getQuestionChannelId() {
		return questionChannelId;
	}

	protected void setDiscordId(long discordId) {
		this.discordId = discordId;
	}

	protected void setMinecraftUuid(UUID minecraftUuid) {
		this.minecraftUuid = minecraftUuid;
	}

	protected void setDiscordApplicationChannelId(Long discordApplicationChannelId) {
		this.discordApplicationChannelId = discordApplicationChannelId;
	}

	protected void setGuildApplicationChannelId(Long guildApplicationChannelId) {
		this.guildApplicationChannelId = guildApplicationChannelId;
	}

	protected void setStatus(UserStatus status) {
		this.status = status;
	}

	protected void setQuestionChannelId(Long questionChannelId) {
		this.questionChannelId = questionChannelId;
	}

	@Override
	public String toString() {
		return "NinetalesUser{" +
				"discordId=" + discordId +
				", minecraftUuid=" + minecraftUuid +
				", discordApplicationChannelId=" + discordApplicationChannelId +
				", guildApplicationChannelId=" + guildApplicationChannelId +
				", questionChannelId=" + questionChannelId +
				", status=" + status +
				'}';
	}
}