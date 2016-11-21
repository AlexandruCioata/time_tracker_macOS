import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class CompressJPEGFIle {

    public static void compress(BufferedImage image, String rootPath, String filename) throws IOException
    {
        //File imageFile = new File(inputFileName);
        File compressedImageFile = new File(rootPath + "/" + filename);

        //InputStream is = new FileInputStream(imageFile);

        OutputStream os = new FileOutputStream(compressedImageFile);

        float quality = 0.4f;

        // create a BufferedImage as the result of decoding the supplied InputStream
        //BufferedImage image = ImageIO.read(is);

        // get all image writers for JPG format
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");

        if (!writers.hasNext())
            throw new IllegalStateException("No writers found");

        ImageWriter writer = (ImageWriter) writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        // compress to a given quality
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        // appends a complete image stream containing a single image and
        //associated stream and image metadata and thumbnails to the output
        writer.write(null, new IIOImage(image, null, null), param);

        // close all streams
        os.close();
        ios.close();
        writer.dispose();

    }

    public static void main(String[] args) throws IOException {

        //compress("screenshot_1.jpg","myimage_compressed.jpg");
    }

}