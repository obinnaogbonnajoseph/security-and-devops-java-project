package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JacksonTester<CreateUserRequest> json;

    @Before
    public void setup() {
        User user = getUser();
        when(cartRepository.save(any())).thenReturn(getCart());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(anyString())).thenReturn(user);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(user.getPassword());
        when(userRepository.save(any())).thenReturn(user);
    }

    @Test
    @WithMockUser
    public void verify_find_by_id() throws Exception {
        mockMvc.perform(
                get(new URI("/api/user/id/1"))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(Long.valueOf(getUser().getId()).intValue())));
    }

    @Test
    @WithMockUser
    public void verify_find_by_username() throws Exception {
        mockMvc.perform(
                        get(new URI("/api/user/obi"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("username", is(getUser().getUsername())));
    }

    @Test
    public void create_user_bad_request() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(getUser().getUsername());
        request.setPassword("test");
        request.setConfirmPassword("test");

        mockMvc.perform(
                        post(new URI("/api/user/create"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json.write(request).getJson())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void verify_create() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(getUser().getUsername());
        request.setPassword(getUser().getPassword());
        request.setConfirmPassword(getUser().getPassword());

        mockMvc.perform(
                post(new URI("/api/user/create"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.write(request).getJson())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("username", is(getUser().getUsername())));
    }

    private User getUser() {
        User user = new User();
        user.setUsername("Obi");
        user.setPassword("test_user_123");
        user.setId(1L);
        return user;
    }

    private Cart getCart() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItems(new ArrayList<>());
        return cart;
    }
}
