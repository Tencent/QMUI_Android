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
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * 检测图片的尺寸的正确性，例如2倍图的宽高应该为偶数，3倍图的宽高应该为3的倍数
 * Created by Kayo on 2017/9/7.
 */

public class QMUIImageScaleDetector extends Detector implements Detector.BinaryResourceScanner {

    public static final Issue ISSUE_IMAGE_SCALE =
            Issue.create("QMUIImageSizeDisproportionate",
                    "The size of this image is disproportionate.",
                    "Please check the size of the image, for example, the height and width of the 3x plot should be 1.5 times 2x plot.",
                    Category.ICONS, 4, Severity.WARNING,
                    new Implementation(QMUIImageScaleDetector.class, Scope.BINARY_RESOURCE_FILE_SCOPE));

    private static final String IGNORE_IMAGE_NIGHT_PNG = ".9.png";
    private static final String CHECK_IMAGE_WEBP = ".webp";
    private static final String CHECK_IMAGE_PNG = ".png";
    private static final String CHECK_IMAGE_JPEG = ".jpeg";
    private static final String CHECK_IMAGE_JPG = ".jpg";

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
            String pattern = ".*?[mipmap|drawable]\\-xhdpi.*?";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(filePath);
            if (m.find()) {
                String threePlotFilePath = filePath.replace("xhdpi", "xxhdpi");
                File threePlotFile = new File(threePlotFilePath);
                try {
                    BufferedImage targetImage = ImageIO.read(context.file);
                    int targetWidth = targetImage.getWidth();
                    int targetHeight = targetImage.getHeight();

                    BufferedImage threePlotImage = ImageIO.read(threePlotFile);
                    int threePlotWidth = threePlotImage.getWidth();
                    int threePlotHeight = threePlotImage.getHeight();
                    if ((double) threePlotWidth / targetWidth != 1.5 || (double) threePlotHeight / targetHeight != 1.5) {
                        Location fileLocation = Location.create(context.file);
                        context.report(ISSUE_IMAGE_SCALE, fileLocation, "2倍图 " + filePath +
                                " 与其3倍图宽高分别为 (" + targetWidth + ", " + targetHeight + ") 和 (" + threePlotWidth + ", " + threePlotHeight + ")，不符合比例关系。");
                    }
                } catch (Exception ignored) {
                }
            }
        }

    }
}
