package no.ntnu.datakomm.chat;

import java.util.ArrayList;

public class StringSplitter {

    private String splitRegex;

    private String[] splittedString;


    /**
     * @param splitRegex regex for the splitting
     */
    StringSplitter(String splitRegex) {
        this.setSplitRegex(splitRegex);
    }

    /**
     * Sets the regex string for splitting the splitting.
     *
     * @param splitRegex the string to
     * @throws IllegalArgumentException if string is empty or null
     */
    private void setSplitRegex(String splitRegex) {
        if (splitRegex == null) throw new IllegalArgumentException("Regex string object is null");
        if (splitRegex.isEmpty()) throw new IllegalArgumentException("Regex string is empty");
        this.splitRegex = splitRegex;
    }

    /**
     * Splits the string by the regex provided on creation.
     * And stores the divided string in local field.
     *
     * @param string the string to split
     * @param limit  the number of times to split the string
     * @throws IllegalArgumentException if string is empty or {@code null}
     */
    public void split(String string, int limit) {
        if (string == null) throw new IllegalArgumentException("String can not be null");
        if (string.isEmpty()) throw new IllegalArgumentException("String can not be empty.");
        this.splittedString = string.split(this.splitRegex, limit);
    }

    /**
     * Returns the the part of the splitted string.
     * <p>
     * Throws <code>IllegalArgumentException</code> if part number is negative
     * Throws <code>IndexOutOfBoundsException</code> if invalid part is provided.
     *
     * @param string the string to get a part from
     * @param part   part number from 1 to X
     * @return the string at part position
     * @throws IndexOutOfBoundsException if trying to get invalid part (index)
     * @throws IllegalArgumentException  if part number is negative
     */
    public String getPartFromString(String string, int part) {
        if (part < 0) throw new IllegalArgumentException("Part number must be greater or equal to 0");
        part = part > 0 ? part - 1 : part;
        return string.split(this.splitRegex, part)[part];
    }


    /**
     * Returns all parts of the string after it is split by the the provided regex.
     * <p>
     * Throws {@code IllegalArgumentException} if part number is negative
     *
     * @param string the string to get all parts from
     * @return all parts of the split string.
     * @throws IllegalArgumentException if string is empty or {@code null}
     */
    public String[] getAllPartsFromString(String string) {
        if (string == null) throw new IllegalArgumentException("String can not be null");
        if (string.isEmpty()) throw new IllegalArgumentException("String can not be empty.");
        return string.split(this.splitRegex, 0);
    }


    /**
     * Returns the part from the last splitted string.
     * The part number must be a positive number.
     * <p>
     * Throws {@see IllegalArgumentException} if part number is negative
     * Throws {@see IndexOutOfBoundsException} if invalid part is provided.
     *
     * @param part part number from 1 to X
     * @return the string at part position
     * @throws IndexOutOfBoundsException if trying to get invalid part (index)
     * @throws IllegalArgumentException  if part number is negative
     */
    public String getPart(int part) {
        if (part < 0) throw new IllegalArgumentException("Part number must be greater or equal to 0");
        part = part > 0 ? part - 1 : part;
        return this.splittedString[part];
    }

    /**
     * Returns the splitted from the last {@link #split}
     *
     * @return the splitted string array
     */
    public String[] getSplittedString() {
        return this.splittedString;
    }

}
