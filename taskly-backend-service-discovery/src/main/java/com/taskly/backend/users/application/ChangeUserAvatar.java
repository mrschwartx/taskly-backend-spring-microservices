package com.taskly.backend.users.application;

import com.taskly.backend.shared.domain.EntityIds;
import com.taskly.backend.shared.domain.NotFoundException;
import com.taskly.backend.shared.domain.ValidationException;
import com.taskly.backend.users.domain.AvatarImageType;
import com.taskly.backend.users.domain.User;
import com.taskly.backend.users.domain.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChangeUserAvatar {

	private final UserRepository userRepository;

	private final AvatarStorage avatarStorage;

	public ChangeUserAvatar(UserRepository userRepository, AvatarStorage avatarStorage) {
		this.userRepository = userRepository;
		this.avatarStorage = avatarStorage;
	}

	public UserDto execute(UUID requesterId, String userId, AvatarUpload upload) {
		OwnAccount.assertOwned(requesterId, userId, "You can only upload an avatar for your own account");
		UUID id = EntityIds.parse(userId, "user");

		if (upload == null) {
			throw new ValidationException("No file uploaded");
		}
		AvatarImageType type = AvatarImageType.fromContentType(upload.contentType())
				.orElseThrow(() -> new ValidationException("Only JPEG, PNG, WEBP and GIF images are allowed"));

		User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found!"));

		String storedPath = avatarStorage.store(type, upload.content());
		try {
			String previousAvatar = user.getAvatar();
			user.changeAvatar(storedPath);

			if (!userRepository.update(user)) {
				throw new NotFoundException("User not found!");
			}

			avatarStorage.remove(previousAvatar);
			return UserDto.from(user);
		}
		catch (RuntimeException error) {
			avatarStorage.remove(storedPath);
			throw error;
		}
	}
}
