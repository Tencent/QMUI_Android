package com.qmuiteam.qmuidemo.fragment.components.qqface.emojicon;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;


public class EmojiconEditText extends EditText {
    private int mEmojiconSize;
    private int mEmojiconTextSize;
    private int mTextStart = 0;
    private int mTextLength = -1;
    private boolean mUseSystemDefault = false;

    public EmojiconEditText(Context context) {
        super(context);
        mEmojiconSize = (int) getTextSize();
        mEmojiconTextSize = (int) getTextSize();
    }

    public EmojiconEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmojiconEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mEmojiconTextSize = (int) getTextSize();
        mEmojiconSize = (int) getTextSize();
        setText(getText());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
//        if (!TextUtils.isEmpty(text)) {
//            SpannableStringBuilder builder = new SpannableStringBuilder(text);
//            EmojiconHandler.addEmojis(getContext(), builder, mEmojiconSize, mEmojiconTextSize, mTextStart, mTextLength, mUseSystemDefault);
//            text = builder;
//        }
        super.setText(text, type);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        Log.d("baggiotest", "onTextChanged : " + text + "; " + start + "; " + lengthBefore + "; " + lengthAfter);
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    /**
     * Set the size of emojicon in pixels.
     */
    public void setEmojiconSize(int pixels) {
        mEmojiconSize = pixels;
        super.setText(getText());
    }

    /**
     * Set whether to use system default emojicon
     */
    public void setUseSystemDefault(boolean useSystemDefault) {
        mUseSystemDefault = useSystemDefault;
    }
}
