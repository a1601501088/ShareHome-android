package com.vunken.tv_sharehome.fragment;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.RxBus;
import com.vunken.tv_sharehome.activity.HomeActivity;
import com.vunken.tv_sharehome.adapter.BaseHolder;
import com.vunken.tv_sharehome.adapter.CallLogHolder;
import com.vunken.tv_sharehome.adapter.DefaultAdapter;
import com.vunken.tv_sharehome.base.BaseFragment;
import com.vunken.tv_sharehome.greendao.dao.CallRecordersDao.Properties;
import com.vunken.tv_sharehome.greendao.dao.bean.CallRecorders;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.PhoneUtils;

public class CallLogFragment extends BaseFragment implements OnKeyListener {
	private List<CallRecorders> callLogs;
	public ListView listview;
	private CallLogAdapter adapter;
	private Button btn_remove_all;
	private TextView tv_not_call_log;
	private int count;
	private Subscription subscribe2;
	private String TAG = "CallLogFragment";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_call_log, null);
		initViews(view);
		initCallLog();
		initRxBus();
		//initDatas();
		initListener();
		return view;
	}

	private void initRxBus() {
		/**
		 * 刷新通话记录
		 */
		subscribe2 = RxBus.getInstance().toObservable(Integer.class)
						.filter(new Func1<Integer, Boolean>() {
							@Override
							public Boolean call(Integer arg0) {
								return arg0 == 100 ;
							}
						}).subscribe(new Action1<Integer>() {
							@Override
							public void call(Integer arg0) {
								
								//callLogFragment = new CallLogFragment();
//								setFragment(callLogFragment);
								//setFragment(1);
								/**
								 * 查询所有通话记录
								 */
								callRecorders = DbCore.getDaoSession().getCallRecordersDao().
										queryBuilder().orderDesc(Properties.CallId).build().list();
								
								
								adapterNotify();
							}
						});
		
	}

	private void initListener() {
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CallRecorders callRecorders = (CallRecorders) listview.getItemAtPosition(position);
				if (callRecorders != null) {
					String call = callRecorders.getCallRecordersPhone();
					if ( !TextUtils.isEmpty(call)) {
						/**
						 * 视频呼叫
						 */
						PhoneUtils.callVideo(getActivity(), call,"");
					}
				}

			}
		});

		btn_remove_all.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((HomeActivity) getActivity()).home_call_log.requestFocus();
				showPrompt(true);
				
				//CallLogApi.removeAll();
				/**
				 * 清空通话记录
				 */
				DbCore.getDaoSession().getCallRecordersDao().deleteAll();
				callRecorders.clear();
				adapterNotify();
				
			}
		});
		
		btn_remove_all.setOnKeyListener(this);
		listview.setOnKeyListener(this);

	}
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction()==KeyEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.btn_remove_all:
				if (keyCode==KeyEvent.KEYCODE_DPAD_LEFT) {
					((HomeActivity)getActivity()).home_call_log.requestFocus();
					return true;
				}else if (keyCode==KeyEvent.KEYCODE_DPAD_DOWN) {
					if (listview!=null) {
						listview.setSelection(0);
					}
				}
				break;
			case R.id.listview:
				if (keyCode==KeyEvent.KEYCODE_DPAD_LEFT) {
					((HomeActivity)getActivity()).home_call_log.requestFocus();
					return true;
				}else if (keyCode==KeyEvent.KEYCODE_DPAD_DOWN) {
					if (listview.getSelectedItemPosition()==adapter.getCount()-1) {
						listview.setSelection(0);
						return true;
					}
					
				}
				break;

			default:
				break;
			}
		}
		return false;
	}
	

	private void adapterNotify() {
		//List<CallLog> callLogs = CallLogApi.getCallLogList(CallLogApi.QUERY_FILTER_TYPE_ALL,-1);
		
		if (callRecorders==null||callRecorders.isEmpty()) {
			showPrompt(true);
		}else {
			showPrompt(false);
			  Observable.from(callRecorders)
			.filter(new Func1<CallRecorders, Boolean>() {
				@Override
				public Boolean call(CallRecorders arg0) {
					return arg0!=null ;
				}
			})
			.distinct(new Func1<CallRecorders, String>() {
				@Override
				public String call(CallRecorders arg0) {
					String username = arg0.getCallRecordersPhone();
					if (username.startsWith("8")||username.startsWith("9")) {
						username = username.substring(1);
					}
					return username;
				}
			})
			.take(6)
			.buffer(6)
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Subscriber<List<CallRecorders>>() {
				@Override
				public void onCompleted() {
					this.unsubscribe();
				}

				@Override
				public void onError(Throwable arg0) {
					this.unsubscribe();
				}
				@Override
				public void onNext(List<CallRecorders> arg0) {
					
					CallLogFragment.this.callLogs = arg0;
						adapter = new CallLogAdapter(arg0);
						listview.setAdapter(adapter);
						
						
				}
				
			});
		}	
				

	}


	private class CallLogAdapter extends DefaultAdapter<CallRecorders> {
		protected CallLogAdapter(List<CallRecorders> list) {
			super(list);
		}

		@Override
		protected BaseHolder getHolder() {
			return new CallLogHolder(CallLogFragment.this);
		}
	}

	private void initViews(View view) {
		listview = (ListView) view.findViewById(R.id.listview);
		count = listview.getCount();
		btn_remove_all = (Button) view.findViewById(R.id.btn_remove_all);
		tv_not_call_log = (TextView) view.findViewById(R.id.tv_not_call_log);

	}

	private List<CallRecorders> callRecorders;
	private void initCallLog() {
		/**
		 * 那到所有通话记录
		 */
		callRecorders = ((HomeActivity)getActivity()).callRecorders;
	
		adapterNotify();
	}
/**
 * true : 为显示提示
 * @param b
 */
	private void showPrompt(boolean b) {
		tv_not_call_log.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
		btn_remove_all.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
		listview.setVisibility(b?View.INVISIBLE:View.VISIBLE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (subscribe2 != null) {
			subscribe2.unsubscribe();
		}
	}

	
}
