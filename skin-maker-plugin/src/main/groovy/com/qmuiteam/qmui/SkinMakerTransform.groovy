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

package com.qmuiteam.qmui

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

class SkinMakerTransform extends Transform {

    private Project mProject
    private SkinMakerPlugin.SkinMaker mSkinMaker

    SkinMakerTransform(Project project, SkinMakerPlugin.SkinMaker skinMaker) {
        mProject = project
        mSkinMaker = skinMaker
    }


    @Override
    String getName() {
        return "skin-maker"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        File sourceFile = mSkinMaker.file
        if (sourceFile == null || !sourceFile.exists()) {
            return
        }
        mProject.logger.log(LogLevel.INFO, "skin code source: " + sourceFile.path)

        def injectCode = new InjectCode()
        injectCode.parseFile(sourceFile)

        def androidJar = mProject.android.bootClasspath[0].toString()

        def externalDepsJars = new ArrayList<File>()
        def externalDepsDirs = new ArrayList<File>()

        transformInvocation.referencedInputs.forEach { transformInput ->
            externalDepsJars += transformInput.jarInputs.map { it.file }
            externalDepsDirs += transformInput.directoryInputs.map { it.file }
        }

        transformInvocation.outputProvider.deleteAll()

        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { directoryInput ->

                def baseDir = directoryInput.file
                ClassPool pool = new ClassPool()
                pool.appendSystemPath()
                pool.appendClassPath(baseDir.absolutePath)
                pool.appendClassPath(androidJar)
                externalDepsJars.each { pool.insertClassPath(it.absolutePath) }
                externalDepsDirs.each { pool.insertClassPath(it.absolutePath) }


                directoryInput.file.eachFileRecurse { file ->
                    String filePath = file.absolutePath
                    if (filePath.endsWith(".class")) {
                        def className = filePath.substring(directoryInput.file.absolutePath.length() + 1, filePath.length() - 6)
                                .replace('/', '.')
                        def codes = injectCode.getCode(className)
                        if (codes != null && !codes.isEmpty()) {
                            CtClass ctClass = pool.getCtClass(className)
                            if (ctClass.isFrozen()) {
                                ctClass.defrost()
                            }
                            codes.keySet().each { prefix ->
                                def sb = new StringBuilder()
                                sb.append("public void skinMaker")
                                sb.append(prefix)
                                sb.append("(){")
                                codes.get(prefix).each { text ->
                                    sb.append(text)
                                    sb.append(";")
                                }
                                sb.append("}")
                                CtMethod newMethod = CtMethod.make(sb.toString(), ctClass)
                                ctClass.addMethod(newMethod)
                            }

                            if (className.endsWith("Fragment")) {
                                CtMethod ctMethod = ctClass.getDeclaredMethod("onViewCreated", pool.get("android.view.View"))
                                ctMethod.insertAfter("skinMaker" + className.split("\\.").last() + "();")
                            } else if (className.endsWith("Activity")) {
                                CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate", pool.get("android.os.Bundle"))
                                ctMethod.insertAfter("skinMaker" + className.split("\\.").last() + "();")
                            }

                            ctClass.writeFile(baseDir.absolutePath)
                            ctClass.detach()
                        }
                    }
                }

                def dest = transformInvocation.outputProvider.getContentLocation(
                        directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes,
                        Format.DIRECTORY)

                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            //遍历jar文件 对jar不操作，但是要输出到out路径
            input.jarInputs.each { jarInput ->
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(
                        jarName + md5Name,
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }


    class InjectCode {
        private HashMap<String, HashMap<String, ArrayList<String>>> mCodeMap = new HashMap<>()

        private String mCurrentClassName = null
        private HashMap<String, ArrayList<String>> mCurrentCodes = null

        void parseFile(File file) {
            file.newReader().lines().each { text ->
                if (text != null) {
                    text = text.trim()
                    if (!text.isBlank()) {
                        if (mCurrentClassName == null) {
                            mCurrentClassName = text
                            mCurrentCodes = new HashMap<String, ArrayList<String>>()
                            mCodeMap.put(mCurrentClassName, mCurrentCodes)
                        } else if (text != ";") {
                            int split = text.indexOf(",")
                            if (split > 0 && split < text.length()) {
                                String key = text.substring(0, split)
                                ArrayList<String> codes = mCurrentCodes.get(key)
                                if (codes == null) {
                                    codes = new ArrayList<String>()
                                    mCurrentCodes.put(key, codes)
                                }
                                codes.add(text.substring(split + 1, text.length()))
                            }
                        } else {
                            mCurrentClassName = null
                            mCurrentCodes = null
                        }
                    }
                }
            }
        }

        HashMap<String, ArrayList<String>> getCode(String className) {
            return mCodeMap.get(className)
        }
    }
}