package ac.uk.bangor.experiment;

import java.io.PrintStream;

/**
 * Created by wfaithfull on 19/09/16.
 */
public class ProgressBar {

    final char[] SPINNER_CHARS = {'|', '/', '-', '\\'};
    int spindex = 0;

    private char character;
    private int width;
    private PrintStream printStream;

    public ProgressBar(char character, int width) {
        this(character, width, System.out);
    }

    public ProgressBar(char character, int width, PrintStream printStream) {
        this.character = character;
        this.width = width;
        this.printStream = printStream;
    }

    private StringBuilder builder = new StringBuilder(width);

    public void update(long progress, long total) {
        update(progress, total, "");
    }

    public char getSpinner() {
        if(spindex == SPINNER_CHARS.length)
            spindex = 0;

        return SPINNER_CHARS[spindex++];
    }

    private long lastTotal;

    public void update(long progress, long total, String message) {
        char spinner = getSpinner();

        long percent = (++progress * 100) / total;
        long extrachars = (int) ((percent / 2L) - this.builder.length());

        while (extrachars-- > 0) {
            builder.append(character);
        }

        String progressBar = String.format("(%c) %3d%% [%-"+width+"s] %s", spinner, percent, builder, message);
        printStream.printf("\r(%c) %3d%% [%-"+width+"s] %s", spinner, percent, builder, message);

        if(lastTotal != total) {
            reset();
        }

        if (progress == total) {
            printStream.flush();
            printStream.println();
            builder = new StringBuilder(width);
        }

        lastTotal = total;
    }

    public void reset() {
        printStream.flush();
        builder = new StringBuilder(width);
    }
}
