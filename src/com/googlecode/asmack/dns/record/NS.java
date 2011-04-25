package com.googlecode.asmack.dns.record;

import com.googlecode.asmack.dns.Record.TYPE;

public class NS extends CNAME {

    @Override
    public TYPE getType() {
        return TYPE.NS;
    }

}
