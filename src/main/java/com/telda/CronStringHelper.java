package com.telda;

import java.util.*;

class CronStringHelper {
    final static long MINUTE = 1000 * 60;

    public static long getMillisToNextCron(Date now, String cron) {
        String[] cronParts = cron.split(" ");
        if (cronParts.length != 5) {
            throw new IllegalArgumentException("Cron expressions should contain 5 parts");
        }

        int[] minutes = getAvailableValues(cronParts[0], 0, 59);
        int[] hours = getAvailableValues(cronParts[1], 0, 23);
        int[] dayOfMonth = getAvailableValues(cronParts[2], 1, 31);
        int[] month = getAvailableValues(cronParts[3], 1, 12);
        int[] dayOfWeek = getAvailableValues(cronParts[4], 0, 6);

        Calendar calendar = Calendar.getInstance();
        long minuteMod = now.getTime() % MINUTE;
        if (minuteMod == 0) {
            calendar.setTime(now);
        }
        else {
            calendar.setTime(new Date(now.getTime() + (MINUTE - minuteMod)));
        }


    }

    private static int[] getAvailableValues(String cronPart, int lowerLimit, int upperLimit) {
        if (cronPart.equals("*")) {
            return new int[0];
        }
        Set<Integer> values = new TreeSet<>();

        String[] separatedExpr = cronPart.split(",");
        for (String expr: separatedExpr) {
            if (expr.matches("^\\d+$")) {
                int value = Integer.parseInt(expr);
                validateInteger(value, lowerLimit, upperLimit);
                values.add(value);
            }
            else if (expr.matches("^\\d+-\\d+$")){
                String[] rangeParts = expr.split("-");
                int lowerValue = Integer.parseInt(rangeParts[0]);
                int upperValue = Integer.parseInt(rangeParts[1]);

                if (lowerValue > upperValue) {
                    throw new IllegalArgumentException(String.format("The lower value should come first in the range expression, %s is invalid", expr));
                }

                validateInteger(lowerLimit, lowerLimit, upperLimit);
                validateInteger(upperValue, lowerLimit, upperLimit);

                for (int i = lowerValue; i <= upperValue; i++) {
                    values.add(i);
                }
            } else if (expr.matches("^\\*/\\d+$")) {
                String[] stepParts = expr.split("/");
                int value = Integer.parseInt(stepParts[1]);
                validateInteger(value, lowerLimit, upperLimit);
                for (int i = lowerLimit; i <= upperLimit; i+= value) {
                    values.add(i);
                }
            }
            else {
                throw new IllegalArgumentException(String.format("Invalid cron part format %s", expr));
            }
        }

        return values.stream().mapToInt(i -> i).toArray();
    }

    private static void validateInteger(int x, int lowerLimit, int upperLimit) {
        if (x > upperLimit || x < lowerLimit) {
            throw new IllegalArgumentException(String.format("The value %d is not in range %d-%d", x, lowerLimit, upperLimit));
        }
    }
}
