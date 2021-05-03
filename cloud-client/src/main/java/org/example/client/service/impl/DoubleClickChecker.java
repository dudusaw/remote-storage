package org.example.client.service.impl;

public class DoubleClickChecker {
    private long trackTime;
    private boolean checkedOneTime;
    private String lastClickedItem;
    private final int delay;

    public DoubleClickChecker(int delayMillis) {
        this.delay = delayMillis;
        lastClickedItem = "";
    }

    /**
     * @return true if it was the second click on the same item
     */
    public boolean check(String item) {
        boolean result = false;
        if (!lastClickedItem.equals(item)) {
            checkedOneTime = false;
        }
        if (!checkedOneTime) {
            checkedOneTime = true;
        } else if (System.currentTimeMillis() - trackTime < delay) {
            checkedOneTime = false;
            result = true;
        }
        trackTime = System.currentTimeMillis();
        lastClickedItem = item;
        return result;
    }
}
