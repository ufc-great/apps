package br.ufc.mdcc.benchimage.util;

import java.text.NumberFormat;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Util Transformations
 * 
 * @author Philipp
 */
public final class WidgetUtil {

	private Activity act;

	public WidgetUtil(Activity act) {
		this.act = act;
	}

	public String buttonToString(int id) {
		Button b = (Button) act.findViewById(id);
		return b.getText().toString();
	}

	public String spinnerToString(int id) {
		Spinner sp = (Spinner) act.findViewById(id);
		return sp.getSelectedItem().toString();
	}

	public String editTextToString(int id) {
		EditText et = (EditText) act.findViewById(id);
		return et.getText().toString();
	}

	public String editTextToStringMoney(int id) {
		// retira o cifrao
		return this.editTextToString(id).replaceAll("[a-zA-Z$,]", "");
	}

	public int editTextToInteger(int id) {
		EditText et = (EditText) act.findViewById(id);

		try {
			return Integer.parseInt(et.getText().toString());
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	public void cleanFields(List<Integer> lst) {
		for (int id : lst) {
			View v = act.findViewById(id);

			if (v instanceof EditText)
				((EditText) v).setText("");

			if (v instanceof Spinner)
				((Spinner) v).setSelection(0);

		}
	}

	public void stringToEditText(int id, String vlr) {
		EditText et = (EditText) act.findViewById(id);
		et.setText(vlr);
	}

	public void stringMoneyToEditText(int id, String vlr) {
		vlr = moedaFormat(vlr);
		this.stringToEditText(id, vlr);
	}

	public void stringToTextView(int id, String vlr) {
		TextView tv = (TextView) act.findViewById(id);
		tv.setText(vlr);
	}

	public void stringMoneyToTextView(int id, String vlr) {
		vlr = moedaFormat(vlr);
		this.stringToTextView(id, vlr);
	}

	public static String moedaFormat(String vlr) {
		return NumberFormat.getCurrencyInstance().format(Double.parseDouble(vlr));
	}

	// seta os listening nos buttons genericos
	public void setListeningButton(int idButton, Button.OnClickListener listen) {
		Button but = (Button) act.findViewById(idButton);
		but.setOnClickListener(listen);
	}

	public void clean() {
		act = null;
	}

	public Activity getActivity() {
		return act;
	}

}
