package com.yc.patrol;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.FaceDepthRegisterActivity;
import com.baidu.idl.face.main.activity.FaceIRRegisterActivity;
import com.baidu.idl.face.main.activity.FaceRGBRegisterActivity;
import com.baidu.idl.face.main.activity.FaceUserGroupListActivity;
import com.baidu.idl.face.main.activity.PicoFaceDepthRegisterActivity;
import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.facesdkdemo.R;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @Time: 2019/5/24
 * @Author: v_zhangxiaoqing01
 * @Description: 注册页面
 */

public class PatrolFaceRegisterActivity extends BaseActivity implements View.OnClickListener {


    public static final int SOURCE_REG = 1;

    public static final int PICK_REG_VIDEO = 100;
    private Spinner usernameEt;
    private EditText userGroupEt;
    private TextView userInfoEt;

    private Button autoDetectBtn;
    private Button settingButton;
    private Button backBtn;

    private static final int TEXT_LENGTH = 20;
    private static final int USERINFO_LENGTH = 100;
    String[] names = null,fullNames = null;
    String userInfoSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_register_patrol);

        initView();
        initData();
    }

    public void initView() {

        usernameEt = (Spinner) findViewById(R.id.username_et);
        userGroupEt = (EditText) findViewById(R.id.userGroup_et);
        autoDetectBtn = (Button) findViewById(R.id.auto_detect_btn);
        settingButton = (Button) findViewById(R.id.id_reg_setting);
        backBtn = (Button) findViewById(R.id.id_reg_back);
        userInfoEt = (TextView) findViewById(R.id.user_info_tx);

        autoDetectBtn.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        backBtn.setOnClickListener(this);

    }

    private void initData() {
        if(App.patrolPlan.size() == 0){
            batchImport();
        }
        if(App.patrolPlan.size() > 0){
            names = new String[App.patrolPlan.size()];
            fullNames = new String[App.patrolPlan.size()];

            for(int i = 0 ; i < App.patrolPlan.size() ;i++){
                People people = App.patrolPlan.get(i);
                names[i] = people.getName();
                fullNames[i] = people.getFullName();
            }
            userInfoSpinner = names[0];

            ArrayAdapter<String> adapter = new ArrayAdapter<
                    >(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            usernameEt.setAdapter(adapter);
            usernameEt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    userInfoSpinner = names[position];
                    userInfoEt.setText(fullNames[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

    }

    // 正则只支持数字与字符
    private static final Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");

    @Override
    public void onClick(View view) {

        if (view == autoDetectBtn) {

            final String username = userInfoEt.getText().toString().trim();
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(PatrolFaceRegisterActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            Matcher matcher = pattern.matcher(username);
            if (!matcher.matches()) {
                Toast.makeText(PatrolFaceRegisterActivity.this, "用户名由数字、字母中的一个或者多个组成", Toast.LENGTH_SHORT).show();
                return;
            }
            if (username.length() > TEXT_LENGTH) {
                Toast.makeText(PatrolFaceRegisterActivity.this, "用户名输入长度超过限制！", Toast.LENGTH_SHORT).show();
                return;
            }

            String groupId = null;
            String groupStr = userGroupEt.getText().toString();
            if (TextUtils.isEmpty(groupStr)) {
                groupId = "default";
            } else {
                groupId = userGroupEt.getText().toString().trim();
            }
            if (groupId.length() > TEXT_LENGTH) {
                Toast.makeText(PatrolFaceRegisterActivity.this, "用户组输入长度超过限制！", Toast.LENGTH_SHORT).show();
                return;
            }

            matcher = pattern.matcher(groupId);
            if (!matcher.matches()) {
                Toast.makeText(PatrolFaceRegisterActivity.this, "groupId由数字、字母中的一个或者多个组成",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取用户
            List<User> listUsers = FaceApi.getInstance().getUserListByUserName(groupId, username);
            if (listUsers != null && listUsers.size() > 0) {
                for (User user : listUsers) {
                    String DBUserName = user.getUserName();
                    if (username.equals(DBUserName)) {
                        Toast.makeText(PatrolFaceRegisterActivity.this, "注册失败，有重名用户！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

//            final String userInfo = userInfoEt.getText().toString().trim();
            final String userInfo = userInfoSpinner;
            if (userInfo.length() > USERINFO_LENGTH) {
                Toast.makeText(PatrolFaceRegisterActivity.this, "用户信息输入长度超过限制！", Toast.LENGTH_SHORT).show();
                return;
            }

            // 判断活体类型
            int liveType = SingleBaseConfig.getBaseConfig().getType();
            if (liveType == 1 || liveType == 2) { // RGB
                if (liveType == 1) {
                    Toast.makeText(this, "当前活体策略：无活体", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "当前活体策略：RGB活体", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(PatrolFaceRegisterActivity.this, FaceRGBRegisterActivity.class);
                intent.putExtra("group_id", groupId);
                intent.putExtra("user_name", username);
                if (!TextUtils.isEmpty(userInfo)) {
                    intent.putExtra("user_info", userInfo);
                }
                startActivityForResult(intent, PICK_REG_VIDEO);
                finish();

            } else if (liveType == 3) { // NIR

                Toast.makeText(this, "当前活体策略：IR活体", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PatrolFaceRegisterActivity.this, FaceIRRegisterActivity.class);
                intent.putExtra("group_id", groupId);
                intent.putExtra("user_name", username);
                if (!TextUtils.isEmpty(userInfo)) {
                    intent.putExtra("user_info", userInfo);
                }
                startActivityForResult(intent, PICK_REG_VIDEO);
                finish();

            } else if (liveType == 4) { // Depth

                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                switch (cameraType) {
                    case 1: {
                        Intent proIntent = new Intent(PatrolFaceRegisterActivity.this, FaceDepthRegisterActivity.class);
                        proIntent.putExtra("group_id", groupId);
                        proIntent.putExtra("user_name", username);
                        if (!TextUtils.isEmpty(userInfo)) {
                            proIntent.putExtra("user_info", userInfo);
                        }
                        startActivityForResult(proIntent, PICK_REG_VIDEO);
                        finish();
                        break;
                    }
                    case 2: { // atlas
                        Intent proIntent = new Intent(PatrolFaceRegisterActivity.this, FaceDepthRegisterActivity.class);
                        proIntent.putExtra("group_id", groupId);
                        proIntent.putExtra("user_name", username);
                        if (!TextUtils.isEmpty(userInfo)) {
                            proIntent.putExtra("user_info", userInfo);
                        }
                        startActivityForResult(proIntent, PICK_REG_VIDEO);
                        finish();
                        break;
                    }
                    case 3: { // 奥比中光大白、海燕(结构光)
                        Intent proIntent = new Intent(PatrolFaceRegisterActivity.this, FaceDepthRegisterActivity.class);
                        proIntent.putExtra("group_id", groupId);
                        proIntent.putExtra("user_name", username);
                        if (!TextUtils.isEmpty(userInfo)) {
                            proIntent.putExtra("user_info", userInfo);
                        }
                        startActivityForResult(proIntent, PICK_REG_VIDEO);
                        finish();
                        break;
                    }

                    case 6: { // pico camera
                        Intent proIntent = new Intent(PatrolFaceRegisterActivity.this, PicoFaceDepthRegisterActivity.class);
                        proIntent.putExtra("group_id", groupId);
                        proIntent.putExtra("user_name", username);
                        if (!TextUtils.isEmpty(userInfo)) {
                            proIntent.putExtra("user_info", userInfo);
                        }
                        startActivityForResult(proIntent, PICK_REG_VIDEO);
                        finish();
                        break;
                    }
                    default:
                        break;

                }
            }

        } else if (view == settingButton) {
            startActivity(new Intent(this, FaceUserGroupListActivity.class));
        } else if (view == backBtn) {
            PatrolFaceRegisterActivity.this.finish();
        }
    }
}
