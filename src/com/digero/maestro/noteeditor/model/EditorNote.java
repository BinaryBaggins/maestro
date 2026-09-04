package com.digero.maestro.noteeditor.model;

public class EditorNote {

    private int midiNote;
    private double startBeat;
    private double durationBeats;

    public EditorNote(int midiNote, double startBeat, double durationBeats) {
        this.midiNote = midiNote;
        this.startBeat = startBeat;
        this.durationBeats = durationBeats;
    }

    public int getMidiNote() {
        return midiNote;
    }

    public void setMidiNote(int midiNote) {
        this.midiNote = midiNote;
    }

    public double getStartBeat() {
        return startBeat;
    }

    public void setStartBeat(double startBeat) {
        this.startBeat = startBeat;
    }

    public double getDurationBeats() {
        return durationBeats;
    }

    public void setDurationBeats(double durationBeats) {
        this.durationBeats = durationBeats;
    }
}
