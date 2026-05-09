package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

public class CustomizeActivity extends AppCompatActivity {

    private ImageView imgCharacter;
    private TabLayout tabLayout;
    private RecyclerView rvItems;
    private Button btnShop;
    private Button btnGoMain;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);

        imgCharacter = findViewById(R.id.imgCharacterCustomize);
        tabLayout = findViewById(R.id.tabLayoutCustomize);
        rvItems = findViewById(R.id.rvItems);
        btnShop = findViewById(R.id.btnShop);
        btnGoMain = findViewById(R.id.btnGoMain);
        btnBack = findViewById(R.id.btnCustomizeBack);

        tabLayout.addTab(tabLayout.newTab().setText("헤어"));
        tabLayout.addTab(tabLayout.newTab().setText("악세"));
        tabLayout.addTab(tabLayout.newTab().setText("옷"));
        tabLayout.addTab(tabLayout.newTab().setText("신발"));
        rvItems.setLayoutManager(new GridLayoutManager(this, 3));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String category = tab != null && tab.getText() != null ? tab.getText().toString() : "카테고리";
                Toast.makeText(CustomizeActivity.this, category + " 카테고리를 준비 중입니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        btnBack.setOnClickListener(v -> finish());
        btnShop.setOnClickListener(v -> startActivity(new Intent(this, ShopActivity.class)));
        btnGoMain.setOnClickListener(v -> openMain());
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
