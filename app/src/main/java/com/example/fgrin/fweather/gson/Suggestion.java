package com.example.fgrin.fweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    public class Comfort{
        @SerializedName("txt")
        public String info;
    }

}
