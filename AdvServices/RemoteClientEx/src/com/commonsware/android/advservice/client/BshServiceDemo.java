/***
  Copyright (c) 2008-2011 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Advanced Android Development_
    http://commonsware.com/AdvAndroid
*/

package com.commonsware.android.advservice.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.commonsware.android.advservice.IScript;
import com.commonsware.android.advservice.IScriptResult;

public class BshServiceDemo extends Activity {
  private IScript service=null;
  private ServiceConnection svcConn=new ServiceConnection() {
    public void onServiceConnected(ComponentName className,
                                    IBinder binder) {
      service=IScript.Stub.asInterface(binder);
    }

    public void onServiceDisconnected(ComponentName className) {
      service=null;
    }
  };

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.main);
  
    Button btn=(Button)findViewById(R.id.eval);
    final EditText script=(EditText)findViewById(R.id.script);
    
    btn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        String src=script.getText().toString();
        
        try {
          service.executeScript(src, callback);
        }
        catch (android.os.RemoteException e) {
          failureImpl(e.toString());
        }
      }
    });
    
    bindService(new Intent("com.commonsware.android.advservice.IScript"),
                svcConn, Context.BIND_AUTO_CREATE);
  }
  
  @Override
  public void onDestroy() {
    super.onDestroy();
    
    unbindService(svcConn);
  }
  
  private void successImpl(String result) {
    Toast
      .makeText(BshServiceDemo.this, result, Toast.LENGTH_LONG)
      .show();
  }
  
  private void failureImpl(String error) {
    AlertDialog.Builder builder=
              new AlertDialog.Builder(BshServiceDemo.this);
    
    builder
      .setTitle("Exception!")
      .setMessage(error)
      .setPositiveButton("OK", null)
      .show();
  }

  private final IScriptResult.Stub callback=new IScriptResult.Stub() {
    public void success(final String result) {
      runOnUiThread(new Runnable() {
        public void run() {
          successImpl(result);
        }
      });
    }
    
    public void failure(final String error) {
      runOnUiThread(new Runnable() {
        public void run() {
          failureImpl(error);
        }
      });
    }
  };
}
