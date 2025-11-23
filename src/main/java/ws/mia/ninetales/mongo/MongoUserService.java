package ws.mia.ninetales.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import jakarta.annotation.Nullable;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MongoUserService {

	private final MongoCollection<Document> usersCollection;

	public MongoUserService(MongoCollection<Document> usersCollection) {
		this.usersCollection = usersCollection;
	}

	public boolean linkUser(long discordId, UUID minecraftUuid) {
		// Check if this minecraft UUID is already linked to a different discord account
		NinetalesUser existingUser = getUser(minecraftUuid);
		if (existingUser != null && existingUser.getDiscordId() != discordId) {
			return false;
		}

		// Check if this discord ID is already linked to a different minecraft account
		NinetalesUser currentUser = getUser(discordId);
		if (currentUser != null && currentUser.getMinecraftUuid() != null && !currentUser.getMinecraftUuid().equals(minecraftUuid)) {
			return false;
		}

		// Update or insert the user document
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.combine(
						Updates.set("minecraftUuid", minecraftUuid.toString())
				),
				new UpdateOptions().upsert(true)
		);

		return true;
	}

	public List<NinetalesUser> getAllUsers() {
		List<NinetalesUser> users = new ArrayList<>();
		usersCollection.find().forEach(doc -> {
			NinetalesUser user = documentToUser(doc);
			if (user != null) {
				users.add(user);
			}
		});
		return users;
	}

	/// May return a null value, check with isUserLinked, set a field to ensure non-null, or do null-checks on this return.
	public NinetalesUser getUser(long discordId) {
		Document doc = usersCollection.find(Filters.eq("discordId", discordId)).first();
		return documentToUser(doc);
	}

	public NinetalesUser getUser(UUID minecraftUuid) {
		Document doc = usersCollection.find(Filters.eq("minecraftUuid", minecraftUuid.toString())).first();
		return documentToUser(doc);
	}

	public NinetalesUser getUserByApplicationChannelId(long channelId) {
		Bson filter = Filters.or(
				Filters.eq("discordApplicationChannelId", channelId),
				Filters.eq("guildApplicationChannelId", channelId)
		);
		Document doc = usersCollection.find(filter).first();
		return documentToUser(doc);
	}

	public NinetalesUser getUserByQuestionChannelId(long channelId) {
		Document doc = usersCollection.find(Filters.eq("questionChannelId", channelId)).first();
		return documentToUser(doc);
	}

	public UUID getMinecraftUuid(long discordId) {
		Document userDoc = usersCollection.find(Filters.eq("discordId", discordId)).first();
		if (userDoc == null) {
			return null;
		}
		String uuidString = userDoc.getString("minecraftUuid");
		return uuidString != null ? UUID.fromString(uuidString) : null;
	}

	public Long getDiscordId(UUID minecraftUuid) {
		Document userDoc = usersCollection.find(Filters.eq("minecraftUuid", minecraftUuid.toString())).first();
		if (userDoc == null) {
			return null;
		}
		return userDoc.getLong("discordId");
	}

	public boolean isUserLinked(long discordId) {
		Document userDoc = usersCollection.find(Filters.eq("discordId", discordId)).first();
		if (userDoc == null) {
			return false;
		}
		return userDoc.getString("minecraftUuid") != null;
	}

	public boolean isUserLinked(UUID minecraftUuid) {
		Document userDoc = usersCollection.find(Filters.eq("minecraftUuid", minecraftUuid.toString())).first();
		return userDoc != null;
	}

	public boolean deleteUser(long discordId) {
		return usersCollection.deleteOne(Filters.eq("discordId", discordId)).getDeletedCount() > 0;
	}

	public boolean deleteUser(UUID minecraftUuid) {
		return usersCollection.deleteOne(Filters.eq("minecraftUuid", minecraftUuid.toString())).getDeletedCount() > 0;
	}

	public void setDiscordApplicationChannelId(long discordId, Long channelId) {
		ensureUserExists(discordId);
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.set("discordApplicationChannelId", channelId)
		);
	}

	public void setGuildApplicationChannelId(long discordId, Long channelId) {
		ensureUserExists(discordId);
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.set("guildApplicationChannelId", channelId)
		);
	}

	public void setQuestionChannelId(long discordId, Long channelId) {
		ensureUserExists(discordId);
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.set("questionChannelId", channelId)
		);
	}

	public void setDiscordMember(long discordId, boolean b) {
		ensureUserExists(discordId);
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.set("discordMember", b)
		);
	}

	public void setAwaitingHypixelInvite(long discordId, boolean a) {
		ensureUserExists(discordId);
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.set("awaitingHypixelInvite", a)
		);
	}

	private void ensureUserExists(long discordId) {
		if (!isUserLinked(discordId)) {
			Document userDoc = new Document()
					.append("discordId", discordId);
			usersCollection.insertOne(userDoc);
		}
	}

	private NinetalesUser documentToUser(Document doc) {
		if (doc == null) {
			return null;
		}

		NinetalesUser ninetalesUser = new NinetalesUser();
		ninetalesUser.setDiscordId(doc.getLong("discordId"));

		String uuidString = doc.getString("minecraftUuid");
		if (uuidString != null) {
			ninetalesUser.setMinecraftUuid(UUID.fromString(uuidString));
		}

		ninetalesUser.setDiscordApplicationChannelId(doc.getLong("discordApplicationChannelId"));
		ninetalesUser.setGuildApplicationChannelId(doc.getLong("guildApplicationChannelId"));
		ninetalesUser.setQuestionChannelId(doc.getLong("questionChannelId"));
		ninetalesUser.setAwaitingHypixelInvite(doc.getBoolean("awaitingHypixelInvite", false));
		ninetalesUser.setAwaitingHypixelInvite(doc.getBoolean("discordMember", false));

		return ninetalesUser;
	}

}