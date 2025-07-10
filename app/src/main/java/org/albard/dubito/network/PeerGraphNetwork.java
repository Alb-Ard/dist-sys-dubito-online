package org.albard.dubito.network;

import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.albard.dubito.messaging.MessengerFactory;
import org.albard.utils.Logger;
import org.albard.utils.ObservableHashMap;
import org.albard.utils.ObservableMap;
import org.albard.utils.ObservableMapListener;
import org.albard.dubito.messaging.messages.ConnectionRouteMessage;
import org.albard.dubito.messaging.messages.GameMessage;
import org.albard.dubito.messaging.messages.RouteRemovedMessage;

public final class PeerGraphNetwork {
    /**
     * An object that manages each connected peer's server end point
     */
    private static final class PeerGraph {
        private final ObservableMap<PeerId, PeerEndPoint> peerServerEndPoints = new ObservableHashMap<>();

        public boolean addPeer(final PeerId peerId, final PeerEndPoint endPoint) {
            return this.peerServerEndPoints.putIfAbsent(peerId, endPoint) == null;
        }

        public boolean removePeer(final PeerId peerId) {
            return this.peerServerEndPoints.remove(peerId) != null;
        }

        public Map<PeerId, PeerEndPoint> getPeers() {
            return Map.copyOf(this.peerServerEndPoints);
        }

        public void addPeersChangedListener(final ObservableMapListener<PeerId, PeerEndPoint> listener) {
            this.peerServerEndPoints.addListener(listener);
        }
    }

    private PeerGraphNetwork() {
    }

    public static PeerNetwork createBound(final PeerId localPeerId, final String bindAddress, final int bindPort,
            final MessengerFactory messengerFactory) throws IOException {
        return of(PeerNetwork.createBound(localPeerId, bindAddress, bindPort, messengerFactory));
    }

    public static PeerNetwork of(final PeerNetwork network) throws IOException {
        final PeerGraph graph = new PeerGraph();
        graph.addPeersChangedListener(new ObservableMapListener<PeerId, PeerEndPoint>() {
            @Override
            public void entryAdded(PeerId id, PeerEndPoint remoteEndPoint) {
                Logger.logInfo(network.getLocalPeerId() + ": My graph now has " + graph.getPeers().size() + " peers");
                if (!network.getPeers().containsKey(id)) {
                    Logger.logInfo(network.getLocalPeerId() + ": New peer " + id
                            + " added to graph, connecting to it via " + remoteEndPoint);
                    // If this is a NEW peer (received from the network via ConnectionRouteMessage)
                    // connect to it
                    network.connectToPeer(remoteEndPoint);
                }
                // Then, in any case, notify my network of this peer and the new peer of my
                // network graph.
                // sendPeerRouteToNetwork(network, id, remoteEndPoint);
                sendPeerGraphToPeer(network, graph, id);
            }

            @Override
            public void entryRemoved(PeerId id, PeerEndPoint remoteEndPoint) {
                network.disconnectFromPeer(id);
            }

        });
        network.addPeerConnectedListener((id, connection, remoteEndPoint) -> {
            try {
                // We a new peer finished connecting, add it to the graph
                Logger.logInfo(network.getLocalPeerId() + ": I'm connected to " + network.getPeers().size() + " peers");
                graph.addPeer(id, remoteEndPoint);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });
        network.addPeerDisconnectedListener(id -> {
            Logger.logInfo(network.getLocalPeerId() + ": I'm connected to " + network.getPeers().size() + " peers");
            graph.removePeer(id);
        });
        network.addMessageListener(x -> handleMessage(network, graph, x));
        return network;
    }

    private static void sendPeerGraphToPeer(final PeerNetwork network, final PeerGraph graph, final PeerId id) {
        final List<Entry<PeerId, PeerEndPoint>> peers = Set.copyOf(graph.getPeers().entrySet()).stream()
                .filter(x -> !x.getKey().equals(id)).filter(x -> !x.getKey().equals(network.getLocalPeerId())).toList();
        Logger.logInfo(network.getLocalPeerId() + ": Sending peers " + peers.stream().map(x -> x.getKey().id()).toList()
                + " to peer " + id);
        peers.forEach(x -> {
            Logger.logInfo(network.getLocalPeerId() + ": Sending peer " + x.getKey() + " at " + x.getValue()
                    + " to peer " + id);
            network.sendMessage(
                    new ConnectionRouteMessage(network.getLocalPeerId(), Set.of(id), x.getKey(), x.getValue()));
        });
    }

    private static boolean handleMessage(final PeerNetwork network, final PeerGraph graph, final GameMessage message) {
        if (message instanceof ConnectionRouteMessage connectionRouteMessage) {
            // When another peer notifies me of a new connection, add it to the graph.
            Logger.logInfo(network.getLocalPeerId() + ": Received route to " + connectionRouteMessage.getRoutePeerId()
                    + " via endpoint " + connectionRouteMessage.getRouteEndPoint());
            graph.addPeer(connectionRouteMessage.getRoutePeerId(), connectionRouteMessage.getRouteEndPoint());
        }
        if (message instanceof RouteRemovedMessage routeRemovedMessage) {
            Logger.logInfo(network.getLocalPeerId() + ": Received removed route to " + routeRemovedMessage.getSender());
            graph.removePeer(routeRemovedMessage.getSender());
        }
        return false;
    }
}
