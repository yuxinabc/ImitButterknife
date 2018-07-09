package com.synertone.imitbutterknife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.synertone.butterknife.Butterknife;
import com.synertone.butterknife_annotation.BindView;

public class SecondActivity extends AppCompatActivity {
    @BindView(R.id.tv_content)
    TextView tv_content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Butterknife.bind(this);
        tv_content.setText("SecondActivity");
    }
}
