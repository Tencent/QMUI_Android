package com.qmuiteam.qmui.lint;

import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.ResourceContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * 检测图片的尺寸的正确性，例如2倍图的宽高应该为偶数，3倍图的宽高应该为3的倍数
 * Created by Kayo on 2017/8/30.
 */

public class QMUIImageSizeDetector extends Detector implements Detector.BinaryResourceScanner {

    public static final Issue ISSUE_IMAGE_SIZE =
            Issue.create("QMUIImageSizeInvalid",
                    "The size of this image is not correct.",
                    "Please check the size of the image, for example, the height and width of the 2x plot should be even.",
                    Category.ICONS, 2, Severity.WARNING,
                    new Implementation(QMUIImageSizeDetector.class, Scope.BINARY_RESOURCE_FILE_SCOPE));

    private static final String IGNORE_IMAGE_NIGHT_PNG = ".9.png";
    private static final String CHECK_IMAGE_WEBP = ".webp";
    private static final String CHECK_IMAGE_PNG = ".png";
    private static final String CHECK_IMAGE_JPEG = ".jpeg";
    private static final String CHECK_IMAGE_JPG = ".jpg";

    /**
     * 去掉数值多余的0与小数点符号
     */
    public static String trimZeroAndDot(double number) {
        String value = String.valueOf(number);
        if (value.indexOf(".") > 0) {
            value = value.replaceAll("0+?$", ""); // 去掉多余的0
            value = value.replaceAll("[.]$", ""); // 若此时最后一位是小数点符号，则去掉该符号
        }
        return value;
    }

    @Override
    public boolean appliesTo(ResourceFolderType var1) {
        return var1.getName().equalsIgnoreCase(String.valueOf(ResourceFolderType.MIPMAP)) || var1.getName().equalsIgnoreCase(String.valueOf(ResourceFolderType.DRAWABLE));
    }

    @Override
    public void checkBinaryResource(ResourceContext context) {

        String filename = context.file.getName();

        if (filename.contains(IGNORE_IMAGE_NIGHT_PNG)) {
            return;
        }

        if (filename.contains(CHECK_IMAGE_WEBP) || filename.contains(CHECK_IMAGE_PNG) || filename.contains(CHECK_IMAGE_JPEG) || filename.contains(CHECK_IMAGE_JPG)) {
            String filePath = context.file.getPath();
            String pattern = ".*?[mipmap|drawable]\\-(x*)hdpi.*?";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(filePath);
            if (m.find()) {
                double multiple = 1.5;
                if (m.group(1).length() > 0) {
                    multiple = m.group(1).length() + 1;
                }
                try {
                    BufferedImage targetImage = ImageIO.read(context.file);
                    int width = targetImage.getWidth();
                    int height = targetImage.getHeight();
                    if (width % multiple != 0 || height % multiple != 0) {
                        Location fileLocation = Location.create(context.file);
                        context.report(ISSUE_IMAGE_SIZE, fileLocation, filePath + " 为" + trimZeroAndDot(multiple) + "倍图，其宽高应该是" + trimZeroAndDot(multiple) + "的倍数，目前宽高为 (" + width + ", " + height + ")。");
                    }
                } catch (Exception ignored) {
                }
            }
        }

    }
}
