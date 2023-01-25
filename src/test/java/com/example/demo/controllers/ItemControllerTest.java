package com.example.demo.controllers;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRepository itemRepository;

    @Before
    public void setup() {
        List<Item> items = getItems();
        Item item = getItem();
        when(itemRepository.findAll()).thenReturn(items);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.findByName(anyString())).thenReturn(Collections.singletonList(item));
    }

    @Test
    @WithMockUser
    public void verify_get_items() throws Exception {
        mockMvc.perform(get(new URI("/api/item")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].name", is(getItems().get(0).getName())));
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser
    public void verify_get_item_by_id() throws Exception {
        mockMvc.perform(get(new URI("/api/item/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is(getItem().getName())));
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    @WithMockUser
    public void verify_get_items_by_name() throws Exception {
        mockMvc.perform(get(new URI("/api/item/name/test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].name", is(getItem().getName())));
        verify(itemRepository, times(1)).findByName(anyString());
    }

    private Item getItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Round Widget");
        item.setPrice(new BigDecimal("2.99"));
        item.setDescription("A widget that is round");
        return item;
    }

    private List<Item> getItems() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Round Widget");
        item1.setPrice(new BigDecimal("2.99"));
        item1.setDescription("A widget that is round");

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Square Widget");
        item2.setPrice(new BigDecimal("1.99"));
        item2.setDescription("A widget that is square");
        return Arrays.asList(item1, item2);
    }
}
