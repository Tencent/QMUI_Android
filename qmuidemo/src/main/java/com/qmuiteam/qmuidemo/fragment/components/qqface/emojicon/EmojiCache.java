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

package com.qmuiteam.qmuidemo.fragment.components.qqface.emojicon;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.collection.LruCache;

public class EmojiCache {
	//caceh里面默认只存放32个表情
	private static final int EMOJI_CACHE_SIZE = 32;
	private static EmojiCache _instance;
	public static void createInstance(int cacheSize) {
		if(_instance == null) {
			_instance = new EmojiCache(cacheSize);
		}
	}
	public static EmojiCache getInstance() {
		if (_instance == null) {
			createInstance(EMOJI_CACHE_SIZE);
		}

		return _instance;
	}
	
	private LruCache<Integer, Drawable> mCache;
	public EmojiCache(int cacheSize) {
		mCache = new LruCache<Integer, Drawable>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Drawable value) {
                return 1;
            }
            
            @Override
            protected void entryRemoved(boolean evicted, Integer key, Drawable oldValue, Drawable newValue) {
            	//这种情况，可能该drawable还在页面使用中，不能随便recycle。这里解除引用即可，gc会自动清除
//            	if (oldValue instanceof BitmapDrawable) {
//					((BitmapDrawable)oldValue).getBitmap().recycle();
//				}
            }
        };
	}
	
	public Drawable getDrawable(Context context, int resourceId) {
		Drawable drawable = mCache.get(resourceId);
		if (drawable == null) {
			drawable = ContextCompat.getDrawable(context, resourceId);
			mCache.put(resourceId, drawable);
		}
		
		return drawable;
	}
}
