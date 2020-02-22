package me.bristermitten.privatemines.config;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

public enum LangKeys implements MessageKeyProvider {
    ERR_PLAYER_HAS_MINE,
    ERR_PLAYER_HAS_NO_MINE,
    ERR_SENDER_HAS_NO_MINE,

    INFO_TAX_SET,
    INFO_TAX_INFO,
    INFO_TAX_TAKEN,
    INFO_MINE_GIVEN,
    INFO_MINE_OPENED,
    INFO_MINE_CLOSED;

    private final MessageKey key = MessageKey.of("privatemines." + name().toLowerCase());

    @Override
    public MessageKey getMessageKey() {
        return key;
    }
}
