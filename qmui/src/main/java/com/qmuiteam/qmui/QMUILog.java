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

package com.qmuiteam.qmui;

/**
 *
 * @author cginechen
 * @date 2016-08-11
 */
public class QMUILog {
    public interface QMUILogDelegate {
        void e(final String tag, final String msg, final Object ... obj);
        void w(final String tag, final String msg, final Object ... obj);
        void i(final String tag, final String msg, final Object ... obj);
        void d(final String tag, final String msg, final Object ... obj);
        void printErrStackTrace(String tag, Throwable tr, final String format, final Object ... obj);
    }

    private static QMUILogDelegate sDelegete = null;

    public static void setDelegete(QMUILogDelegate delegete) {
        sDelegete = delegete;
    }

    public static void e(final String tag, final String msg, final Object ... obj) {
        if (sDelegete != null) {
            sDelegete.e(tag, msg, obj);
        }
    }

    public static void w(final String tag, final String msg, final Object ... obj) {
        if (sDelegete != null) {
            sDelegete.w(tag, msg, obj);
        }
    }

    public static void i(final String tag, final String msg, final Object ... obj) {
        if (sDelegete != null) {
            sDelegete.i(tag, msg, obj);
        }
    }

    public static void d(final String tag, final String msg, final Object ... obj) {
        if (sDelegete != null) {
            sDelegete.d(tag, msg, obj);
        }
    }

    public static void printErrStackTrace(String tag, Throwable tr, final String format, final Object ... obj) {
        if (sDelegete != null) {
            sDelegete.printErrStackTrace(tag, tr, format, obj);
        }
    }
}
