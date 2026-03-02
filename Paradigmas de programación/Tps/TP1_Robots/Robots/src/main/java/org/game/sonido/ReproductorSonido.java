package org.game.sonido;

import javax.sound.sampled.*;
import java.util.Objects;

/// Librería de reproducción de sonido.

public class ReproductorSonido {
    private static final String RUTA_BACK_SOUND = "/Sounds/backSong.wav";
    private static final String RUTA_LOSS_SOUND = "/Sounds/lossSound.wav";
    private static final String RUTA_WIN_SOUND = "/Sounds/winSound.wav";
    private static final String RUTA_TELEPORT_SOUND = "/Sounds/teleportSound.wav";
    private static final String RUTA_MOV_SOUND = "/Sounds/movSound.wav";
    private static final String RUTA_COLISION_SOUND = "/Sounds/colisionSound.wav";
    private static final String RUTA_DIAMOND_SOUND = "/Sounds/diamondSound.wav";
    private static final String MSG_ERROR_REPRODUCCION = "Error al reproducir sonido: ";
    private static final float VOLUMEN_SONIDO_BACK = -12.0f;
    private static final float VOLUMEN_SONIDO_UNA_VEZ = -5.0f;
    private static final long TIEMPO_ESPERA_SONIDO_MS = 500;

    private static Clip loopClip;

    // Metodo para mantener el sonido de fondo siempre
    public static void reproducirLoop(String rutaWav) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(
                    Objects.requireNonNull(ReproductorSonido.class.getResource(rutaWav)));
            loopClip = AudioSystem.getClip();
            loopClip.open(audio);
            FloatControl control = (FloatControl) loopClip.getControl(FloatControl.Type.MASTER_GAIN);
            control.setValue(VOLUMEN_SONIDO_BACK);
            loopClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println(MSG_ERROR_REPRODUCCION + e.getMessage());
        }
    }

    // Metodo para reproducir una sola vez los sonidos
    public static void reproducirUnaVez(String rutaWav) {
        new Thread(() -> {
            try {
                AudioInputStream audio = AudioSystem.getAudioInputStream(
                        Objects.requireNonNull(ReproductorSonido.class.getResource(rutaWav)));
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(VOLUMEN_SONIDO_UNA_VEZ);
                clip.start();
                Thread.sleep(clip.getMicrosecondLength() / TIEMPO_ESPERA_SONIDO_MS);
                clip.close();
            } catch (Exception e) {
                System.err.println(MSG_ERROR_REPRODUCCION + e.getMessage());
            }
        }).start();
    }

    // Detiene el sonido de fondo
    private static void detenerClip(Clip clip) {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    /*
     *
     * SECCION REPRODUCTORES
     *
     */

    public static void reproducirBackSound() {
        reproducirLoop(RUTA_BACK_SOUND);
    }

    public static void detenerBackSound() {
        detenerClip(loopClip);
    }

    public static void reproducirLossSound() {
        reproducirUnaVez(RUTA_LOSS_SOUND);
    }

    public static void reproducirWinSound() {
        reproducirUnaVez(RUTA_WIN_SOUND);
    }

    public static void reproducirTeleportSound() {
        reproducirUnaVez(RUTA_TELEPORT_SOUND);
    }

    public static void reproducirMovSound() {
        reproducirUnaVez(RUTA_MOV_SOUND);
    }

    public static void reproducirColisionSound() {
        reproducirUnaVez(RUTA_COLISION_SOUND);
    }

    public static void reproducirDiamondSound() {
        reproducirUnaVez(RUTA_DIAMOND_SOUND);
    }

}
