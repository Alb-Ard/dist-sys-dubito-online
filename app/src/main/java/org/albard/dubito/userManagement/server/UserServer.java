package org.albard.dubito.userManagement.server;

import java.util.Set;

import org.albard.dubito.connection.PeerConnection;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.User;
import org.albard.dubito.userManagement.UserService;
import org.albard.dubito.userManagement.messages.UserListUpdatedMessage;
import org.albard.dubito.userManagement.messages.UpdateUserMessage;

public final class UserServer {
    private final PeerNetwork network;
    private final UserService userService;

    public UserServer(final PeerNetwork network, final UserService userService) {
        this.network = network;
        this.userService = userService;
        this.network.addMessageListener(this::handleMessage);
        this.network.setPeerConnectedListener(this::handlePeerConnected);
        this.network.setPeerDisconnectedListener(this::handlePeerDisconnected);
    }

    public int getUserCount() {
        return this.userService.getUserCount();
    }

    public Set<User> getUsers() {
        return Set.copyOf(this.userService.getUsers());
    }

    private void handlePeerConnected(final PeerId id, final PeerConnection connection) {
        this.userService.addUser(new User(id, id.id()));
        this.sendUserListTo(Set.of(id));
    }

    private void handlePeerDisconnected(final PeerId id) {
        this.userService.removeUser(id);
    }

    private boolean handleMessage(final GameMessage message) {
        if (message instanceof UpdateUserMessage updateUserMessage) {
            this.updateUser(updateUserMessage.getSender(), updateUserMessage.getUserName());
            return true;
        }
        return false;
    }

    private void updateUser(final PeerId peerId, final String userName) {
        this.userService.updatePeer(peerId, i -> i.changeName(userName));
        this.sendUserListTo(null);
    }

    private void sendUserListTo(final Set<PeerId> receipients) {
        this.network.sendMessage(
                new UserListUpdatedMessage(this.network.getLocalPeerId(), receipients, this.userService.getUsers()));
    }
}
