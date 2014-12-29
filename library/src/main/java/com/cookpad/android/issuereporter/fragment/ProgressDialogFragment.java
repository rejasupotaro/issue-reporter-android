package com.cookpad.android.issuereporter.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ProgressDialogFragment extends DialogFragment {
    private static final String EXTRA_MESSAGE = "message";

    public static ProgressDialogFragment newInstance(int message) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MESSAGE, message);

        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static void show(Activity activity, int message) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        ProgressDialogFragment dialogFragment = ProgressDialogFragment.newInstance(message);
        dialogFragment.show(fragmentManager, ProgressDialogFragment.class.getName());
    }

    public static void dismiss(Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        Fragment fragment = fragmentManager
                .findFragmentByTag(ProgressDialogFragment.class.getName());
        if (!(fragment instanceof ProgressDialogFragment)) {
            String message = "A fragment in the FragmentManager with tag name "
                    + ProgressDialogFragment.class.getName() + " is not in "
                    + ProgressDialogFragment.class.getName();
            throw new IllegalStateException(message);
        }

        ProgressDialogFragment dialogFragment = (ProgressDialogFragment) fragment;
        dialogFragment.dismiss();
    }

    public static boolean isShowing(Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        Fragment fragment =
                fragmentManager.findFragmentByTag(ProgressDialogFragment.class.getName());
        return fragment != null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int messageId = args.getInt(EXTRA_MESSAGE);
        String message = getString(messageId);

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });

        return progressDialog;
    }
}

