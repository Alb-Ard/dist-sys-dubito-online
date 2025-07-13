package org.abianchi.dubito.app.gameSession.views;

import java.awt.Component;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.*;

import org.abianchi.dubito.app.gameSession.models.Card;
import org.abianchi.dubito.app.gameSession.models.Player;
import org.albard.utils.Debouncer;

public final class GameBoardPlayerPanel extends JPanel {
    // Use a reentrant lock, so that when a method tries to re-lock from the same
    // thread, we don't have a deadlock
    private final Lock refreshLock = new ReentrantLock();
    private final Debouncer refreshDebouncer = new Debouncer(Duration.ofMillis(150));
    private final GameBoardPlayerActionsPanel actionsPanel;
    private final JPanel playerCardPanel = new JPanel();
    private final Optional<Rotation> rotation;

    private boolean isActive = false;

    public GameBoardPlayerPanel(final Player player, final Optional<Rotation> rotation,
            final Consumer<List<Card>> playCardsListener, final Runnable callLiarListener, final String playerName) {
        this.rotation = rotation;
        this.playerCardPanel.setLayout(
                new BoxLayout(playerCardPanel, rotation.map(x -> BoxLayout.PAGE_AXIS).orElse(BoxLayout.LINE_AXIS)));
        this.actionsPanel = new GameBoardPlayerActionsPanel(player.getLives(), rotation.isPresent(), playerName,
                () -> playCardsListener.accept(this.getSelectedCards()), callLiarListener);
        this.add(this.playerCardPanel);
        this.add(this.actionsPanel);
        this.actionsPanel.setActive(this.isActive);
        this.updateCards(player.getHand());
    }

    // Use suppliers since the runnable invoked in the debouncer always gets the
    // most up-to-date models instead of the ones that were captured when the method
    // was called
    public void refresh(final Supplier<Player> playerSupplier, final Supplier<Boolean> isActiveSupplier,
                        final boolean callLiarEnabled) {
        this.refreshDebouncer.debounce(() -> this.runLocked(() -> {
            this.setActive(isActiveSupplier.get(), callLiarEnabled);
            final Player player = playerSupplier.get();
            this.actionsPanel.setLivesCount(player.getLives());
            this.updateCards(player.getHand());
            this.revalidate();
            this.repaint();
        }));
    }

    private void updateCards(final List<Card> hand) {
        // Don't update if all the cards match
        if (hand.equals(this.getCardViews().stream().map(CardView::getCard).toList())) {
            return;
        }
        this.playerCardPanel.removeAll();
        for (final Card card : hand) {
            final CardView buttonCard = creteCardView(card, this.rotation, this.isActive);
            this.playerCardPanel.add(buttonCard);
        }
    }

    private void setActive(final boolean isActive, final boolean callLiarEnabled) {
        this.runLocked(() -> {
            // Don't update if the state hasn't changed
            if (this.isActive == isActive) {
                return;
            }
            this.isActive = isActive;
            for (final Component component : this.playerCardPanel.getComponents()) {
                if (component instanceof CardView cardView) {
                    cardView.setCardVisibility(isActive);
                    cardView.setEnabled(isActive);
                    cardView.repaint();
                }
            }
            this.actionsPanel.setActive(isActive);
            JLabel livesLabel = (JLabel)this.actionsPanel.getComponent(0);
            if(!livesLabel.getText().contains("0")) {
                this.actionsPanel.getComponent(3).setEnabled(callLiarEnabled);
            }
        });
    }

    private List<CardView> getCardViews() {
        return Arrays.stream(this.playerCardPanel.getComponents())
                .flatMap(x -> x instanceof CardView cardView ? Stream.of(cardView) : Stream.empty()).toList();
    }

    private List<Card> getSelectedCards() {
        return this.getCardViews().stream().filter(AbstractButton::isSelected).map(CardView::getCard).toList();
    }

    /** method used to add buttons for all the cards */
    private static CardView creteCardView(final Card card, final Optional<Rotation> rotation, final boolean isActive) {
        final CardView cardView = new CardView(card);
        cardView.setRotation(rotation);
        cardView.setCardVisibility(isActive);
        cardView.setEnabled(isActive);
        return cardView;
    }

    private void runLocked(final Runnable action) {
        if (this.refreshLock.tryLock()) {
            try {
                action.run();
            } finally {
                this.refreshLock.unlock();
            }
        }
    }
}
