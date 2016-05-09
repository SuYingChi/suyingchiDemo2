package com.keyboard.inputmethod.panels.gif.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;


/**
 * Created by dsapphire on 16/1/9.
 */
public final class CustomSearchEditText extends EditText {
	private EditorInfo editorInfo;

	public CustomSearchEditText(Context context) {
		super(context);
	}

	public CustomSearchEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomSearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	private EditorInfo getLocalEditorInfo() {
		EditorInfo localEditorInfo = new EditorInfo();
		localEditorInfo.imeOptions = EditorInfo.IME_ACTION_SEARCH;
		localEditorInfo.actionId =getImeActionId();
		localEditorInfo.fieldId = getId();
		localEditorInfo.packageName=getContext().getPackageName();
		localEditorInfo.fieldName="com.ihs.inputmethod.suggestions.InsideEditText";
		localEditorInfo.inputType = EditorInfo.TYPE_CLASS_TEXT;
		localEditorInfo.initialSelEnd = getSelectionStart();
		localEditorInfo.initialSelEnd = getSelectionEnd();
		setFocusableInTouchMode(true);
		return localEditorInfo;
	}
	public final EditorInfo getEditorInfo() {
		if (this.editorInfo == null) {
			this.editorInfo = getLocalEditorInfo();
		}

		this.editorInfo.imeOptions=EditorInfo.IME_ACTION_SEARCH;
		return this.editorInfo;
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {

		return super.onCreateInputConnection(getEditorInfo());
	}
}