package cs307.butterfly;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Travis Kent on 10/6/2016.
 */
public class EventCalendarView extends LinearLayout
{
    // for logging
    private static final String LOGTAG = "Calendar View";

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "MMMM yyyy";

    // date format
    private String dateFormat;

    // current displayed month
    private Calendar currentDate = Calendar.getInstance();

    //event handling
    private EventHandler eventHandler = null;

    private ArrayList<CommunityEvent> events;

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;

    public EventCalendarView(Context context)
    {
        super(context);
        this.events = new ArrayList<>();
    }

    public EventCalendarView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.events = new ArrayList<>();
        initControl(context, attrs);
    }

    public EventCalendarView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.events = new ArrayList<>();
        initControl(context, attrs);
    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.content_calendar, this);

        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();
    }

    private void loadDateFormat(AttributeSet attrs)
    {
        dateFormat = DATE_FORMAT;
    }
    private void assignUiElements()
    {
        // layout is inflated, assign local variables to components
        header = (LinearLayout)findViewById(R.id.calendar_header);
        btnPrev = (ImageView)findViewById(R.id.calendar_prev_button);
        btnNext = (ImageView)findViewById(R.id.calendar_next_button);
        txtDate = (TextView)findViewById(R.id.calendar_date_display);
        grid = (GridView)findViewById(R.id.calendar_grid);
    }

    private void assignClickHandlers()
    {
        // add one month and refresh UI
        btnNext.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentDate.add(Calendar.MONTH, 1);
                CalendarActivity.currentMonth++;
                updateCalendar(events);
            }
        });

        // subtract one month and refresh UI
        btnPrev.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentDate.add(Calendar.MONTH, -1);
                CalendarActivity.currentMonth--;
                updateCalendar(events);
            }
        });

        // long-pressing a day
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> view, View cell, int position, long id)
            {
                if (eventHandler == null) {
                    return;
                }
                eventHandler.onClick((Date)view.getItemAtPosition(position));
            }
        });
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar(ArrayList<CommunityEvent> events)
    {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar)currentDate.clone();


        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT)
        {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // update grid
        grid.setAdapter(new CalendarAdapter(getContext(), cells, events, this));

        // update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        txtDate.setText(sdf.format(currentDate.getTime()));

    }

    public Calendar getCurrentDate() {
        return this.currentDate;
    }

    public void setEvents(ArrayList<CommunityEvent> events) {
        this.events = (ArrayList<CommunityEvent>)events.clone();
        updateCalendar(this.events);
    }

    public ArrayList<CommunityEvent> getEvents() {
        return this.events;
    }

    private class CalendarAdapter extends ArrayAdapter<Date>
    {
        // days with events
        private ArrayList<CommunityEvent> eventDays;

        private EventCalendarView eventCalendarView;

        // for view inflation
        private LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days, ArrayList<CommunityEvent> eventDays, EventCalendarView eventCalendarView)
        {
            super(context, R.layout.content_community_day, days);
            this.eventDays = eventDays;
            inflater = LayoutInflater.from(context);
            this.eventCalendarView = eventCalendarView;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent)
        {
            //currently viewed month
            Calendar current = eventCalendarView.getCurrentDate();
            int currentMonth = current.get(Calendar.MONTH);
            int currentYear = current.get(Calendar.YEAR);

            // day in question
            Date date = getItem(position);
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);
            dateCalendar.set(Calendar.HOUR_OF_DAY, 0);
            dateCalendar.set(Calendar.MINUTE, 0);
            dateCalendar.set(Calendar.SECOND, 0);
            dateCalendar.set(Calendar.MILLISECOND, 0);

            int dayOfYear = dateCalendar.get(Calendar.DAY_OF_YEAR);
            int month = dateCalendar.get(Calendar.MONTH);
            int year = dateCalendar.get(Calendar.YEAR);

            // today
            Date today = new Date();
            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.setTime(today);

            // inflate item if it does not exist yet
            if (view == null)
                view = inflater.inflate(R.layout.content_community_day, parent, false);

            // if this day has an event, specify event image
            view.setBackgroundResource(0);
            if (eventDays != null)
            {
                for (int i = 0; i < eventDays.size(); i++) {
                    if (eventDays.get(i).getDate().get(Calendar.DAY_OF_YEAR) == dayOfYear &&
                            eventDays.get(i).getDate().get(Calendar.YEAR) == year) {
                        view.setBackgroundResource(R.drawable.reminder);
                    }
                }
            }

            // clear styling
            ((TextView)view).setTypeface(null, Typeface.NORMAL);
            ((TextView)view).setTextColor(Color.BLACK);

            int todayMonth = todayCalendar.get(Calendar.MONTH);
            int todayYear = todayCalendar.get(Calendar.YEAR);
            int todayDayOfYear = todayCalendar.get(Calendar.DAY_OF_YEAR);

            if (month != currentMonth || year != currentYear)
            {
                // if this day is outside current month, grey it out
                ((TextView)view).setTextColor(Color.LTGRAY);
            }
            if (dayOfYear == todayDayOfYear && month == todayMonth && year == todayYear)
            {
                // if it is today, set it to blue/bold
                ((TextView)view).setTypeface(null, Typeface.BOLD);
                ((TextView)view).setTextColor(0xFFFF0000);
            }

            // set text
            ((TextView)view).setText(String.valueOf(dateCalendar.get(Calendar.DAY_OF_MONTH)));

            return view;
        }
    }

    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler)
    {
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler
    {
        void onClick(Date date);
    }
}