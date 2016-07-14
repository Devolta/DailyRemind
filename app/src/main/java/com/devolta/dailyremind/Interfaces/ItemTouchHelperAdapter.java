package com.devolta.dailyremind.Interfaces;

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}