package com.mustupid.pitchgram;

class Settings {
    static final float CENTS_INDICATOR_SIZE = 16;
    static final float PITCHGRAM_PEN_SIZE = 3;
    static final int PITCHGRAM_SCROLL_SPEED = 10;
    static final int PITCHGRAM_MAX_WIDTH = 4090; // >= getWidth()
    static final int NUM_KEYS = 88;
    static final int ANCHOR_MIDI = 21;
    static final int REFERENCE_MIDI = 69;
    static final float REFERENCE_FREQUENCY = 440;
    static final double CENTS_IN_OCTAVE = 1200;
    static final int SAMPLE_RATE = 22050; // >= 4 * highest frequency

    static float threshold1;
    static float threshold2;
    static int lowest;
    static int window;
    static int shift;
    static float smoothness;
}
