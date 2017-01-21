package pranav.apps.amazing.rxandroid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.view.ViewClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Pranav Gupta on 1/20/2017.
 */

public class BufferFragment extends Fragment {

    @Bind(R.id.list_threading_log)ListView _logList;
    @Bind(R.id.btn_start_operation)Button _tapBtn;

    private LogAdapter _logAdapter;
    private List<String> _logs;

    /** disposable acts as a observable and is used with disposable observer which acts as a observer
     * */
    private Disposable _disposable;

    @Override
    public void onResume() {
        super.onResume();
        _disposable = getDisposableforBuffer();
    }

    @Override
    public void onPause() {
        super.onPause();
        _disposable.dispose();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }

    private Disposable getDisposableforBuffer() {
        return RxJavaInterop.toV2Observable(RxView.clickEvents(_tapBtn))
                .map(new Function<ViewClickEvent, Integer>() {
                    @Override
                    public Integer apply(ViewClickEvent viewClickEvent) throws Exception {
                        _log("GOT A TAP");
                        return 1;
                    }
                }).buffer(4, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Integer>>() {
                    @Override
                    public void onNext(List<Integer> value) {
                        if(value.size()>0)
                        _log(String.format("GOT %d taps in 4 secs",value.size()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        _log("Error Error Error");

                    }

                    @Override
                    public void onComplete() {
                       _log("Work is Completed");
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buffer,container,false);
        ButterKnife.bind(this,view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _logAdapter = new LogAdapter(getActivity(),_logs);
        _logList.setAdapter(_logAdapter);
    }

    private void _log(String logMsg) {

        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, logMsg + " (main thread) ");
            _logAdapter.notifyDataSetChanged();
        } else {
            _logs.add(0, logMsg + " (NOT main thread) ");

            // You can only do below stuff on main thread.
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                   _logAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private boolean _isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


}
