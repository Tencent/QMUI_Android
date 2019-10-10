/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmui.skin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.tencent.mmkv.MMKV;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class QMUISkinMaker {
    private static final String MMKV_ID = "qmui_skin_maker";
    private static QMUISkinMaker sSkinMaker;
    private int sNextId = 1;
    private SparseArray<HashMap<View, ViewInfo>> mBindInfo = new SparseArray<>();
    private String[] mPackageNames;
    private List<String> mAttrsInR;

    public static boolean isInited() {
        return sSkinMaker != null;
    }

    public static QMUISkinMaker init(Context context, String[] packageNames, Class<?> attrsClassInR) {
        if (sSkinMaker == null) {
            sSkinMaker = new QMUISkinMaker();
        }
        MMKV.initialize(context);
        sSkinMaker.mPackageNames = packageNames;


        Field[] fields = attrsClassInR.getFields();

        sSkinMaker.mAttrsInR = new ArrayList<>();
        for (Field field : fields) {
            String name = field.getName();
            if(name.startsWith("qmui") || name.startsWith("color") || name.startsWith("app") || name.contains("skin")){
                String lowerCase = name.toLowerCase();
                if(lowerCase.endsWith("width")
                        || lowerCase.endsWith("height")
                        || lowerCase.endsWith("size")
                        || lowerCase.endsWith("style")
                        || lowerCase.endsWith("theme")){
                    continue;
                }
                sSkinMaker.mAttrsInR.add(name);
            }

        }
        return sSkinMaker;
    }

    public static QMUISkinMaker getInstance() {
        if (sSkinMaker == null) {
            throw new RuntimeException("must invoke init() to init sSkinMaker");
        }
        return sSkinMaker;
    }

    private QMUISkinMaker() {

    }

    /**
     * must be called after {@link Activity#setContentView(View)}
     *
     * @param activity
     * @return
     */
    public int bind(Activity activity) {
        int ret = bind(QMUIViewHelper.getActivityRoot(activity));
        HashMap<View, ViewInfo> viewInfoMap = mBindInfo.get(ret);
        recursiveReflect(activity, activity.getClass().getSimpleName(), viewInfoMap, new ArrayList<>(100));
        restoreSkinFromMMKV(viewInfoMap);
        return ret;
    }

    /**
     * must be called in or after {@link Fragment#onViewCreated(View, Bundle)}}
     *
     * @param fragment
     * @return
     */
    public int bind(Fragment fragment) {
        int ret = bind(fragment.getView());
        HashMap<View, ViewInfo> viewInfoMap = mBindInfo.get(ret);
        recursiveReflect(fragment, fragment.getClass().getSimpleName(), viewInfoMap, new ArrayList<>(100));
        restoreSkinFromMMKV(viewInfoMap);
        return ret;
    }


    private int bind(View view) {
        int id = sNextId++;
        HashMap<View, ViewInfo> viewInfoMap = new HashMap<>();
        mBindInfo.put(id, viewInfoMap);
        innerBind(view, viewInfoMap);
        return id;
    }

    private void innerBind(View view, HashMap<View, ViewInfo> viewInfoMap) {
        if (view instanceof QMUITopBar) {
            return;
        }
        viewInfoMap.put(view, generateViewInfo(view));
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                innerBind(viewGroup.getChildAt(i), viewInfoMap);
            }
        }
    }

    public void unbind(int id) {
        HashMap<View, ViewInfo> viewInfoMap = mBindInfo.get(id);
        if (viewInfoMap != null) {
            for (View view : viewInfoMap.keySet()) {
                ViewInfo viewInfo = viewInfoMap.get(view);
                if (viewInfo != null) {
                    view.setOnClickListener(viewInfo.originClickListener);
                }
            }
        }
    }

    private ViewInfo generateViewInfo(final View view) {
        final ViewInfo viewInfo = new ViewInfo();
        viewInfo.view = view;
        try {
            Field listenerInfoFiled = View.class.getDeclaredField("mListenerInfo");
            listenerInfoFiled.setAccessible(true);
            Object listenerInfo = listenerInfoFiled.get(view);
            Field onClickListenerField = listenerInfo.getClass().getDeclaredField("mOnClickListener");
            onClickListenerField.setAccessible(true);
            viewInfo.originClickListener = (View.OnClickListener) onClickListenerField.get(listenerInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        View.OnClickListener skinOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                QMUIGroupListView groupListView = new QMUIGroupListView(context);
                groupListView.setId(QMUIViewHelper.generateViewId());
                QMUIGroupListView.newSection(context)
                        .addItemView(groupListView.createItemView("Background"), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chooseAttr(view, new ValueWriter() {
                                    @Override
                                    public void write(String attrName) {
                                        viewInfo.valueBuilder.background(attrName);
                                        QMUISkinHelper.setSkinValue(view, viewInfo.valueBuilder);
                                        viewInfo.saveToMMKV();
                                        QMUISkinManager.getInstance(context).refreshTheme(view);
                                    }
                                });
                            }
                        })
                        .setUseTitleViewForSectionSpace(false)
                        .setShowSeparator(false)
                        .addTo(groupListView);
                QMUIPopups.popup(view.getContext(), QMUIDisplayHelper.dp2px(context, 200))
                        .arrow(true)
                        .shadow(true)
                        .view(groupListView)
                        .dismissIfOutsideTouch(false)
                        .show(v);
            }
        };
        viewInfo.skinClickListener = skinOnClickListener;
        view.setOnClickListener(skinOnClickListener);
        return viewInfo;
    }

    private void chooseAttr(View anchorView, ValueWriter valueWriter) {
        SkinAttrChooseMakerPopup popup = new SkinAttrChooseMakerPopup(anchorView.getContext(), mAttrsInR, valueWriter);
        popup.show(anchorView);
    }

    private void recursiveReflect(Object object, String prefix, HashMap<View, ViewInfo> viewInfoMap, List<Object> scanned) {
        if (object == null) {
            return;
        }

        String className = object.getClass().getName();
        if (!isBusinessClass(className)) {
            return;
        }
        Field[] fields = object.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(object);
                if (value != null) {
                    // avoid circular reference
                    if (scanned.contains(value)) {
                        continue;
                    }
                    scanned.add(value);
                    if (value instanceof View) {
                        if (viewInfoMap.containsKey(value)) {
                            ViewInfo viewInfo = viewInfoMap.get(value);
                            if (viewInfo != null) {
                                viewInfo.belongClassName = className;
                                viewInfo.fieldName = field.getName();
                                viewInfo.prefixName = prefix;
                            }
                        }
                    }
                    recursiveReflect(value, prefix + "_" + field.getName(), viewInfoMap, scanned);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void restoreSkinFromMMKV(HashMap<View, ViewInfo> viewInfoMap){
        for (View view : viewInfoMap.keySet()) {
            ViewInfo viewInfo = viewInfoMap.get(view);
            String mmkvKey = viewInfo.getKey();
            if (mmkvKey == null) {
                continue;
            }
            String result = MMKV.mmkvWithID(MMKV_ID).decodeString(mmkvKey);
            if (result == null || result.isEmpty()) {
                continue;
            }
            String[] splits = result.split("-");
            if (viewInfo.belongClassName.equals(splits[0])) {
                QMUISkinHelper.setSkinValue(view, splits[1]);
                viewInfo.valueBuilder.convertFrom(splits[1]);
            }
            QMUISkinManager.getInstance(view.getContext()).refreshTheme(view);
        }
    }

    private boolean isBusinessClass(String className) {
        for (String packageName : mPackageNames) {
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    static class ViewInfo {
        public View.OnClickListener originClickListener;
        public View.OnClickListener skinClickListener;
        public View view;
        public QMUISkinValueBuilder valueBuilder = new QMUISkinValueBuilder();
        public String fieldName;
        public String belongClassName;
        public String prefixName;


        @Nullable
        public String getKey() {
            if (fieldName == null) {
                return null;
            }
            return prefixName + "_" + fieldName;
        }

        public void saveToMMKV() {
            if (fieldName == null) {
                return;
            }
            MMKV.mmkvWithID(MMKV_ID).encode(getKey(), belongClassName + "-" + valueBuilder.build());
        }
    }

    public void export(){
        MMKV kv = MMKV.mmkvWithID(MMKV_ID);
        Map<String, String> map = (Map<String, String>) kv.getAll();
    }

    interface ValueWriter {
        void write(String attrNaME);
    }
}
