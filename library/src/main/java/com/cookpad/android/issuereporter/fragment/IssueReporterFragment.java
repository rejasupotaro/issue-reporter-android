package com.cookpad.android.issuereporter.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.cookpad.android.issuereporter.IIntentReceiveService;
import com.cookpad.android.issuereporter.IIntentReceiveServiceCallback;
import com.cookpad.android.issuereporter.R;
import com.cookpad.android.issuereporter.ReportMail;
import com.cookpad.android.issuereporter.ReportNotification;
import com.cookpad.android.issuereporter.service.IntentReceiveService;
import com.cookpad.android.issuereporter.task.ScreenshotTask;
import com.cookpad.android.issuereporter.util.IntentUtils;

import java.io.File;
import java.io.IOException;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class IssueReporterFragment extends Fragment {
    public static final String FRAGMENT_TAG = IssueReporterFragment.class.getName();
    public static final String EXTRA_REPORT = "extra_report";

    private ReportMail reportMail;

    public static IssueReporterFragment newInstance(ReportMail reportMail) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_REPORT, reportMail);

        IssueReporterFragment fragment = new IssueReporterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        reportMail = args.getParcelable(EXTRA_REPORT);
    }

    @Override
    public void onResume() {
        super.onResume();

        ReportNotification.show(getActivity());

        Intent intent = IntentReceiveService.createIntent(getActivity());
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();

        ReportNotification.cancel(getActivity());

        unbindService(connection);
    }

    private IIntentReceiveService intentReceiverService;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            intentReceiverService = IIntentReceiveService.Stub.asInterface(service);
            try {
                intentReceiverService.registerCallback(callback);
            } catch (RemoteException e) {
                intentReceiverService = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            intentReceiverService = null;
        }
    };

    private IIntentReceiveServiceCallback callback = new IIntentReceiveServiceCallback.Stub() {
        @Override
        public void onReceiveReportIssueIntent() throws RemoteException {
            try {
                takeScreenshotAndSend();
            } catch (IOException e) {
                Toast.makeText(getActivity(), R.string.failed_to_take_screenshot,
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void takeScreenshotAndSend() throws IOException {
        ProgressDialogFragment.show(getActivity(), R.string.wait_a_moment);
        new ScreenshotTask(getActivity(), new ScreenshotTask.Callback() {
            @Override
            public void onTakeScreenshot(File bitmapFile) {
                Activity activity = getActivity();

                ProgressDialogFragment.dismiss(activity);
                String authority = "com.cookpad.android.issuereporter.fileprovider";
                Uri bitmapUri = FileProvider.getUriForFile(activity, authority, bitmapFile);

                IntentUtils.sendMail(getActivity(), reportMail, bitmapUri);
            }

            @Override
            public void onCatchIOException(IOException e) {
                ProgressDialogFragment.dismiss(getActivity());

                Toast.makeText(getActivity(), R.string.failed_to_take_screenshot,
                        Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    protected boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return getActivity().bindService(service, conn, flags);
    }

    protected void unbindService(ServiceConnection conn) {
        getActivity().unbindService(conn);
    }
}

