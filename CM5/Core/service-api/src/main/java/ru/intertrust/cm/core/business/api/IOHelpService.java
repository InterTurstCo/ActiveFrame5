package ru.intertrust.cm.core.business.api;

import java.io.OutputStream;

/**
 * Service contains util methods to work with IO
 */
public interface IOHelpService {

    /**
     * Method gets {@link java.io.InputStream} from {@link InputStreamProvider} and copies
     * data to {@link OutputStream}. Method closes {@link java.io.InputStream} after work.
     *
     * Methods changes the data to control that output data will be returned with LF EOL
     *
     * @param inputStreamProvider parameter provides the {@link java.io.InputStream} to copy from. It will be closed after using
     * @param os the {@link OutputStream} to copy to
     */
    void copyWithEolControl(InputStreamProvider inputStreamProvider, OutputStream os);

    /**
     * Method gets {@link java.io.InputStream} from {@link InputStreamProvider} and copies
     * data to {@link OutputStream}. Method closes {@link java.io.InputStream} after work.
     *
     * Methods changes the data to control that output data will be returned with LF EOL
     *
     * In additions, method returns MD5 sum of {@link OutputStream} (in HEX)
     *
     * @param inputStreamProvider parameter provides the {@link java.io.InputStream} to copy from. It will be closed after using
     * @param os the {@link OutputStream} to copy to
     * @return HEX of {@link OutputStream}
     */
    String copyWithEolControlAndMd5(InputStreamProvider inputStreamProvider, OutputStream os);

}
