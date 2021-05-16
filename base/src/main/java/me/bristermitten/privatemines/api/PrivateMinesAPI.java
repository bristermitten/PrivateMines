package me.bristermitten.privatemines.api;

import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused") //Most of these methods are called from external programs
public class PrivateMinesAPI {

    private final MineStorage storage;

    /**
     * @param storage - The storage for the plugin
     */

    public PrivateMinesAPI(MineStorage storage) {
        this.storage = storage;
    }

    public String formatTime(int time) {
        int hours = time / 10000;
        int minutes = (time % 10000) / 100;
        int seconds = time % 100;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * @param player - The player of who you want to check has a mine or not
     * @return boolean
     */

    public boolean hasMine(Player player) {
        return storage.hasMine(player);
    }

    /**
     * @param player - The player who you want to check the tax of
     * @return double
     */

    public double getMineTaxPercentage(Player player) {
        return Objects.requireNonNull(storage.get(player)).getTaxPercentage();
    }

    /**
     * @param player     - The player who's tax you want to set
     * @param percentage - The percentage you want to set the mine tax to
     */

    public void setMineTaxPercentage(Player player, Double percentage) {
        Objects.requireNonNull(storage.get(player)).setTaxPercentage(percentage);
    }

    /**
     * @param player     - The player who's mine reset percentage you want to check
     * @param percentage - The percentage of which the mine should reset at
     */

    public void setMineResetPercentage(Player player, Double percentage) {
        Objects.requireNonNull(storage.get(player)).setResetPercentage(percentage);
    }

    /**
     * @param player     - The player who's mine reset percentage you want to increase the reset percentage of
     * @param percentage - Increase the percentage of which the mine should reset at
     */

    public void increaseMineResetPercentage(Player player, Double percentage) {
        Objects.requireNonNull(storage.get(player)).increaseResetPercentage(percentage);
    }

    /**
     * @param player     - The player who's mine reset percentage you want to increase the reset percentage of
     * @param percentage - Decrease the percentage of which the mine should reset at
     */

    public void decreaseMineResetPercentage(Player player, Double percentage) {
        Objects.requireNonNull(storage.get(player)).decreaseResetPercentage(percentage);
    }


    public double getResetPercentage(Player player) {
        if (storage.hasMine(player) && Objects.requireNonNull(storage.get(player)).getResetPercentage() != 0) {
            return Objects.requireNonNull(storage.get(player)).getResetPercentage();
        } else {
            return 0;
        }
    }

    /**
     * @param player - The player who's mine blocks you want to check
     * @return This will return the blocks in the mine
     */

    public List<ItemStack> getMineBlocks(Player player) {
        return Objects.requireNonNull(storage.get(player)).getMineBlocks();
    }

    /**
     * @param player - The player who you want to check the mine blocks of.
     * @return String list of blocks.
     */

    public List<String> getMineBlocksString(Player player) {
        return Objects.requireNonNull(storage.get(player)).getMineBlocksString();
    }

    /**
     * @param player - The player who you want to set the mine blocks of
     * @param items  - The ItemStack list you want to set the mine blocks to.
     */

    public void setMineBlocks(Player player, List<ItemStack> items) {
        Objects.requireNonNull(storage.get(player)).setMineBlocks(items);
    }

    public void addBlocks(Player player, ItemStack itemStack) {
        Objects.requireNonNull(storage.get(player)).addMineBlock(itemStack);
    }

    public void removeBlocks(Player player, ItemStack itemStack) {
        Objects.requireNonNull(storage.get(player)).removeMineBlock(itemStack);
    }

    /**
     * @param player - The player you want to upgrade their private mine
     */

    public void upgradePlayerMine(Player player) {
        if (storage.hasMine(player)) {
            Objects.requireNonNull(storage.get(player)).upgradeMine(player);
        } else {
            Bukkit.getLogger().warning(player.getName() + " Doesn't have a Private Mine, cannot execute this method!");
        }
    }
}
