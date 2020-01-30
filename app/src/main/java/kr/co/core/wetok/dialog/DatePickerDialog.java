package kr.co.core.wetok.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import kr.co.core.wetok.R;


public class DatePickerDialog extends AppCompatActivity {
    private static final String TAG = "TEST_HOME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker_dialog);

        int year = getIntent().getIntExtra("year", 1970);
        int month = getIntent().getIntExtra("month", 1);
        int day = getIntent().getIntExtra("day", 1);

        DatePicker datePicker = (DatePicker)findViewById(R.id.dataPicker);
        datePicker.init(year, month-1, day, null);

        LinearLayout confirmBtn = (LinearLayout) findViewById(R.id.ll_confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("year", datePicker.getYear());
                resultIntent.putExtra("month", datePicker.getMonth()+1);
                resultIntent.putExtra("day", datePicker.getDayOfMonth());

                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
