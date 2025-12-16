package com.socialmedia.user.properties;

import com.socialmedia.user.dto.UserProfileRequest;
import com.socialmedia.user.dto.UserProfileResponse;
import com.socialmedia.user.dto.UserSearchResponse;
import com.socialmedia.user.entity.UserProfile;
import com.socialmedia.user.exception.AuthorizationException;
import com.socialmedia.user.repository.UserProfileRepository;
import com.socialmedia.user.service.UserService;
import net.jqwik.api.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@SpringBootTest
@Testcontainers
class UserServicePropertiesTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @BeforeEach
    void setUp() {
        userProfileRepository.deleteAll();
    }

    // Feature: social-media-backend, Property 12: Profile creation stores all fields
    // Validates: Requirements 5.1
    @Property(tries = 100)
    @Label("profileCreationShouldStoreAllFields")
    void profileCreationShouldStoreAllFields(
            @ForAll("validUserId") Long userId,
            @ForAll("validName") String name,
            @ForAll("optionalBio") String bio,
            @ForAll("optionalAvatarUrl") String avatarUrl,
            @ForAll("optionalLocation") String location,
            @ForAll("optionalWebsite") String website) {
        
        // Given a valid profile request with all fields
        UserProfileRequest request = UserProfileRequest.builder()
                .name(name)
                .bio(bio)
                .avatarUrl(avatarUrl)
                .location(location)
                .website(website)
                .build();

        // When creating a profile
        UserProfileResponse response = userService.createProfile(userId, request);

        // Then all fields should be stored
        Assertions.assertThat(response.getUserId()).isEqualTo(userId);
        Assertions.assertThat(response.getName()).isEqualTo(name);
        Assertions.assertThat(response.getBio()).isEqualTo(bio);
        Assertions.assertThat(response.getAvatarUrl()).isEqualTo(avatarUrl);
        Assertions.assertThat(response.getLocation()).isEqualTo(location);
        Assertions.assertThat(response.getWebsite()).isEqualTo(website);
        Assertions.assertThat(response.getCreatedAt()).isNotNull();
        Assertions.assertThat(response.getUpdatedAt()).isNotNull();

        // Verify in database
        UserProfile savedProfile = userProfileRepository.findByUserId(userId).orElseThrow();
        Assertions.assertThat(savedProfile.getName()).isEqualTo(name);
        Assertions.assertThat(savedProfile.getBio()).isEqualTo(bio);
        Assertions.assertThat(savedProfile.getAvatarUrl()).isEqualTo(avatarUrl);
        Assertions.assertThat(savedProfile.getLocation()).isEqualTo(location);
        Assertions.assertThat(savedProfile.getWebsite()).isEqualTo(website);
    }

    // Feature: social-media-backend, Property 14: Profile retrieval round-trip
    // Validates: Requirements 5.3
    @Property(tries = 100)
    @Label("profileRetrievalShouldRoundTrip")
    void profileRetrievalShouldRoundTrip(
            @ForAll("validUserId") Long userId,
            @ForAll("validName") String name,
            @ForAll("optionalBio") String bio,
            @ForAll("optionalAvatarUrl") String avatarUrl,
            @ForAll("optionalLocation") String location,
            @ForAll("optionalWebsite") String website) {
        
        // Given a created profile
        UserProfileRequest request = UserProfileRequest.builder()
                .name(name)
                .bio(bio)
                .avatarUrl(avatarUrl)
                .location(location)
                .website(website)
                .build();
        
        UserProfileResponse createdProfile = userService.createProfile(userId, request);

        // When retrieving the profile by ID
        UserProfileResponse retrievedProfile = userService.getProfileById(createdProfile.getId());

        // Then all fields should match the created profile
        Assertions.assertThat(retrievedProfile.getId()).isEqualTo(createdProfile.getId());
        Assertions.assertThat(retrievedProfile.getUserId()).isEqualTo(createdProfile.getUserId());
        Assertions.assertThat(retrievedProfile.getName()).isEqualTo(createdProfile.getName());
        Assertions.assertThat(retrievedProfile.getBio()).isEqualTo(createdProfile.getBio());
        Assertions.assertThat(retrievedProfile.getAvatarUrl()).isEqualTo(createdProfile.getAvatarUrl());
        Assertions.assertThat(retrievedProfile.getLocation()).isEqualTo(createdProfile.getLocation());
        Assertions.assertThat(retrievedProfile.getWebsite()).isEqualTo(createdProfile.getWebsite());
        Assertions.assertThat(retrievedProfile.getCreatedAt()).isEqualTo(createdProfile.getCreatedAt());
        Assertions.assertThat(retrievedProfile.getUpdatedAt()).isEqualTo(createdProfile.getUpdatedAt());
    }

    // Feature: social-media-backend, Property 15: Cross-user profile update rejection
    // Validates: Requirements 5.4
    @Property(tries = 100)
    @Label("crossUserProfileUpdateShouldBeRejected")
    void crossUserProfileUpdateShouldBeRejected(
            @ForAll("validUserId") Long userIdA,
            @ForAll("validUserId") Long userIdB,
            @ForAll("validName") String name,
            @ForAll("validName") String updatedName) {
        
        Assume.that(!userIdA.equals(userIdB));

        // Given a profile created by user A
        UserProfileRequest request = UserProfileRequest.builder()
                .name(name)
                .build();
        
        UserProfileResponse profileA = userService.createProfile(userIdA, request);

        // When user B attempts to update user A's profile
        UserProfileRequest updateRequest = UserProfileRequest.builder()
                .name(updatedName)
                .build();

        // Then it should throw AuthorizationException
        Assertions.assertThatThrownBy(() -> 
                userService.updateProfile(profileA.getId(), userIdB, updateRequest))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("not authorized");
    }

    // Feature: social-media-backend, Property 18: Pagination consistency
    // Validates: Requirements 6.4
    @Property(tries = 50)
    @Label("paginationShouldBeConsistent")
    void paginationShouldBeConsistent(
            @ForAll("profileList") List<UserProfileRequest> profiles) {
        
        Assume.that(profiles.size() >= 10);

        // Given multiple profiles with searchable names
        for (int i = 0; i < profiles.size(); i++) {
            userService.createProfile((long) (1000 + i), profiles.get(i));
        }

        // When requesting consecutive pages
        int pageSize = 5;
        UserSearchResponse page1 = userService.searchByName("", 0, pageSize);
        UserSearchResponse page2 = userService.searchByName("", 1, pageSize);

        // Then pages should not overlap
        List<Long> page1Ids = page1.getUsers().stream()
                .map(UserProfileResponse::getId)
                .toList();
        
        List<Long> page2Ids = page2.getUsers().stream()
                .map(UserProfileResponse::getId)
                .toList();

        // No IDs should appear in both pages
        for (Long id : page1Ids) {
            Assertions.assertThat(page2Ids).doesNotContain(id);
        }

        // Total elements should be consistent
        Assertions.assertThat(page1.getTotalElements()).isEqualTo(page2.getTotalElements());
        
        // Page numbers should be correct
        Assertions.assertThat(page1.getCurrentPage()).isEqualTo(0);
        Assertions.assertThat(page2.getCurrentPage()).isEqualTo(1);
    }

    // Arbitraries for generating test data

    @Provide
    Arbitrary<Long> validUserId() {
        return Arbitraries.longs().between(1L, 1000000L);
    }

    @Provide
    Arbitrary<String> validName() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ')
                .ofMinLength(1)
                .ofMaxLength(50);
    }

    @Provide
    Arbitrary<String> optionalBio() {
        return Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .withCharRange('A', 'Z')
                        .withChars(' ', '.', ',', '!')
                        .ofMinLength(0)
                        .ofMaxLength(200)
        );
    }

    @Provide
    Arbitrary<String> optionalAvatarUrl() {
        return Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .numeric()
                        .withChars('/', '.', '-', '_')
                        .ofMinLength(10)
                        .ofMaxLength(100)
                        .map(s -> "https://example.com/avatars/" + s + ".jpg")
        );
    }

    @Provide
    Arbitrary<String> optionalLocation() {
        return Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.of("New York", "London", "Tokyo", "Paris", "Berlin", "Sydney", "Toronto", "Mumbai")
        );
    }

    @Provide
    Arbitrary<String> optionalWebsite() {
        return Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .numeric()
                        .ofMinLength(5)
                        .ofMaxLength(20)
                        .map(s -> "https://" + s + ".com")
        );
    }

    @Provide
    Arbitrary<List<UserProfileRequest>> profileList() {
        return Arbitraries.of(
                "Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Henry",
                "Ivy", "Jack", "Kate", "Liam", "Mia", "Noah", "Olivia", "Peter"
        ).list().ofMinSize(10).ofMaxSize(20)
                .map(names -> names.stream()
                        .map(name -> UserProfileRequest.builder()
                                .name(name)
                                .build())
                        .toList());
    }
}
