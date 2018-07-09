package com.synertone.imitbutterknife;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.synertone.butterknife.Butterknife;
import com.synertone.butterknife_annotation.BindView;
import com.synertone.butterknife_annotation.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.tv_content)
    Button tv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Butterknife.bind(this);
        tv_content.setText("MainActivity");
    }

    @OnClick({R.id.tv_content,R.id.tv_rel})
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.tv_content:
                Intent intent=new Intent(this,SecondActivity.class);
                startActivity(intent);
                break;
            case  R.id.tv_rel:
                Toast.makeText(MainActivity.this,"hahahaha",Toast.LENGTH_SHORT).show();
                break;
                default:
                    break;
        }

    }
}
