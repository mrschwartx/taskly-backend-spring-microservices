package com.taskly.backend.users.application;

import com.taskly.backend.shared.application.EventBus;
import com.taskly.backend.shared.domain.DomainEvent;
import com.taskly.backend.shared.domain.EntityIds;
import com.taskly.backend.shared.domain.IntegrationEvents;
import com.taskly.backend.shared.domain.NotFoundException;
import com.taskly.backend.users.domain.User;
import com.taskly.backend.users.domain.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DeleteUser {

	private final UserRepository userRepository;

	private final AvatarStorage avatarStorage;

	private final EventBus eventBus;

	public DeleteUser(UserRepository userRepository, AvatarStorage avatarStorage, EventBus eventBus) {
		this.userRepository = userRepository;
		this.avatarStorage = avatarStorage;
		this.eventBus = eventBus;
	}

	public void execute(UUID requesterId, String userId) {
		OwnAccount.assertOwned(requesterId, userId, "You can only delete your own account");
		UUID id = EntityIds.parse(userId, "user");

		User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found!"));

		if (!userRepository.deleteById(id)) {
			throw new NotFoundException("User not found!");
		}

		avatarStorage.remove(user.getAvatar());
		eventBus.publish(DomainEvent.of(IntegrationEvents.USER_DELETED, Map.of("userId", id)));
	}
}
