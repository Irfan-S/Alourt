/*
 * Copyright (c) 2020 Irfan S.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Syed Irfan Ahmed <irfansa1@ymail.com>
 */

package irfan.apps.alourt.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;

import irfan.apps.alourt.R;

public class DialogUtil extends Dialog implements android.view.View.OnClickListener {


    public DialogUtil(@NonNull Context context) {
        super(context);
    }

    //switch seems to run into errors, hence using if else if's
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_onboarding);
    }

    @Override
    public void onClick(View v) {

    }
}
