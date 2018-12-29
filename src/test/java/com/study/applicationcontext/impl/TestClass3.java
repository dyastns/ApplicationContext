package com.study.applicationcontext.impl;

import java.util.Objects;

public class TestClass3 {
    private TestClass1 refField31;

    public TestClass3() {
    }

    public TestClass1 getRefField31() {
        return refField31;
    }

    public void setRefField31(TestClass1 refField31) {
        this.refField31 = refField31;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestClass3 that = (TestClass3) o;
        return Objects.equals(refField31, that.refField31);
    }

    @Override
    public int hashCode() {

        return Objects.hash(refField31);
    }
}
