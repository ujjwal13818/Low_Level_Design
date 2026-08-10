package com.lld.solid.isp.bad;

public interface Worker {
    //one interface has a lot of methods which is not applicable to all the implementing classes so, one class implemnting this
    //interface will have to implement those methods which is not relatable.
    // so, it came ISP = Interface segregation Principle

        void code();
        void attendMeetings();
        void designUI();
}
