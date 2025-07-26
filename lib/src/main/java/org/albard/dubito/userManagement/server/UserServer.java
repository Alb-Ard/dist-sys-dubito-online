package org.albard.dubito.userManagement.server;

import java.util.Set;

import org.albard.dubito.connection.PeerConnection;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.network.PeerEndPoint;
import org.albard.dubito.network.PeerId;
import org.albard.dubito.network.PeerNetwork;
import org.albard.dubito.userManagement.User;
import org.albard.dubito.userManagement.messages.UserListUpdatedMessage;
import org.albard.utils.Logger;
import org.albard.dubito.userManagement.messages.UpdateUserMessage;

public final class UserServer {
    private final PeerNetwork network;
    private final UserService userService;

    public UserServer(final PeerNetwork network, final UserService userService) {
        this.network = network;
        this.userService = userService;
        this.network.addMessageListener(this::handleMessage);
        this.network.addPeerConnectedListener(this::handlePeerConnected);
        this.network.addPeerDisconnectedListener(this::handlePeerDisconnected);
        this.userService.addPeerAddedListener(u -> this.sendUserListTo(null));
        this.userService.addPeerUpdatedListener(u -> this.sendUserListTo(null));
        this.userService.addPeerRemovedListener(u -> this.sendUserListTo(null));
    }

    public int getUserCount() {
        return this.userService.getUserCount();
    }

    public Set<User> getUsers() {
        return Set.copyOf(this.userService.getUsers());
    }

    private void handlePeerConnected(final PeerId id, final PeerConnection connection,
            final PeerEndPoint remoteEndPoint) {
        this.userService.addUser(new User(id, id.id()));
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
    }

    private void sendUserListTo(final Set<PeerId> receipients) {
        final Set<User> users = this.userService.getUsers();
        Logger.logInfo(this.network.getLocalPeerId() + ": Sending " + users.size() + " user to "
                + (receipients == null ? "ALL" : Integer.toString(receipients.size())) + " peers");
        this.network.sendMessage(new UserListUpdatedMessage(this.network.getLocalPeerId(), receipients, users));
    }
}
