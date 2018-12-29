package com.study.applicationcontext.impl;

public class TestBadConstructorClass {
    private String field1;
    private int field2;

    public TestBadConstructorClass(String field1, int field2) {
        this.field1 = field1;
        this.field2 = field2;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public void setField2(int field2) {
        this.field2 = field2;
    }
}
