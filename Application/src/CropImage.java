import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by mihai on 7/14/16.
 */
public class CropImage {

    private final static Logger logger = Logger.getLogger(CropImage.class);

    public static void cropImage(BufferedImage src, Rectangle rect,
                                 String outputPath, String cropped_image_name) throws Exception
    {
        try{
            BufferedImage dest = src.getSubimage(0, 0, rect.width, rect.height);

            File outputfile = new File(outputPath + "/" + cropped_image_name);
            ImageIO.write(dest, "jpg", outputfile);

        }
        catch(Exception e)
        {
            logger.error("Exception in CropImage->cropImage -> ",e);
        }

    }

    public static void cropImagesFrom(String folderName,String output) throws Exception
    {
        java.util.List<String> imagesToCrop = listAllFilesFromDirectory(folderName);


        for(String imageSource:imagesToCrop)
        {
            System.out.println("image: " + imageSource);

            String[] tokens = imageSource.split("\\.(?=[^\\.]+$)");

            try{
                BufferedImage bufferedImage = ImageIO.read(
                        Files.newInputStream(Paths.get(folderName + "/" + imageSource)));

                cropImage(bufferedImage,
                        new Rectangle(0,0,bufferedImage.getWidth(),130),
                        output,tokens[0] + "_cropped.jpg");
            }
            catch(Exception e)
            {
                System.out.println("Exception: " + e);
            }

            System.out.println("image: " + imageSource);
        }


    }

    private static java.util.List<String> listAllFilesFromDirectory(String path)
    {
        java.util.List<String> fileNameList = new ArrayList<String>();

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                //System.out.println("File " + listOfFiles[i].getName());
                fileNameList.add(listOfFiles[i].getName());
            }
        }

        return fileNameList;
    }


    public static void main(String[] args) throws Exception{


        cropImagesFrom("test_folder_cropped_images_low_resolution","cropped_images");



/*        BufferedImage image = new Robot().createScreenCapture(
                new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

        cropImage(image,new Rectangle(0,0,image.getWidth(),130),".","saved.jpg");

       *//* BufferedImage output = cropImage(image,new Rectangle(0,0,image.getWidth(),130));

        File outputfile = new File("saved.jpg");
        ImageIO.write(output, "jpg", outputfile);*/

    }

}
