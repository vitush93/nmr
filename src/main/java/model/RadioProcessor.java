package model;


import api.SpinAPI;

public class RadioProcessor {

    private boolean boardConnected;

    public RadioProcessor() {

    }

    public boolean isBoardConnected() {
        return boardConnected;
    }

    public void updateBoardStatus() {
        boardConnected = (SpinAPI.INSTANCE.pb_count_boards() > 0);
    }

}
