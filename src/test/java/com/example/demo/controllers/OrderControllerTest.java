package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderRepository orderRepository;

    @Before
    public void setup() {
        User user = getUser();
        Cart cart = getCart();
        cart.addItem(getItem());
        cart.setUser(user);
        user.setCart(cart);
        when(userRepository.findByUsername(anyString())).thenReturn(user);
        when(orderRepository.save(any()))
                .thenReturn(UserOrder.createFromCart(cart));
        when(orderRepository.findByUser(any()))
                .thenReturn(Collections.singletonList(UserOrder.createFromCart(cart)));
    }

    @Test
    @WithMockUser
    public void verify_submit() throws Exception {
        mockMvc.perform(
                post(new URI("/api/order/submit/obi"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("user.username", is(getUser().getUsername())));
    }

    @Test
    @WithMockUser
    public void verify_get_orders_for_user() throws Exception {
        mockMvc.perform(
                        get(new URI("/api/order/history/obi"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].user.username", is(getUser().getUsername())));
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
