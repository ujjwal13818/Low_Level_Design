package com.lld.solid.isp.bad;

public class BackEndDev implements Worker {
    @Override
    public void code() {
        System.out.println("Back-end developer is coding.");
    }

    @Override
    public void attendMeetings() {
        System.out.println("Back-end developer is attending meetings.");
    }

    //this method is useless for the class.
    @Override
    public void designUI() {
        // Back-end developers typically don't design UI, so this method might not be applicable.
        throw new UnsupportedOperationException("Back-end developers don't usually design UI.");
    }
}
