package com.keyboard.rainbow.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ihs.inputmethod.thirdparty.wxapi.HSWechatImageUtils;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	
    private IWXAPI mApi;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.entry);

        mApi = WXAPIFactory.createWXAPI(this, HSWechatImageUtils.APP_ID, false);
        mApi.handleIntent(getIntent(), this);
        
        //Toast.makeText(this, "WXEntryActivity called", Toast.LENGTH_LONG).show();
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
        mApi.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:	
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			break;
		default:
			break;
		}
	}

	@Override
	public void onResp(BaseResp resp) {
		String rst = "";
		
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			rst = "ok";
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
		    rst = "cancel";
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
		    rst = "deny";
			break;
		default:
			break;
		}
		
		if (!rst.isEmpty()) {
		    Toast.makeText(this, rst, Toast.LENGTH_LONG).show();
		}
	}
}