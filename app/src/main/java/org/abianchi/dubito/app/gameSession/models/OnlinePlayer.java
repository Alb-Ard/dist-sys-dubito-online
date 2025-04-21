package org.abianchi.dubito.app.gameSession.models;

import org.albard.dubito.network.PeerId;

public interface OnlinePlayer extends Player{

    PeerId getOnlineId();
}
