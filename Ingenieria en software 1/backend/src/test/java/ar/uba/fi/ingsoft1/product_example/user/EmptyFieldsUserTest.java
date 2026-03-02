package ar.uba.fi.ingsoft1.product_example.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmptyFieldsUserTest {

    @Test
    void whenUserHasEmptyFirstName_shouldShowEmpty() {
        // Tests user with empty firstName
        User user = new User("", "Lastname", 25, "M", "Address", "image", "username", "password", "USER");
        user.setEmail("test@fi.uba.ar");
        UserDTO dto = new UserDTO(user);
        assertEquals("", dto.firstName());
    }

    @Test
    void whenUserHasNullFirstName_shouldShowNull() {
        // Tests user with null firstName
        User user = new User(null, "Lastname", 25, "M", "Address", "image", "username", "password", "USER");
        user.setEmail("test@fi.uba.ar");
        UserDTO dto = new UserDTO(user);
        assertNull(dto.firstName());
    }

    @Test
    void whenUserHasEmptyLastName_shouldShowEmpty() {
        // Tests user with empty lastName
        User user = new User("Name", "", 25, "M", "Address", "image", "username", "password", "USER");
        user.setEmail("test@fi.uba.ar");
        UserDTO dto = new UserDTO(user);
        assertEquals("", dto.lastName());
    }

    @Test
    void whenUserHasEmptyEmail_shouldShowNull() {
        // Tests user with null email
        User user = new User("Name", "Lastname", 25, "M", "Address", "image", "username", "password", "USER");
        user.setEmail(null);
        UserDTO dto = new UserDTO(user);
        assertNull(dto.email());
    }

    @Test
    void whenUserHasEmptyGender_shouldShowEmpty() {
        // Tests user with empty gender
        User user = new User("Name", "Lastname", 25, "", "Address", "image", "username", "password", "USER");
        user.setEmail("test@fi.uba.ar");
        UserDTO dto = new UserDTO(user);
        assertEquals("", dto.gender());
    }

    @Test
    void whenUserHasEmptyAddress_shouldShowEmpty() {
        // Tests user with empty address
        User user = new User("Name", "Lastname", 25, "M", "", "image", "username", "password", "USER");
        user.setEmail("test@fi.uba.ar");
        UserDTO dto = new UserDTO(user);
        assertEquals("", dto.address());
    }

    @Test
    void whenUserHasEmptyBase64Image_shouldShowEmpty() {
        // Tests user with empty base64Image
        User user = new User("Name", "Lastname", 25, "M", "Address", "", "username", "password", "USER");
        user.setEmail("test@fi.uba.ar");
        UserDTO dto = new UserDTO(user);
        assertEquals("", dto.base64Image());
    }

    @Test
    void whenUserHasZeroAge_shouldShowZero() {
        // Tests user with age 0
        User user = new User("Name", "Lastname", 0, "M", "Address", "image", "username", "password", "USER");
        user.setEmail("test@fi.uba.ar");
        UserDTO dto = new UserDTO(user);
        assertEquals(0, dto.age());
    }

    @Test
    void whenUserHasCompleteData_shouldShowAllFields() {
        // Tests user with all fields complete
        User user = new User("Juan", "Pérez", 25, "M", "Street 123", "image_base64", "juanperez", "password", "USER");
        user.setEmail("juan@fi.uba.ar");
        UserDTO dto = new UserDTO(user);
        assertEquals("Juan", dto.firstName());
        assertEquals("Pérez", dto.lastName());
        assertEquals(25, dto.age());
        assertEquals("M", dto.gender());
        assertEquals("Street 123", dto.address());
        assertEquals("image_base64", dto.base64Image());
        assertEquals("juanperez", dto.username());
        assertEquals("juan@fi.uba.ar", dto.email());
    }
}
