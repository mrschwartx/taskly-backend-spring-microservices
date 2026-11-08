package com.taskly.user.application;

import com.taskly.user.shared.domain.EntityIds;
import com.taskly.user.shared.domain.NotFoundException;
import com.taskly.user.shared.domain.ValidationException;
import com.taskly.user.domain.AvatarImageType;
import com.taskly.user.domain.User;
import com.taskly.user.domain.UserRepository;
import java.util.UUID;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Service;

@Service
public class ChangeUserAvatar {

	private final UserRepository userRepository;

	private final AvatarStorage avatarStorage;

	public ChangeUserAvatar(UserRepository userRepository, AvatarStorage avatarStorage) {
		this.userRepository = userRepository;
		this.avatarStorage = avatarStorage;
	}

	@ConcurrencyLimit(limitString = "${taskly.storage.avatar-upload-concurrency:8}")
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
