package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private ItemRepository itemRepository;

    @Autowired
    private JacksonTester<ModifyCartRequest> json;

    @Before
    public void setup() {
        User user = getUser();
        Item item = getItem();
        Cart cart = getCart();
        cart.setUser(user);
        user.setCart(cart);
        given(userRepository.findByUsername(anyString())).willReturn(user);
        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item));
        given(cartRepository.save(any())).willReturn(cart);
    }

    @Test
    @WithMockUser
    public void verify_add_to_cart_success() throws Exception {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setQuantity(2);
        cartRequest.setItemId(1L);
        cartRequest.setUsername(getUser().getUsername());

        mockMvc.perform(
                post(new URI("/api/cart/addToCart"))
                        .content(json.write(cartRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(getCart().getId().intValue())))
                .andExpect(jsonPath("items.[0].name", is(getItem().getName())))
                .andExpect(jsonPath("user.username", is(getUser().getUsername())));
        verify(cartRepository, times(1)).save(any());
        verify(userRepository, times(1)).findByUsername(getUser().getUsername());
        verify(itemRepository, times(1)).findById(getItem().getId());
    }

    @Test
    @WithMockUser
    public void verify_remove_from_cart_success() throws Exception {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setQuantity(1);
        cartRequest.setItemId(1L);
        cartRequest.setUsername(getUser().getUsername());

        mockMvc.perform(
                        post(new URI("/api/cart/removeFromCart"))
                                .content(json.write(cartRequest).getJson())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(getCart().getId().intValue())))
                .andExpect(jsonPath("user.username", is(getUser().getUsername())));
        verify(cartRepository, times(1)).save(any());
        verify(userRepository, times(1)).findByUsername(getUser().getUsername());
        verify(itemRepository, times(1)).findById(getItem().getId());
    }

    @Test
    public void unauthenticated_request_should_return_forbidden_status() throws Exception {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setQuantity(1);
        cartRequest.setItemId(1L);
        cartRequest.setUsername(getUser().getUsername());

        mockMvc.perform(
                        post(new URI("/api/cart/removeFromCart"))
                                .content(json.write(cartRequest).getJson())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    private User getUser() {
        User user = new User();
        user.setUsername("Obi");
        user.setPassword("test");
        user.setId(1L);
        return user;
    }

    private Item getItem() {
        Item item = new Item();
        item.setId(1L);
        item.setDescription("Test Item");
        item.setName("Test Item");
        item.setPrice(new BigDecimal("1.99"));
        return item;
    }

    private Cart getCart() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItems(new ArrayList<>());
        return cart;
    }
}
