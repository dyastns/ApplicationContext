package com.study.applicationcontext.impl;

import java.util.Objects;

public class TestClass1 {
    private String field11;
    private int field12;
    private long field13;
    private double field14;
    private boolean field15;

    public TestClass1() {
    }

    public String getField11() {
        return field11;
    }

    public void setField11(String field11) {
        this.field11 = field11;
    }

    public int getField12() {
        return field12;
    }

    public void setField12(int field12) {
        this.field12 = field12;
    }

    public long getField13() {
        return field13;
    }

    public void setField13(long field13) {
        this.field13 = field13;
    }

    public double getField14() {
        return field14;
    }

    public void setField14(double field14) {
        this.field14 = field14;
    }

    public boolean getField15() {
        return field15;
    }

    public void setField15(boolean field15) {
        this.field15 = field15;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestClass1 that = (TestClass1) o;
        return field12 == that.field12 &&
                field13 == that.field13 &&
                Double.compare(that.field14, field14) == 0 &&
                field15 == that.field15 &&
                Objects.equals(field11, that.field11);
    }

    @Override
    public int hashCode() {

        return Objects.hash(field11, field12, field13, field14, field15);
    }
}
