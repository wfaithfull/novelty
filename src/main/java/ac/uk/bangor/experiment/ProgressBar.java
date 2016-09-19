package ac.uk.bangor.experiment;

/**
 * Created by wfaithfull on 19/09/16.
 */
public class ProgressBar {

    final char[] SPINNER_CHARS = {'|', '/', '—', '\\'};
    int spindex = 0;

    private char character;
    private int width;

    public ProgressBar(char character, int width) {
        this.character = character;
        this.width = width;
    }

    private StringBuilder builder = new StringBuilder(width);

    public void update(int progress, int total) {
        update(progress, total, "");
    }

    public char getSpinner() {
        if(spindex == SPINNER_CHARS.length)
            spindex = 0;

        return SPINNER_CHARS[spindex++];
    }

    public void update(int progress, int total, String message) {
        char spinner = getSpinner();

        int percent = (++progress * 100) / total;
        int extrachars = (percent / 2) - this.builder.length();

        while (extrachars-- > 0) {
            builder.append(character);
        }

        System.out.printf("\r(%c) %3d%% [%-"+(width-1)+"s] %s", spinner, percent, builder, message);

        if (progress == total) {
            System.out.flush();
            System.out.println();
            builder = new StringBuilder(width);
        }
    }
}
