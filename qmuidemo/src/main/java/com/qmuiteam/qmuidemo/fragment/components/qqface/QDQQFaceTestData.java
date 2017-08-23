package com.qmuiteam.qmuidemo.fragment.components.qqface;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.span.QMUITouchableSpan;

import java.util.ArrayList;

/**
 * @author cginechen
 * @date 2016-12-22
 */

public class QDQQFaceTestData {
    private ArrayList<CharSequence> mList = new ArrayList<>();

    public QDQQFaceTestData() {
        for (int i = 0; i < 100; i++) {
            String topic = "#表情[发呆][微笑]大战";
            String at = "@伟大的[发呆]工程师";
            String text = "index = " + i + " : " + at + "，人生就是要不断[微笑]\n[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                    "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                    "[得意][微笑][撇嘴][色][微笑][得意][流泪][害羞][闭嘴][睡][微笑][微笑][微笑]" +
                    "[微笑][微笑][惊讶][微笑][微笑][微笑][微笑][发怒][微笑]\n[微笑][微笑][微笑][微笑]" +
                    "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][调皮][微笑][微笑][微笑][微笑]" +
                    "[微笑][微笑][微笑][呲牙][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                    "人生就是要不断[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                    "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]\n\n[微笑][微笑][微笑]" +
                    "[微笑][微笑]也会出现不合格的[这是不合格的标签]其它表情[发呆][发呆][发呆][发呆][发呆] " +
                    "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][发呆][发呆][发呆][发呆] " +
                    "[微笑][微笑][微笑][微笑][微笑][微笑][微笑]" + topic + "[微笑][微笑][微笑][微笑][微笑][微笑]" +
                    "[微笑][微笑][微笑][微笑][微笑][微笑]\n[微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                    "[微笑][微笑][微笑][微笑][微笑][发呆][发呆][发呆][发呆][发呆][微笑][微笑][微笑]";
            SpannableString sb = new SpannableString(text);
            sb.setSpan(new QMUITouchableSpan(Color.BLUE, Color.BLACK, Color.GRAY, Color.GREEN) {
                @Override
                public void onSpanClick(View widget) {
                    Toast.makeText(widget.getContext(), "点击了@", Toast.LENGTH_SHORT).show();
                }
            }, text.indexOf(at), text.indexOf(at) + at.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            sb.setSpan(new QMUITouchableSpan(Color.RED, Color.BLACK, Color.YELLOW, Color.GREEN) {
                @Override
                public void onSpanClick(View widget) {
                    Toast.makeText(widget.getContext(), "点击了话题", Toast.LENGTH_SHORT).show();
                }
            }, text.indexOf(topic), text.indexOf(topic) + topic.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            mList.add(sb);
        }

    }

    public ArrayList<CharSequence> getList() {
        return mList;
    }
}
