package com.ihs.inputmethod.uimodules.widget;


import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

public final class CustomTagEditText extends EditText implements TextWatcher,View.OnFocusChangeListener{
    public static final char  DIVIDER=',';
	private static final int TAGS_COUNT_LIMIT = 50;
    private static final int SPANCOLOR=Color.parseColor("#19ff1e");
    private Stack<Integer> commaPostion=new Stack<>();
    private int lastDviderPosition =0;
    private char divider;
	private int lastPosition =0;
    private final String CODE=".*\"':;!?/_@#$%&-+()~`|•√π÷×¶∆£¢€¥^°={}\\©®™℅[]<>。，、；：？！„-«ˉˇ¨‘\\'“”〄～‖⁅＂＇｀｜〃【】々〆〇〈〉《》「．〒〓」『（）［］｛｝①②③④⑤⑥⑦⑧⑨⑩⑴⑵⑶⑷⑸⑹⑺⑻⑼⑽⑾⑿⒀⒁⒂⒃⒄⒅⒆⒇，、。．？！～＄％＠＆＃＊?；∶…¨，·˙?‘’“””〃‘′〃↑↓←→↖↗↙↘㊣◎○●⊕⊙○●△▲☆★◇◆□■▽▼§￥〒￠￡※♀♂αβγδεζηθικλμνξοπρστυφχψωⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫ≈≡≠=≤≥<>≮≯∷±+-×÷/∫∮∝∞∧∨∑∏∪∩∈∵∴⊥‖∠⌒⊙≌∽√°′〃$￡￥‰%℃¤￠M┌┍┎┏┐┑┒┓—┄┈├┝┞┟┠┡┢┣|┆┊┬┭┮┯┰┱┲┳┼┽┾┿╀╂╁╃§№☆★○●◎◇◆□■△▲※→←↑↓〓#&@\\^_⊙●○①⊕◎Θ⊙¤㊣▂▃▄▅▆▇██■▓回□〓≡╝╚╔╗╬═╓╩┠┨┯┷┏┓┗┛┳⊥『』┌♀◆◇◣◢◥▲▼△▽⊿▁▂▃▄▅▆▇█▉▊▋▌▍▎▏▓▔▕◢◣◤◥⊙♀♂ゃōゃ⊙▂⊙⊙0⊙⊙＾⊙⊙ω⊙⊙﹏⊙⊙△⊙⊙▽⊙?▂??0??＾??ω??﹏??△??▽?≥▂≤≥0≤≥＾≤≥ω≤≥﹏≤≥△≤≥▽≤∪▂∪∪0∪∪＾∪∪ω∪∪﹏∪∪△∪∪▽∪21●▂●●0●●＾●●ω●●﹏●●△●●▽●22∩▂∩∩0∩∩＾∩∩ω∩∩﹏∩∩△∩∩▽∩";
    private final ForegroundColorSpan colorSpan=new ForegroundColorSpan(SPANCOLOR);
    public CustomTagEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTagEditText(Context context) {
        super(context);
        init();
    }
    public CustomTagEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() { 
        commaPostion.push(lastDviderPosition);
        divider=DIVIDER;
        setCustomSelectionActionModeCallback(new ActionMode.Callback() {
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {

			}
		});
        setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

		// redefine the action
		setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				HSLog.d("actionId " + actionId);
				if (EditorInfo.IME_ACTION_NEXT == actionId) {
					afterTextChanged(getText().append(","));
					return true;
				}
				return false;
			}
		});

        setLongClickable(false);
        this.setOnFocusChangeListener(this);
	    addTextChangedListener(this);
    }
      
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//	    HSLog.d(">>>" + s.toString() + ">>start=" + start + ">>count=" + count + ">>after=" + after);
    }

    @Override
    public void afterTextChanged(Editable s) {
	    HSLog.d(s.toString());
	    String text=s.toString();
	    if(text.length()==lastPosition)
		    return;
	    if(text.length()== lastDviderPosition){
		    lastPosition =text.length();
		    return;
	    }

		// If we input a ',' to complete the tag, but tag only contains spaces, we remove this ',' and go on
		if (text.charAt(text.length() - 1) == DIVIDER) {
			final String tag = text.substring(lastDviderPosition, text.length() - 1);
			if (tag.trim().equals("")) {
				text = text.substring(0, text.length() - 1);
			}
		}


	    HSLog.d(text.length()+":"+ lastPosition +":"+ lastDviderPosition);
		if (text.length() >= lastPosition) {
			if(lastDviderPosition>0){
				commaPostion.clear();
				lastDviderPosition=0;
				commaPostion.push(lastDviderPosition);

			}
		    //text=handleMoreText(text).trim();
			text=handleMoreText(text);//.trim();
			int last=0;
			HSLog.d(text);
			while(last<text.length()){
				last=text.indexOf(divider,lastDviderPosition);
				if(last>=lastDviderPosition){
					lastDviderPosition=last+1;
					commaPostion.push(lastDviderPosition);
				}else {
					break;
				}
			}
			Spannable span=new SpannableStringBuilder(text);
			if(lastDviderPosition>0){
				lastPosition=text.length();
				span.setSpan(colorSpan, 0, lastDviderPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//				HSLog.d(span.toString());
				setText(span);
				setSelection(text.length());
				requestFocus();
			}else{
				lastPosition=text.length();
				setText(text);
				setSelection(text.length());
				requestFocus();
			}
//			HSLog.d(span.toString());
	    }else{
			handleDeleteText(text);
	    }
    }

	private String handleMoreText(String more) {
		if (TextUtils.isEmpty(more)) {
			return more;
		}

		StringBuilder sb=new StringBuilder();
		char temp=handleChar(more.charAt(0));
		if(temp!='\0')
			sb.append(temp);
		for(int i=1;i<more.length();i++){
			char op=handleChar(more.charAt(i));
			if(op==divider&&temp==divider){
				continue;
			}
			if(op!='\0'){
				sb.append(op);
				temp=op;
			}
		}
		if(sb.length()>0)
			if(sb.charAt(0)==divider){
				sb.deleteCharAt(0);
			}
		if(more.length()>1){
			char op=handleLastChar(more.charAt(more.length()-1));
			if(op==divider&&temp!=divider){
				sb.append(op);
			}
		}
		return sb.toString();
	}
	private char handleLastChar(char temp) {
//		if(temp==' '){
//			return divider;
//		}
//		return '\0';
		return temp;
	}
	private char handleChar(char temp) {
		if(CODE.indexOf(temp)>-1){
			return '\0';
		}
//		if(temp==' '){
//			return '\0';
//		}
		return temp;
	}

	private String [] handleTagsBeforeUpload(final String tags) {
		final String tagStr[] = tags.split("[,]");
		final HashSet<String> tagSet = new HashSet<>();

		// delete space and duplicate tags
		ArrayList<String> tagList = new ArrayList<>();
		for (String tag : tagStr) {
			tag = tag.trim().replaceAll("\\s+", " ");

			if (!tag.equals("")) {
				if (tagList.size() < TAGS_COUNT_LIMIT) {
					if (!tagSet.contains(tag)) {
						tagList.add(tag);
						tagSet.add(tag);
					}
				}
			}
		}

		for (String t : tagList) {
			HSLog.d("upload tag: " + "["+t+"]" );
		}

		tagList.toArray(tagStr);
		return tagStr;
	}

	@Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    private void handleDeleteText(String text){
        if(text.length()==0){
	        lastPosition =0;
	        return;
        }
        lastDviderPosition =commaPostion.pop();
        while(lastDviderPosition >text.length()){
            if(!commaPostion.isEmpty())
                lastDviderPosition =commaPostion.pop();
        }
        text=text.substring(0, lastDviderPosition);
        if(lastDviderPosition ==0){
            commaPostion.push(lastDviderPosition);
        }else if(commaPostion.size()==1){
            commaPostion.push(lastDviderPosition);
        }
        ForegroundColorSpan colorSpan=new ForegroundColorSpan(SPANCOLOR);
        Spannable span=new SpannableStringBuilder(text);
        span.setSpan(colorSpan, 0, lastDviderPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    lastPosition =text.length();
        setText(span);
        setSelection(lastDviderPosition);
    }
    public void setDivider(char divider){
        this.divider=divider;
    }
    public String[] getTags(){
        String tags="";
        if(getText()!=null){
            tags=getText().toString();
        }

        if(tags.length()>0) {
			return handleTagsBeforeUpload(tags);
			//return tags.split("[,]");
		}
        return null;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if(selEnd<getText().length()||selStart<getText().length()){
            setSelection(getText().length());
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!this.hasFocus()){
            String text=getEditableText().toString();
            if(text.length()>0){
	            if(text.charAt(text.length()-1)!=divider){

					// If no really tag input
					final String tag = text.substring(lastDviderPosition, text.length());
					if (tag.trim().equals("")) {
						return;
					}

		            text=text+",";
		            lastDviderPosition =text.length();
		            commaPostion.push(lastDviderPosition);
		            Spannable span = new SpannableStringBuilder(text);
		            span.setSpan(colorSpan, 0, lastDviderPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		            setText(span);
	            }
            }
        }
    }

}
