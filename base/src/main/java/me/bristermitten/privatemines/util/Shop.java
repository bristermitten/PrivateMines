package me.bristermitten.privatemines.util;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Shop {

    private String name;
    private List<ItemStack> itemStacks;
    private Map<ItemStack, Double> blockPrices;
    private double tax;

    public Shop(UUID id, List<ItemStack> itemStacks, Double tax) {
        this.name = id.toString();
        this.itemStacks = itemStacks;
        this.blockPrices = new HashMap<>();
        this.tax = tax;
    }

    public void setShopName(String shopName) {
        this.name = shopName;
    }

    public void setItems(List<ItemStack> items) {
        this.itemStacks = items;
    }

    public void setBlockPrices(Map<ItemStack, Double> map) {
        this.blockPrices = map;
    }

    public String getName() {
        return name;
    }

    public double getTax() {
        return tax;
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    public Map<ItemStack, Double> getBlockPrices() {
        return blockPrices;
    }

    public int getItemCount() {
        return itemStacks.size();
    }
}

