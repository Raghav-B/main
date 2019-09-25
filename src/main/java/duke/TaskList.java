package duke;

//import java.util.;
import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;
import duke.exceptions.BadInputException;
import duke.items.DateTime;
import duke.items.Deadline;
import duke.items.Event;
import duke.items.Snooze;
import duke.items.Task;
import duke.items.Todo;
import duke.enums.TaskType;

/**
 * Manages the list of (different types of classes),
 * including all the methods to modify the list:
 * Adding each of the 3 types, print, delete, mark as done, search.
 */

public class TaskList {
    private ArrayList<Task> taskList;
    private int listIndex;

    public TaskList(ArrayList<Task> savedFile) {
        taskList = savedFile;
        //listIndex = lastIndex;
    }

    public TaskList() {
        listIndex = 0;
        taskList = new ArrayList<Task>();
    }

    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    public int getSize() {
        return taskList.size();
    }

    public void setListIndex(int index) {
        listIndex = index;
    }

    public int getListIndex() {
        return listIndex;
    }

    /**
     * Adds a task to the tasklist.
     * @return True if item was added successfully.
     */
    public boolean addItem(TaskType type, String description) {
        taskList.add(new Todo(description));

        return true;
    }

    /**
     * Adds a ToDo item to the list.
     * @param type TaskType enum MUST BE TODO.
     * @param description User input description of task
     * @param hrs Duration of task.
     * @return true if item was added successfully.
     */
    public boolean addItem(TaskType type, String description, int hrs) {
        if (type != TaskType.TODO) {
            return false;
        }
        taskList.add(new Todo(description, hrs));

        return true;
    }

    /**
     * Adds a TODO, DEADLINE or EVENT item to the list.
     * @param type TaskType enum of task to be added.
     * @param description User input description of task.
     * @param dateTimes Vararg of DateTimes that are to be added.
     * @return true if item was added successfully.
     */
    public boolean addItem(TaskType type, String description, DateTime... dateTimes) {
        try {
            switch (type) {
            case TODO:
                taskList.add(new Todo(description));
                break;

            case DEADLINE:
                taskList.add(new Deadline(description, dateTimes[0]));
                break;

            case EVENT:
                taskList.add(new Event(description, dateTimes[0], dateTimes[1]));
                break;

            default:
                return false;
            }

        } catch (BadInputException e) {
            System.out.println(e);
        }

        return true;
    }



    /**
     * Prints the whole list of items with index numbers.
     */
    public void printList() {
        int max = taskList.size();
        if (max == 0) {
            System.out.println("The list is currently empty.");
            return;
        }

        for (int i = 0; i < max; i++) { //Index starts from 0.
            System.out.print(i + 1 + ". "); //Add 1 to follow natural numbers.
            taskList.get(i).printTaskDetails();
        }
    }

    public Task getTask(int i) {
        return taskList.get(i);
    }

    /**
     * Deletes a task of the user's choice.
     *
     * @param i the index of the task to be deleted.
     */
    public void deleteTask(int i) {
        try {
            Task item = taskList.get(i);
            taskList.remove(i); //The original copy is gone.

            System.out.print("Okay! I've deleted this task: ");
            System.out.println(item.getDescription());

            if (item.getIsDone()) {
                System.out.println("The task was completed.");
            } else {
                System.out.println("The task was not completed.");
            }

        } catch (IndexOutOfBoundsException e) {
            printTaskNonexistent();
        }
    }

    /**
     * Marks a task as done.
     *
     * @param i the index of the task to be marked as done.
     */
    public void markTaskAsDone(int i) {

        try {
            taskList.get(i).markAsDone(); //Mark task as done.
            System.out.print("Nice! I've marked this task as done: ");
            System.out.println(taskList.get(i).getDescription()); //Prints task name
        } catch (IndexOutOfBoundsException e) {
            printTaskNonexistent();
        }
    }


    /**
     * Prints error message if a nonexistent task index is accessed.
     * Prints the task list for user to choose again.
     */
    private void printTaskNonexistent() {
        System.out.println("That task doesn't exist! Please check the available tasks again: ");
        printList();
    }

    /**
     * Allows the user to search for task descriptions that match a given string.
     * Prints the list of tasks that match. Alternatively prints a message if none are found.
     */
    public void searchForTask(String search) {
        int max = taskList.size();
        boolean found = false;

        for (int i = 0; i < max; i++) {
            if (taskList.get(i).getDescription().contains(search)) {
                System.out.print(i + 1 + ". "); //Print the index of the task.
                taskList.get(i).printTaskDetails();
                found = true;
            }
        }

        if (!found) {
            System.out.println("Sorry, I could not find any tasks containing the description \"" + search + "\".");
            System.out.println("Please try a different search string.");
        }
    }

    /**
     * Looks for undone deadlines within the next 5 Days and prints the task.
     */
    public void printReminders() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        long millisInFiveDays = 5 * 24 * 60 * 60 * 1000;

        for (Task task: taskList) {
            if (task instanceof Deadline && !task.getIsDone()) {
                Deadline deadline = (Deadline) task;
                long timeDifference = deadline.getDate().getTime() - now.getTime();
                if (timeDifference <= millisInFiveDays && timeDifference > 0) {
                    task.printTaskDetails();
                }
            }
        }
    }

    /**
     * Outputs the nearest event after which a block of free time has been detected.
     *
     * @param reqFreeHours The size of the block of free time the user wishes to find.
     */
    public void findFreeHours(int reqFreeHours) {
        if (reqFreeHours < 0) {
            System.out.println("Please enter an hour value >= 0.");
            return;
        }

        // Creating temporary ArrayList of events.
        ArrayList<Event> eventSortedList = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i) instanceof Event) {
                eventSortedList.add((Event) taskList.get(i));
            }
        }
        eventSortedList.sort(new EventDateTimeComparator());
        if (eventSortedList.size() <= 1) {
            System.out.println("You need at least 2 events to run this command!");
            return;
        }
        // Array is definitely sorted at this point.

        // Printing out for testing purposes.
        for (int i = 0; i < eventSortedList.size(); i++) {
            System.out.println(eventSortedList.get(i).toString());
        }

        DateTime latestEndTime = eventSortedList.get(0).getEventEndTimeObj();
        Event eventBeforeFreeTime = eventSortedList.get(0);
        int curMaxFreeHours = 0;
        boolean freeTimeFound = false;

        for (int i = 1; i < eventSortedList.size(); i++) {
            DateTime nextStartTime = eventSortedList.get(i).getEventStartTimeObj();
            DateTime nextEndTime = eventSortedList.get(i).getEventEndTimeObj();

            int compare = latestEndTime.getAt().compareTo(nextStartTime.getAt());
            // latestEndTime is earlier than nextStartTime
            if (compare < 0) {
                // Getting number of hours between latestEndTime and nextStartTime
                long ms = nextStartTime.getAt().getTime() - latestEndTime.getAt().getTime();
                int potentialMaxFreeHours = Math.round((float)ms / (1000 * 60 * 60));
                System.out.println(potentialMaxFreeHours);

                if (potentialMaxFreeHours >= curMaxFreeHours) {
                    curMaxFreeHours = potentialMaxFreeHours;
                    eventBeforeFreeTime = eventSortedList.get(i - 1);
                }

                // Since curEndTime is earlier than or equal to nextStartTime, it is guaranteed that
                // our latestEndTime will be equiv to nextEndTime - since this definitely
                // takes place after curEndTime.
                latestEndTime = nextEndTime;

                if (curMaxFreeHours >= reqFreeHours) {
                    eventBeforeFreeTime = eventSortedList.get(i - 1);
                    freeTimeFound = true;
                    break;
                }
            } else {
                // If curEndTime is later than or equal to nextStartTime - this only happens in the
                // event of a clash between events, i.e. events running concurrently.

                // Assuming the (next) clashing event takes place perfectly within the current event
                // we keep the value of latestEndTime untouched.
                if (nextEndTime.getAt().getTime() <= latestEndTime.getAt().getTime()) {
                    // Leave latestEndTime untouched.
                } else {
                    // Else, if the clashing event happens to end after the current event, we need to update
                    // out latestEndTime value accordingly.
                    latestEndTime = nextEndTime;
                }
            }
        }

        if (!freeTimeFound) {
            eventBeforeFreeTime = eventSortedList.get(eventSortedList.size() - 1);
        }

        System.out.println("The earliest free time I found was after the following event:\n"
                + eventBeforeFreeTime.toString());
    }
}
