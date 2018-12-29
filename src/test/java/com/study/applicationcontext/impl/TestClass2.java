package com.study.applicationcontext.impl;

import java.util.Objects;

public class TestClass2 {
    private String field21;
    private int field22;
    private TestClass1 refField21;
    private TestClass3 refField22;

    public TestClass2() {
    }

    public String getField21() {
        return field21;
    }

    public void setField21(String field21) {
        this.field21 = field21;
    }

    public int getField22() {
        return field22;
    }

    public void setField22(int field22) {
        this.field22 = field22;
    }

    public TestClass1 getRefField21() {
        return refField21;
    }

    public void setRefField21(TestClass1 refField21) {
        this.refField21 = refField21;
    }

    public TestClass3 getRefField22() {
        return refField22;
    }

    public void setRefField22(TestClass3 refField22) {
        this.refField22 = refField22;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestClass2 that = (TestClass2) o;
        return field22 == that.field22 &&
                Objects.equals(field21, that.field21) &&
                Objects.equals(refField21, that.refField21) &&
                Objects.equals(refField22, that.refField22);
    }

    @Override
    public int hashCode() {

        return Objects.hash(field21, field22, refField21, refField22);
    }
}

