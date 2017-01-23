package pranav.apps.amazing.rxandroid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Pranav Gupta on 1/23/2017.
 */

public class NonBlockingUIExample extends Fragment{

    @Bind(R.id.progress_operation_running) ProgressBar _progress;
    @Bind(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _logAdapter;
    private List<String> _logs;

    /** composite disposables are used to add many disposable observers or disposable to group into one unit so then we can easily
     * remove all at one go whenever we destroy the view
     * Also remember that disposable is an **interface** which can be implemented by an observer or an observable
     * */
    private CompositeDisposable _disposables = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_non_blocking_ui,container,false);
        ButterKnife.bind(this,view);
        return view;
    }

    @OnClick(R.id.btn_start_operation)
    void doSomethingLongInBack(){
         _progress.setVisibility(View.VISIBLE);
         _log("Button Clicked");

        DisposableObserver<Boolean> disposableObserver = _getDisposableObserver();
        
        getObservable()
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserver);

        //Add it to bucket of disposable so that we can clear all of then in one go
        _disposables.add(disposableObserver);


    }
    /** note that there is difference between observable(rx) and observable(io.reactivex)
     * rx corres to 1.x while io.reactivex if from 2.x and has modified backpressure and some other operators are added
     * */
    private Observable<Boolean> getObservable() {
        return Observable.just(true).map(new Function<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean aBoolean) throws Exception {
                _log("We are inside map operator of Observable");
                _doSomethingLong();
                return aBoolean;
            }
        });
    }

    private void _doSomethingLong() {
        _log("Performing long operation...");
        try {
            Thread.sleep(3_000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private DisposableObserver<Boolean> _getDisposableObserver() {
        return new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean value) {
                 _log("on Next with bool value "+value);
            }

            @Override
            public void onError(Throwable e) {
                _log("Error Occured");
                _log(e.getMessage());
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onComplete() {
                _log("Task Completed");
                _progress.setVisibility(View.INVISIBLE);
            }
        };
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    private void _setupLogger() {
        _logs = new ArrayList<>();
        _logAdapter = new LogAdapter(getActivity(),_logs);
        _logsList.setAdapter(_logAdapter);
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
