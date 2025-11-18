package Util;

import javafx.animation.PauseTransition;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class HoverPopup {

    public static void attachHoverPopup(
            Pane hoverButton,      // the button/pane you hover on
            Pane popupPane,        // the popup that appears
            Duration delay         // hide delay
    ) {

        PauseTransition hideDelay = new PauseTransition(delay);

        // ========== SHOW popup on hover ==========
        hoverButton.setOnMouseEntered(e -> {
            hideDelay.stop();
            popupPane.setVisible(true);
            popupPane.setOpacity(1);
        });

        popupPane.setOnMouseEntered(e -> hideDelay.stop());

        // ========== HIDE popup with delay ==========
        popupPane.setOnMouseExited(e -> {
            hideDelay.setOnFinished(ev -> {
                if (!hoverButton.isHover() && !popupPane.isHover()) {
                    popupPane.setVisible(false);
                    popupPane.setOpacity(0);
                }
            });
            hideDelay.play();
        });

        hoverButton.setOnMouseExited(e -> {
            hideDelay.setOnFinished(ev -> {
                if (!hoverButton.isHover() && !popupPane.isHover()) {
                    popupPane.setVisible(false);
                    popupPane.setOpacity(0);
                }
            });
            hideDelay.play();
        });
    }
}
