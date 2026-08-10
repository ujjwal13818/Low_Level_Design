package com.lld.solid.isp.bad;

public class Designer implements Worker {

    @Override
    public void code() {
        System.out.println("Designer does not code."); //this method is not applicable to designer but
        //still has to implement;
    }

    @Override
    public void attendMeetings() {
        System.out.println("Designer is attending meetings.");
    }

    @Override
    public void designUI() {
        System.out.println("Designer is designing UI.");
    }

}

