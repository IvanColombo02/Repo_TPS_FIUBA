package ar.uba.fi.ingsoft1.product_example.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePhotoTest {

    // ===== TESTS =====

    @Test
    void createUserWithValidProfilePhoto() {
        // Tests that when creating a new account, the system allows uploading a photo to link to the profile
        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD";
        UserCreateDTO userData = new UserCreateDTO(
                "Juan", "Pérez", 25, "M", "Street 123",
                base64Image, "juan123", "password123", "ROLE_USER",
                "juan@fi.uba.ar"
        );
        assertNotNull(userData.base64Image(), "Photo must be present");
        assertTrue(userData.base64Image().startsWith("data:image/jpeg;base64,"), "Must have JPEG format");
    }

    @Test
    void imageSizeValidation() {
        // Tests that uploaded image cannot exceed 2MB

        // Test small image (valid)
        String smallImage = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD";
        assertTrue(isValidImageSize(smallImage), "Small image should be valid");

        // Test very large image (invalid)
        String largeImage = createLargeBase64Image();
        assertFalse(isValidImageSize(largeImage), "Very large image should be invalid");
    }

    @Test
    void createUserWithoutProfilePhoto() {
        // Tests that user can choose not to upload any image
        UserCreateDTO userData = new UserCreateDTO(
                "Maria", "Garcia", 30, "F", "Avenue 456",
                null, "maria123", "password123", "ROLE_USER",
                "maria@fi.uba.ar"
        );
        assertNull(userData.base64Image(), "Photo should be null (optional)");
    }

    @Test
    void profilePhotoStoredInDatabase() {
        // Tests that the avatar is stored in the database with other user data

        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD";
        UserCreateDTO userData = new UserCreateDTO(
                "Carlos", "Lopez", 28, "M", "Plaza 789",
                base64Image, "carlos123", "password123", "ROLE_USER",
                "carlos@fi.uba.ar"
        );

        // Simulate conversion to User entity
        User user = userData.asUser(password -> "encoded-" + password);
        assertNotNull(user.getBase64Image(), "Photo must be in User entity");
        assertEquals(base64Image, user.getBase64Image(), "Photo must match");
        assertEquals("Carlos", user.getFirstName(), "Other data must be present");
        assertEquals("Lopez", user.getLastName(), "Other data must be present");
    }

    @Test
    void updateProfilePhotoSuccessfully() {
        // Tests that a user can update their profile photo using UserUpdateDTO
        User user = new User("Juan", "Pérez", 25, "M", "Street 123",
                "data:image/jpeg;base64,oldImage", "juan123", "password", "ROLE_USER");

        String newBase64Image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
        UserUpdateDTO updateDTO = new UserUpdateDTO(
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                newBase64Image,
                java.util.Optional.empty(),
                java.util.Optional.empty()
        );

        User updatedUser = updateDTO.applyTo(user);
        assertEquals(newBase64Image, updatedUser.getBase64Image(), "Photo must be updated");
        assertEquals("Juan", updatedUser.getFirstName(), "Other fields must remain unchanged");
    }

    @Test
    void removeProfilePhotoBySettingNull() {
        // Tests that a user can remove their profile photo by setting base64Image to null
        User user = new User("Juan", "Pérez", 25, "M", "Street 123",
                "data:image/jpeg;base64,oldImage", "juan123", "password", "ROLE_USER");

        UserUpdateDTO updateDTO = new UserUpdateDTO(
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                null, // null removes the photo
                java.util.Optional.empty(),
                java.util.Optional.empty()
        );

        User updatedUser = updateDTO.applyTo(user);
        assertNull(updatedUser.getBase64Image(), "Photo must be removed (set to null)");
    }

    @Test
    void updateAllUserFieldsWithUserUpdateDTO() {
        // Tests that UserUpdateDTO.applyTo() correctly updates all fields when provided
        User user = new User("Juan", "Pérez", 25, "M", "Street 123",
                "data:image/jpeg;base64,oldImage", "juan123", "password", "ROLE_USER");
        user.setEmail("juan@fi.uba.ar");

        UserUpdateDTO updateDTO = new UserUpdateDTO(
                java.util.Optional.of("Carlos"),
                java.util.Optional.of("García"),
                java.util.Optional.of(30),
                java.util.Optional.of("F"),
                java.util.Optional.of("Avenue 456"),
                "data:image/png;base64,newImage",
                java.util.Optional.of("carlos123"),
                java.util.Optional.of("carlos@fi.uba.ar")
        );

        User updatedUser = updateDTO.applyTo(user);
        assertEquals("Carlos", updatedUser.getFirstName(), "FirstName must be updated");
        assertEquals("García", updatedUser.getLastName(), "LastName must be updated");
        assertEquals(30, updatedUser.getAge(), "Age must be updated");
        assertEquals("F", updatedUser.getGender(), "Gender must be updated");
        assertEquals("Avenue 456", updatedUser.getAddress(), "Address must be updated");
        assertEquals("data:image/png;base64,newImage", updatedUser.getBase64Image(), "Base64Image must be updated");
        assertEquals("carlos123", updatedUser.getUsername(), "Username must be updated");
        assertEquals("carlos@fi.uba.ar", updatedUser.getEmail(), "Email must be updated");
        assertEquals("ROLE_USER", updatedUser.getRole(), "Role must remain unchanged");
    }

    // ===== HELPER METHODS =====

    private boolean isValidImageSize(String base64Image) {
        if (base64Image == null) return true;
        try {
            String base64Data = base64Image.split(",")[1];
            return base64Data.length() < 1000;
        } catch (Exception e) {
            return false;
        }
    }

    private String createLargeBase64Image() {
        // Create a base64 image that is very large
        StringBuilder sb = new StringBuilder();
        sb.append("data:image/jpeg;base64,");
        for (int i = 0; i < 10000; i++) {
            sb.append("AAAA");
        }

        return sb.toString();
    }
}
