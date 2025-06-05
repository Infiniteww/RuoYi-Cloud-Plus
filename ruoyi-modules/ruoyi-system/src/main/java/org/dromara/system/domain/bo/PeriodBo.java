package org.dromara.system.domain.bo;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

public class PeriodBo {

    private final int year;
    private final PeriodType type;
    private final int index; // 月份 / 季度 / 半年：从1开始

    public PeriodBo(int year, PeriodType type, int index) {
        this.year = year;
        this.type = type;
        this.index = index;

        validate();
    }

    private void validate() {
        switch (type) {
            case YEAR -> {
                if (index != 1) throw new IllegalArgumentException("年度的 index 必须为 1");
            }
            case HALF_YEAR -> {
                if (index < 1 || index > 2) throw new IllegalArgumentException("上/下半年 index 应为 1 或 2");
            }
            case QUARTER -> {
                if (index < 1 || index > 4) throw new IllegalArgumentException("季度 index 应为 1 到 4");
            }
            case MONTH -> {
                if (index < 1 || index > 12) throw new IllegalArgumentException("月份 index 应为 1 到 12");
            }
        }
    }

    public String getCode() {
        return switch (type) {
            case YEAR -> year + "";
            case HALF_YEAR -> year + "H" + index;
            case QUARTER -> year + "Q" + index;
            case MONTH -> year + "M" + String.format("%02d", index);
        };
    }

    public String getDisplayName() {
        return switch (type) {
            case YEAR -> year + "年度";
            case HALF_YEAR -> year + (index == 1 ? "年上半年" : "年下半年");
            case QUARTER -> year + "年第" + index + "季度";
            case MONTH -> year + "年" + String.format("%02d", index) + "月";
        };
    }

    public enum PeriodType {
        YEAR, HALF_YEAR, QUARTER, MONTH
    }

    // 可选：支持解析 code 创建对象
    public static PeriodBo parseCode(String code) {
        if (code.matches("\\d{4}")) {
            return new PeriodBo(Integer.parseInt(code), PeriodType.YEAR, 1);
        } else if (code.matches("\\d{4}H[12]")) {
            return new PeriodBo(Integer.parseInt(code.substring(0, 4)), PeriodType.HALF_YEAR, Integer.parseInt(code.substring(5)));
        } else if (code.matches("\\d{4}Q[1-4]")) {
            return new PeriodBo(Integer.parseInt(code.substring(0, 4)), PeriodType.QUARTER, Integer.parseInt(code.substring(5)));
        } else if (code.matches("\\d{4}M\\d{2}")) {
            return new PeriodBo(Integer.parseInt(code.substring(0, 4)), PeriodType.MONTH, Integer.parseInt(code.substring(5)));
        } else {
            throw new IllegalArgumentException("无法解析考核周期：" + code);
        }
    }

    // 可选：支持解析 displayName 创建对象
    public static PeriodBo parseDisplayName(String displayName) {
        displayName = displayName.trim();

        // 年度：2025年度
        if (displayName.matches("\\d{4}年度")) {
            int year = Integer.parseInt(displayName.substring(0, 4));
            return new PeriodBo(year, PeriodType.YEAR, 1);
        }

        // 上/下半年：2025年上半年 或 2025年下半年
        if (displayName.matches("\\d{4}年[上下]半年")) {
            int year = Integer.parseInt(displayName.substring(0, 4));
            int index = displayName.contains("上") ? 1 : 2;
            return new PeriodBo(year, PeriodType.HALF_YEAR, index);
        }

        // 季度：2025年第1季度
        if (displayName.matches("\\d{4}年第[1-4]季度")) {
            int year = Integer.parseInt(displayName.substring(0, 4));
            int index = Integer.parseInt(displayName.substring(6, 7)); // 第1季度 -> '1'
            return new PeriodBo(year, PeriodType.QUARTER, index);
        }

        // 月份：2025年12月
        if (displayName.matches("\\d{4}年(0[1-9]|1[0-2]|[1-9])月")) {
            int year = Integer.parseInt(displayName.substring(0, 4));
            String monthStr = displayName.substring(5, displayName.length() - 1); // 去掉“年”和“月”
            int month = Integer.parseInt(monthStr);
            return new PeriodBo(year, PeriodType.MONTH, month);
        }
        throw new IllegalArgumentException("无法从 displayName 解析考核周期：" + displayName);
    }

    public LocalDate getStartDate() {
        return switch (type) {
            case YEAR -> LocalDate.of(year, 1, 1);
            case HALF_YEAR -> LocalDate.of(year, index == 1 ? 1 : 7, 1);
            case QUARTER -> LocalDate.of(year, (index - 1) * 3 + 1, 1);
            case MONTH -> LocalDate.of(year, index, 1);
        };
    }

    public LocalDate getEndDate() {
        return switch (type) {
            case YEAR -> LocalDate.of(year, 12, 31);
            case HALF_YEAR -> LocalDate.of(year, index == 1 ? 6 : 12, Month.of(index == 1 ? 6 : 12).length(java.time.Year.isLeap(year)));
            case QUARTER -> {
                int endMonth = index * 3;
                YearMonth ym = YearMonth.of(year, endMonth);
                yield ym.atEndOfMonth();
            }
            case MONTH -> {
                YearMonth ym = YearMonth.of(year, index);
                yield ym.atEndOfMonth();
            }
        };
    }


    @Override
    public String toString() {
        return "PeriodBo{" +
                "year=" + year +
                ", type=" + type +
                ", index=" + index +
                ", code='" + getCode() + '\'' +
                ", displayName='" + getDisplayName() + '\'' +
                '}';
    }

    public static void main(String[] args) {
//        PeriodBo year = new PeriodBo(2023, PeriodType.YEAR, 1);
//        System.out.println(year.getCode()); // 2023
//        System.out.println(year.getDisplayName()); // 2023年度
//
//        PeriodBo halfYear = new PeriodBo(2023, PeriodType.HALF_YEAR, 1);
//        System.out.println(halfYear.getCode()); // 2023H1
//        System.out.println(halfYear.getDisplayName()); // 2023年上半年
//
//        PeriodBo quarter = new PeriodBo(2023, PeriodType.QUARTER, 2);
//        System.out.println(quarter.getCode()); // 2023Q2
//        System.out.println(quarter.getDisplayName()); // 2023年第2季度
//
//        PeriodBo month = new PeriodBo(2023, PeriodType.MONTH, 5);
//        System.out.println(month.getCode()); // 2023M05
//        System.out.println(month.getDisplayName()); // 2023年05月

        // 测试解析
        PeriodBo periodBo1 = PeriodBo.parseCode("2025M01");
        System.out.println(periodBo1);
        PeriodBo periodBo2 = PeriodBo.parseDisplayName("2025年01月");
        System.out.println(periodBo2);
    }
}
