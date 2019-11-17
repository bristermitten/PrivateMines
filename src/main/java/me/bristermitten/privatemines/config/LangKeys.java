package me.bristermitten.privatemines.config;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

public enum LangKeys implements MessageKeyProvider {
    TAX_SET,
    TAX_INFO,
    ERR_PLAYER_HAS_MINE,
    ERR_PLAYER_HAS_NO_MINE,

    INFO_MINE_GIVEN,
    INFO_MINE_OPENED,
    INFO_MINE_CLOSED;

    private final MessageKey key = MessageKey.of("privatemines." + name().toLowerCase());

    @Override
    public MessageKey getMessageKey() {
        return key;
    }
}
