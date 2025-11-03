package com.example.inktouch.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderNumberGenerator {
    
    public static String generate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String datePart = dateFormat.format(new Date());
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        String timePart = timeFormat.format(new Date());
        
        return "INK-" + datePart + "-" + timePart;
    }
}
