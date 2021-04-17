package me.bristermitten.privatemines.config;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

public enum LangKeys implements MessageKeyProvider {

    /*
    This class manages all the messages from within the plugin
     */

    ERR_PLAYER_HAS_MINE,
    ERR_PLAYER_HAS_NO_MINE,
    ERR_SENDER_HAS_NO_MINE,
    ERR_NO_DEFAULT_SCHEMATIC,
    ERR_PLAYER_ALREADY_BANNED,
    ERR_PLAYER_NOT_BANNED,

    ERR_NO_TRUSTED_PLAYERS,
    ERR_PLAYER_NOT_ON_TRUSTED_LIST,

    ERR_YOU_WERE_BANNED,
    ERR_YOU_WERE_UNBANNED,
    ERR_TAX_DISABLED,
    ERR_YOU_WERE_REMOVED,

    ERR_NO_ACCESS_BLOCK,

    ERR_MINE_UPGRADE_ERROR,
    ERR_MINE_UPGRADE_ERROR_INVALID_SCHEMATIC,
    ERR_MINE_UPGRADE_ERROR_INVALID_TIER,

    ERR_PLAYER_ALREADY_ADDED,
    ERR_CAN_NOT_REMOVE_FROM_OWN_MINE,

    INFO_PLAYER_ADDED,
    INFO_PLAYER_REMOVED,

    INFO_PLAYER_BANNED,
    INFO_PLAYER_UNBANNED,
    INFO_TAX_SET,
    INFO_TAX_INFO,
    INFO_TAX_TAKEN,
    INFO_MINE_GIVEN,
    INFO_MINE_OPENED,
    INFO_MINE_CLOSED,

    INFO_MINE_RESET,
    INFO_MINE_RESET_OTHER,
    INFO_NO_PLAYER;

    private final MessageKey key = MessageKey.of("privatemines." + name().toLowerCase());

    @Override
    public MessageKey getMessageKey() {
        return key;
    }
}
