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
import java.util.ListIterator;

/**
 * Created by a7med on 28/06/2015.
 */
public class EventCalendarView extends LinearLayout
{
    // for logging
    private static final String LOGTAG = "Calendar View";

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "MMM yyyy";

    // date format
    private String dateFormat;

    // current displayed month
    private Calendar currentDate = Calendar.getInstance();

    //event handling
    private EventHandler eventHandler = null;

    private ArrayList<Date> events;

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;

    public EventCalendarView(Context context)
    {
        super(context);
    }

    public EventCalendarView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initControl(context, attrs);
    }

    public EventCalendarView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.content_community, this);

        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();

        ArrayList<Date> eventsList = new ArrayList<>();
        Date date = new Date();
        long time = System.currentTimeMillis();
        for (int i = 1; i < 10; i++) {
            Date dateClone = (Date) date.clone();
            eventsList.add(dateClone);
            date.setTime(date.getTime() + (5 * 86400000));
        }
        setEvents(eventsList);
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
                updateCalendar(events);
            }
        });

        // long-pressing a day
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {

            @Override
            public boolean onItemLongClick(AdapterView<?> view, View cell, int position, long id)
            {
                // handle long-press
                if (eventHandler == null)
                    return false;

                eventHandler.onDayLongPress((Date)view.getItemAtPosition(position));
                return true;
            }
        });
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar(ArrayList<Date> events)
    {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar)currentDate.clone();


        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
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

    public Date getCurrentDate() {
        return this.currentDate.getTime();
    }

    public void setEvents(ArrayList<Date> events) {
        this.events = (ArrayList<Date>)events.clone();
        updateCalendar(this.events);
    }

    private class CalendarAdapter extends ArrayAdapter<Date>
    {
        // days with events
        private ArrayList<Date> eventDays;

        private EventCalendarView eventCalendarView;

        // for view inflation
        private LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days, ArrayList<Date> eventDays, EventCalendarView eventCalendarView)
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
            Date current = eventCalendarView.getCurrentDate();
            String currentString = current.toString();
            String[] currentArray = currentString.split(" ");
            String currentMonth = currentArray[1];
            int currentYear = Integer.parseInt(currentArray[5]);

            // day in question
            Date date = getItem(position);
            String dateString = date.toString();
            String[] dateArray = dateString.split(" ");

            int day = Integer.parseInt(dateArray[2]);
            String month = dateArray[1];
            int year = Integer.parseInt(dateArray[5]);

            // today
            Date today = new Date();

            // inflate item if it does not exist yet
            if (view == null)
                view = inflater.inflate(R.layout.content_community_day, parent, false);

            // if this day has an event, specify event image
            view.setBackgroundResource(0);
            if (eventDays != null)
            {
                ListIterator<Date> dateIterator = eventDays.listIterator();
                while(dateIterator.hasNext()) {
                    Date eventDate = dateIterator.next();
                    String eventString = eventDate.toString();
                    String[] eventArray = eventString.split(" ");
                    int eventDay = Integer.parseInt(eventArray[2]);
                    String eventMonth = eventArray[1];
                    int eventYear = Integer.parseInt(eventArray[5]);
                    if (eventDay == day && eventMonth.equals(month) && eventYear == year)
                    {
                        // mark this day for event
                        view.setBackgroundResource(R.drawable.reminder);
                        break;
                    }
                }
            }

            // clear styling
            ((TextView)view).setTypeface(null, Typeface.NORMAL);
            ((TextView)view).setTextColor(Color.BLACK);

            String todayString = today.toString();
            String[] todayArray = todayString.split(" ");
            String todayMonth = todayArray[1];
            int todayYear = Integer.parseInt(todayArray[5]);
            int todayDate = Integer.parseInt(todayArray[2]);

            if (!month.equals(currentMonth) || year != currentYear)
            {
                // if this day is outside current month, grey it out
                ((TextView)view).setTextColor(Color.LTGRAY);
            }
            if (day == todayDate && month.equals(todayMonth) && year == todayYear)
            {
                // if it is today, set it to blue/bold
                ((TextView)view).setTypeface(null, Typeface.BOLD);
                ((TextView)view).setTextColor(Color.CYAN);
            }

            // set text
            ((TextView)view).setText(String.valueOf(day));

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
        void onDayLongPress(Date date);
    }
}