package ru.obninsk.iate.easycipher.components;

import org.jetbrains.annotations.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.*;

public class NotificationPanel extends JPanel {
    private final String message;
    private Timer timer;
    private Instant animationBeginningTime;
    private double currentAnimationPosition = 0;
    private int targetAnimationPosition = 35;

    public NotificationPanel(@NotNull String message) {
        this.message = message;
        render();
        animate();
    }

    private void render() {
        setBackground(new Color(206, 83, 127));
        var messageTextPane = new JTextPane();
        messageTextPane.setText(message);
        messageTextPane.setBackground(null);
        messageTextPane.setForeground(new Color(255, 255, 255));
        add(messageTextPane);
    }

    private void animate() {
        setPreferredSize(new Dimension(-1,100));
        setMinimumSize(new Dimension(-1,0));
        revalidate();

        animationBeginningTime = Instant.now();
        timer = new Timer(1, (ActionEvent event) -> {
            Duration currentAnimationTime = Duration.between(animationBeginningTime, Instant.now());
            var currentSeconds = currentAnimationTime.getSeconds();

            if (currentSeconds > 2) {
                if (targetAnimationPosition == 0) timer.stop();
                animationBeginningTime = Instant.now();
                targetAnimationPosition = 0;
            }

            currentAnimationPosition += (targetAnimationPosition - currentAnimationPosition) *
                    (1 - Math.pow(Math.E, -currentAnimationTime.getNano() * 0.0000000001));

            setPreferredSize(new Dimension(-1, (int) currentAnimationPosition));
            revalidate();
        });
        timer.start();
    }
}
