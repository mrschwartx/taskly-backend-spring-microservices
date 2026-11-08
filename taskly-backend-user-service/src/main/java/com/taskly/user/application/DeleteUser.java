package com.taskly.user.application;

import com.taskly.user.domain.User;
import com.taskly.user.domain.UserRepository;
import com.taskly.user.shared.application.EventBus;
import com.taskly.user.shared.domain.DomainEvent;
import com.taskly.user.shared.domain.EntityIds;
import com.taskly.user.shared.domain.IntegrationEvents;
import com.taskly.user.shared.domain.NotFoundException;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional
	public void execute(UUID requesterId, String userId) {
		OwnAccount.assertOwned(requesterId, userId, "You can only delete your own account");
		UUID id = EntityIds.parse(userId, "user");
		User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found!"));
		if (!userRepository.deleteById(id)) {
			throw new NotFoundException("User not found!");
		}
		eventBus.publish(DomainEvent.of(IntegrationEvents.USER_DELETED, id.toString(),
				Map.of("userId", id.toString())));
		avatarStorage.remove(user.getAvatar());
	}
}
