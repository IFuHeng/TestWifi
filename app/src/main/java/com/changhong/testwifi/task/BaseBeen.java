package com.changhong.testwifi.task;

import androidx.annotation.Nullable;

import java.util.Objects;

public class BaseBeen<T1, T2> {
    T1 t1;
    T2 t2;

    public BaseBeen(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public T1 getT1() {
        return t1;
    }

    public T2 getT2() {
        return t2;
    }

    @Override
    public String toString() {
        return "BaseBeen{" +
                "t1=" + t1 +
                ", t2=" + t2 +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (obj instanceof BaseBeen) {
            BaseBeen been = (BaseBeen) obj;

            boolean is1 = (t1 != null && t1.equals(been.t1)) || (t1 == null && null == been.t1);
            boolean is2 = (t2 != null && t2.equals(been.t2)) || (t2 == null && null == been.t2);

            return is1 && is2;
        } else
            return super.equals(obj);
    }

    public boolean equals(T1 nt1, T2 nt2) {
//        boolean is1 = (t1 != null && t1.equals(nt1)) || (t1 == null && t1 == nt1);
        boolean is1 = Objects.equals(t1, nt1);
        boolean is2 = t2 == null ? null == nt2 : t2.equals(nt2);
        return is1 && is2;
    }


}