package web.dassem.webtopdf;

import android.os.Environment;

import com.nononsenseapps.filepicker.AbstractFilePickerActivity;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;

public class FilePicker extends AbstractFilePickerActivity {
    private FilteredFilePickerFragment filteredFilePickerFragment;

    public FilePicker() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment getFragment(
            final String startPath, final int mode, final boolean allowMultiple,
            final boolean allowCreateDir) {
        filteredFilePickerFragment = new FilteredFilePickerFragment();
        // startPath is allowed to be null. In that case, default folder should be SD-card and not "/"
        filteredFilePickerFragment.setArgs(startPath != null ? startPath : Environment.getExternalStorageDirectory().getPath(),
                mode, allowMultiple, allowCreateDir);
        return filteredFilePickerFragment;
    }

    @Override
    public void onBackPressed() {
        // If at top most level, normal behaviour
        if (filteredFilePickerFragment != null) {
            if (filteredFilePickerFragment.isBackTop()) {
                super.onBackPressed();
            } else {
                // Else go up
                filteredFilePickerFragment.goUp();
            }
        }
        else{
            super.onBackPressed();
        }
    }
}