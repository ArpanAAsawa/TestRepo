/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores
 * CA 94065 USA or visit www.oracle.com if you need additional information or
 * have any questions.
 */
package com.sun.lwuit;

import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.list.DefaultListModel;
import com.sun.lwuit.list.ListModel;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.EventDispatcher;

/**
 * Date widget for selecting a date/time value.
 * To localize strings for month names
 * use the values "Calendar.Month" using the 3 first characters of the month name
 * in the resource localization e.g. "Calendar.Jan", "Calendar.Feb" etc...
 * To localize strings for day names
 * use the values "Calendar.Day" in the resource localization e.g. "Calendar.Sunday", "Calendar.Monday" etc...
 *
 * @author Iddo Ari, Shai Almog
 */
public class Calendar extends Container {

    private ComboBox month;
    private ComboBox year;
    private MonthView mv;
    private static final String[] MONTHS = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] LABELS = {"Su", "M", "Tu", "W", "Th", "F", "Sa"};
    static final long MINUTE = 1000 * 60;
    static final long HOUR = MINUTE * 60;
    static final long DAY = HOUR * 24;
    static final long WEEK = DAY * 7;
    private EventDispatcher dispatcher = new EventDispatcher();
    private EventDispatcher dataChangeListeners = new EventDispatcher();
    private long[] dates = new long[42];
    private boolean changesSelectedDateEnabled = true;
    private TimeZone tmz;

    /**
     * Creates a new instance of Calendar set to the given date based on time
     * since epoch (the java.util.Date convention)
     *
     * @param time time since epoch
     */
    public Calendar(long time) {
        this(time, java.util.TimeZone.getDefault());
    }

    /**
     * Constructs a calendar with the current date and time
     */
    public Calendar() {
        this(System.currentTimeMillis());
    }

    /**
     * Creates a new instance of Calendar set to the given date based on time
     * since epoch (the java.util.Date convention)
     *
     * @param time time since epoch
     * @param tmz a reference timezone
     */
    public Calendar(long time, TimeZone tmz) {
        super(new BorderLayout());
        this.tmz = tmz;
        setUIID("Calendar");
        Container upper = new Container(new FlowLayout(Component.CENTER));
        month = new ComboBox();
        year = new ComboBox();
        mv = new MonthView(time);

        Vector months = new Vector();
        for (int i = 0; i < MONTHS.length; i++) {
            months.addElement("" + getLocalizedMonth(i));
        }
        ListModel monthsModel = new DefaultListModel(months);
        int selected = months.indexOf(getLocalizedMonth(mv.getMonth()));
        month.setModel(monthsModel);
        month.setSelectedIndex(selected);
        month.addActionListener(mv);

        java.util.Calendar cal = java.util.Calendar.getInstance(tmz);
        cal.setTime(new java.util.Date(time));
        month.getStyle().setBgTransparency(0);
        int y = cal.get(java.util.Calendar.YEAR);
        Vector years = new Vector();
        for (int i = 2100; i > 1900; i--) {
            years.addElement("" + i);
        }
        ListModel yearModel = new DefaultListModel(years);
        selected = years.indexOf("" + y);
        year.setModel(yearModel);
        year.setSelectedIndex(selected);
        year.getStyle().setBgTransparency(0);
        year.addActionListener(mv);
        Container cnt = new Container(new BoxLayout(BoxLayout.X_AXIS));
        cnt.setRTL(false);

        Container dateCnt = new Container(new BoxLayout(BoxLayout.X_AXIS));
        dateCnt.setUIID("CalendarDate");
        dateCnt.addComponent(month);
        dateCnt.addComponent(year);
        cnt.addComponent(dateCnt);
        upper.addComponent(cnt);

        addComponent(BorderLayout.NORTH, upper);
        addComponent(BorderLayout.CENTER, mv);
    }

    /**
     * Returns the time for the current calendar.
     *
     * @return the time for the current calendar.
     */
    public long getSelectedDay() {
        return mv.getSelectedDay();
    }

    private String getLocalizedMonth(int i) {
        Hashtable t = UIManager.getInstance().getResourceBundle();
        String text = MONTHS[i];
        if (t != null) {
            Object o = t.get("Calendar." + text);
            if (o != null) {
                text = (String) o;
            }
        }
        return text;
    }

    void componentChanged() {
        java.util.Calendar cal = java.util.Calendar.getInstance(tmz);
        cal.set(java.util.Calendar.YEAR, mv.getYear());
        cal.set(java.util.Calendar.MONTH, mv.getMonth());
        cal.set(java.util.Calendar.DAY_OF_MONTH, mv.getDayOfMonth());
        month.getParent().revalidate();
    }

    /**
     * Return the date object matching the current selection
     *
     * @return the date object matching the current selection
     */
    public Date getDate() {
        return new Date(mv.getSelectedDay());
    }

    /**
     * Sets the current date in the view and the selected date to be the same.
     *
     * @param d new date
     */
    public void setDate(Date d) {
        mv.setSelectedDay(d.getTime());
        mv.setCurrentDay(mv.currentDay, true);
        componentChanged();
    }

    /**
     * This method sets the Calendar selected day
     * @param d the selected day
     */
    public void setSelectedDate(Date d){
        mv.setSelectedDay(d.getTime());
    }

    /**
     * Sets the Calendar view on the given date, only the the month and year
     * are being considered.
     *
     * @param d the date to set the calendar view on.
     */
    public void setCurrentDate(Date d){
        mv.setCurrentDay(d.getTime(), true);
        componentChanged();
    }

    /**
     * Sets the Calendar timezone, if not specified Calendar will use the
     * default timezone
     * @param tmz the timezone
     */
    public void setTimeZone(TimeZone tmz){
        this.tmz = tmz;
    }

    /**
     * Gets the Calendar timezone
     *
     * @return Calendar TimeZone
     */
    public TimeZone getTimeZone(){
        return tmz;
    }

    /**
     * Sets the selected style of the month view component within the calendar
     *
     * @param s style for the month view
     */
    public void setMonthViewSelectedStyle(Style s) {
        mv.setSelectedStyle(s);
    }

    /**
     * Sets the un selected style of the month view component within the calendar
     *
     * @param s style for the month view
     */
    public void setMonthViewUnSelectedStyle(Style s) {
        mv.setUnselectedStyle(s);
    }

    /**
     * Gets the selected style of the month view component within the calendar
     *
     * @return the style of the month view
     */
    public Style getMonthViewSelectedStyle() {
        return mv.getSelectedStyle();
    }

    /**
     * Gets the un selected style of the month view component within the calendar
     *
     * @return the style of the month view
     */
    public Style getMonthViewUnSelectedStyle() {
        return mv.getUnselectedStyle();
    }

    /**
     * Fires when a change is made to the month view of this component
     *
     * @param l listener to add
     */
    public void addActionListener(ActionListener l) {
        mv.addActionListener(l);
    }

    /**
     * Fires when a change is made to the month view of this component
     *
     * @param l listener to remove
     */
    public void removeActionListener(ActionListener l) {
        mv.removeActionListener(l);
    }

    /**
     * Allows tracking selection changes in the calendar in real time
     *
     * @param l listener to add
     */
    public void addDataChangeListener(DataChangedListener l) {
        mv.addDataChangeListener(l);
    }

    /**
     * Allows tracking selection changes in the calendar in real time
     *
     * @param l listener to remove
     */
    public void removeDataChangeListener(DataChangedListener l) {
        mv.removeDataChangeListener(l);
    }

    /**
     * This flag determines if selected date can be changed by selecting an
     * alternative date
     *
     * @param changesSelectedDateEnabled if true pressing on a date will cause
     * the selected date to be changed to the pressed one
     */
    public void setChangesSelectedDateEnabled(boolean changesSelectedDateEnabled) {
        this.changesSelectedDateEnabled = changesSelectedDateEnabled;
    }

    /**
     * This flag determines if selected date can be changed by selecting an
     * alternative date
     *
     * @return true if enabled
     */
    public boolean isChangesSelectedDateEnabled() {
        return changesSelectedDateEnabled;
    }

    /**
     * This method creates the Day Button Component for the Month View
     *
     * @return a Button that corresponds to the Days Components
     */
    protected Button createDay() {
        Button day = new Button();
        day.setAlignment(CENTER);
        day.setUIID("CalendarDay");
        day.setEndsWith3Points(false);
        day.setTickerEnabled(false);
        return day;
    }

    /**
     * This method creates the Day title Component for the Month View
     *
     * @param day the relevant day values are 0-6 where 0 is sunday.
     * @return a Label that corresponds to the relevant Day
     */
    protected Label createDayTitle(int day) {
        String value = UIManager.getInstance().localize("Calendar." + DAYS[day], LABELS[day]);
        Label dayh = new Label(value, "CalendarTitle");
        dayh.setEndsWith3Points(false);
        dayh.setTickerEnabled(false);
        return dayh;
    }

    /**
     * This method updates the Button day.
     *
     * @param dayButton the button to be updated
     * @param day the new button day
     */
    protected void updateButtonDayDate(Button dayButton, int currentMonth, int day) {
        dayButton.setText("" + day);
    }

        class MonthView extends Container implements ActionListener{

        private long currentDay;
        private Button[] buttons = new Button[42];
        private Button selected;
        private long selectedDay = -1;

        public MonthView(long time) {
            super(new GridLayout(7, 7));
            setUIID("MonthView");
            for (int iter = 0; iter < DAYS.length; iter++) {
                addComponent(createDayTitle(iter));
            }
            for (int iter = 0; iter < buttons.length; iter++) {
                buttons[iter] = createDay();
                addComponent(buttons[iter]);
                if (iter <= 7) {
                    buttons[iter].setNextFocusUp(year);
                }
                buttons[iter].addActionListener(this);
            }
            setCurrentDay(time);

        }

        public void setCurrentDay(long day){
            setCurrentDay(day, false);
        }

        private void setCurrentDay(long day, boolean force) {
            repaint();
            java.util.Calendar cal = java.util.Calendar.getInstance(tmz);
            cal.setTime(new Date(currentDay));
            cal.set(java.util.Calendar.HOUR, 1);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 1);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);

            int yearOld = cal.get(java.util.Calendar.YEAR);
            int monthOld = cal.get(java.util.Calendar.MONTH);
            int dayOld = cal.get(java.util.Calendar.DAY_OF_MONTH);

            cal.setTime(new Date(day));
            cal.set(java.util.Calendar.HOUR, 1);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 1);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            int yearNew = cal.get(java.util.Calendar.YEAR);
            int monthNew = cal.get(java.util.Calendar.MONTH);
            int dayNew = cal.get(java.util.Calendar.DAY_OF_MONTH);
            year.setSelectedItem("" + yearNew);
            month.setSelectedIndex(monthNew);

            if (yearNew != yearOld || monthNew != monthOld || dayNew != dayOld || force) {
                currentDay = cal.getTime().getTime();
                if(selectedDay == -1){
                    selectedDay = currentDay;
                }
                int month = cal.get(java.util.Calendar.MONTH);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                long startDate = cal.getTime().getTime();
                int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
                cal.setTime(new Date(cal.getTime().getTime() - DAY));
                cal.set(java.util.Calendar.HOUR, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 1);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                int lastDay = cal.get(java.util.Calendar.DAY_OF_MONTH);
                int i = 0;
                if(dow > java.util.Calendar.SUNDAY){
                    //last day of previous month

                    while (dow > java.util.Calendar.SUNDAY) {
                        cal.setTime(new Date(cal.getTime().getTime() - DAY));
                        dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
                    }
                    int previousMonthSunday = cal.get(java.util.Calendar.DAY_OF_MONTH);
                    for (; i <= lastDay - previousMonthSunday; i++) {
                        buttons[i].setUIID("CalendarDay");
                        buttons[i].setEnabled(false);
                        buttons[i].setText("" + (previousMonthSunday + i));
                    }
                }
                //last day of current month
                cal.set(java.util.Calendar.MONTH, (month + 1) % 12);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.setTime(new Date(cal.getTime().getTime() - DAY));

                lastDay = cal.get(java.util.Calendar.DAY_OF_MONTH);

                int j = i;
                for (; j < buttons.length && (j - i + 1) <= lastDay; j++) {
                    buttons[j].setEnabled(true);
                    dates[j] = startDate;
                    if(dates[j] == selectedDay){
                        buttons[j].setUIID("CalendarSelectedDay");
                        selected = buttons[j];
                    }else{
                        buttons[j].setUIID("CalendarDay");
                    }
                    updateButtonDayDate(buttons[j], month, j - i + 1);
                    startDate += DAY;
                }
                int d = 1;
                for (; j < buttons.length; j++) {
                    buttons[j].setUIID("CalendarDay");
                    buttons[j].setEnabled(false);
                    buttons[j].setText("" + d++);
                }
            }
        }

        public int getDayOfMonth() {
            java.util.Calendar cal = java.util.Calendar.getInstance(tmz);
            cal.setTime(new Date(currentDay));
            return cal.get(java.util.Calendar.DAY_OF_MONTH);
        }

        public int getMonth() {
            java.util.Calendar cal = java.util.Calendar.getInstance(tmz);
            cal.setTime(new Date(currentDay));
            return cal.get(java.util.Calendar.MONTH);
        }

        public void incrementMonth() {
            int month = getMonth();
            month++;
            int year = getYear();
            if (month > java.util.Calendar.DECEMBER) {
                month = java.util.Calendar.JANUARY;
                year++;
            }
            setMonth(year, month);
        }

        private long getSelectedDay() {
            return selectedDay;
        }

        public void setSelectedDay(long selectedDay){
            java.util.Calendar cal = java.util.Calendar.getInstance(tmz);
            cal.setTime(new Date(selectedDay));
            cal.set(java.util.Calendar.HOUR, 1);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 1);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            this.selectedDay = cal.getTime().getTime();
        }

        private void setMonth(int year, int month) {
            java.util.Calendar cal = java.util.Calendar.getInstance(tmz);
            cal.setTimeZone(TimeZone.getDefault());
            cal.set(java.util.Calendar.MONTH, month);
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            cal.set(java.util.Calendar.YEAR, year);

            Date date = cal.getTime();
            long d = date.getTime();

            // if this is past the last day of the month (e.g. going from January 31st
            // to Febuary) we need to decrement the day until the month is correct
            while (cal.get(java.util.Calendar.MONTH) != month) {
                d -= DAY;
                cal.setTime(new Date(d));
            }
            setCurrentDay(d);
        }

        public void decrementMonth() {
            int month = getMonth();
            month--;
            int year = getYear();
            if (month < java.util.Calendar.JANUARY) {
                month = java.util.Calendar.DECEMBER;
                year--;
            }
            setMonth(year, month);
        }

        public int getYear() {
            java.util.Calendar cal = java.util.Calendar.getInstance(tmz);
            cal.setTime(new Date(currentDay));
            return cal.get(java.util.Calendar.YEAR);
        }

        public void addActionListener(ActionListener l) {
            dispatcher.addListener(l);
        }

        public void removeActionListener(ActionListener l) {
            dispatcher.removeListener(l);
        }

        /**
         * Allows tracking selection changes in the calendar in real time
         *
         * @param l listener to add
         */
        public void addDataChangeListener(DataChangedListener l) {
            dataChangeListeners.addListener(l);
        }

        /**
         * Allows tracking selection changes in the calendar in real time
         *
         * @param l listener to remove
         */
        public void removeDataChangeListener(DataChangedListener l) {
            dataChangeListeners.removeListener(l);
        }

        protected void fireActionEvent() {
            componentChanged();
            super.fireActionEvent();
            dispatcher.fireActionEvent(new ActionEvent(Calendar.this));
        }

        public void actionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            if(src instanceof ComboBox){
                setMonth(Integer.parseInt((String)year.getSelectedItem()),
                        month.getSelectedIndex());
                componentChanged();
                return;
            }
            if(changesSelectedDateEnabled){
                for (int iter = 0; iter < buttons.length; iter++) {
                    if (src == buttons[iter]) {

                        selected.setUIID("CalendarDay");
                        buttons[iter].setUIID("CalendarSelectedDay");
                        selectedDay = dates[iter];
                        selected = buttons[iter];
                        fireActionEvent();
                        if (!getComponentForm().isSingleFocusMode()) {
                            setHandlesInput(false);
                        }
                        return;
                    }
                }
            }
        }

    }
}
