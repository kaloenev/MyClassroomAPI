package com.alibou.security.user;

public enum Grade {
    //    трети(3), четвърти(4), пети(5), шести(6), седми(7), осми(8), девети(9), десети(10), единайсти(11), дванайсти(12), студент(13), друго(1)
//    ;
    Third(3), Fourth(4), Fifth(5), Sixth(6), Seventh(7), Eighth(8), Ninth(9), Tenth(10), Eleventh(11), Twelfth(12),
    Student(13), Other(1);

    Grade(int i) {
        number = i;
    }

    private final int number;

    @Override
    public String toString() {
        if (name().equals("Student") || name().equals("Other")) return name();
        return number + "th" + " grade";
    }
}
