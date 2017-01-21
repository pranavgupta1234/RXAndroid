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

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Pranav Gupta on 1/21/2017.
 */

public class TimeOutFragment extends Fragment{

    @Bind(R.id.list_threading_log)ListView _logList;

    private LogAdapter _logAdapter;
    /**Disposable observer implements disposable interface
    */
    private DisposableObserver<String> _disposable;
    private List<String> _logs;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!_disposable.isDisposed()){
            _disposable.dispose();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buffer,container,false);
        ButterKnife.bind(this,view);
        return view;
    }

    @OnClick(R.id.btn_demo_timeout_1_2s)
    void TimeTakingTask_2s(){
         _disposable = getObserver();

        take2sToCompleteObservable()
                .timeout(3, TimeUnit.SECONDS,_getTimeOutObservable())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_disposable);
    }

    @OnClick(R.id.btn_demo_timeout_1_5s)
    void TimeTakingTask_5s(){
        _disposable = getObserver();

        take5sToCompleteObservable()
                .timeout(3,TimeUnit.SECONDS,_getTimeOutObservable())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_disposable);
    }

    private Observable<String> _getTimeOutObservable(){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> subscriber) throws Exception {
                _log("Sorry bro :D Time Out");
                subscriber.onComplete();
            }
        });
    }


    private Observable<String> take2sToCompleteObservable(){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> subscriber) throws Exception {
                _log("Starting a 2 sec Task");
                subscriber.onNext("2 sec task");
                try {
                    Thread.sleep(2_000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                subscriber.onComplete();
            }
        });
    }

    private Observable<String> take5sToCompleteObservable(){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> subscriber) throws Exception {
                _log("Starting a 5 sec task");
                subscriber.onNext("5 sec task");
                try {
                    Thread.sleep(5_000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                subscriber.onComplete();
            }
        });
    }

    public DisposableObserver<String> getObserver(){
        return new DisposableObserver<String>() {
            @Override
            public void onNext(String value) {
                _log("onNext :"+value);
            }

            @Override
            public void onError(Throwable e) {
                _log("Error Occurred");
            }

            @Override
            public void onComplete() {
                _log("Task Completed ! Well Done :)");
            }
        };
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
