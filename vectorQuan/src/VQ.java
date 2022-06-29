import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class VQ {

    static List<int[] []> allVectors = new ArrayList<>();
    List<int[] []> allAVGVectors = new ArrayList<>();

    public static void  Compress(int height, int width, int codeBlockSize, String Path){
        int[][] image = ImageRW.readImage(Path);
        splitToChunks(8,8,"F:\\m.jpg");
        List<int[] []>Quantized = new ArrayList<>();
        Quantize(codeBlockSize, allVectors, Quantized);
        for (int i=0;i<Quantized.size();i++){
        System.out.println(Arrays.deepToString(Quantized.get(i)));}
        Vector<Integer> VectorsToQuantizedIndices = Optimize(allVectors, Quantized);
    }

    //read original photo
    public int[][] getImageMatrix(String Path) {
        int[][] image = ImageRW.readImage(Path);
        return image;
    }

    //resize original image to fit into smaller matrixs
    public static int[][] changeImage(int height, int width, String Path) {
        //System.out.println("height here");
        int[][] image = ImageRW.readImage(Path);
        int originalHeight= ImageRW.height;
        int originalWidth= ImageRW.width;
        int resizedHeight , resizedWidth;
        if(originalHeight % height == 0) {
            resizedHeight = originalHeight ;
        }
        else {
            resizedHeight = ((originalHeight / height) + 1)*height;
        }
        //System.out.println("height "+resizedHeight);

        if(originalWidth % width == 0) {
            resizedWidth = originalWidth;
        }
        else {
            resizedWidth = ( (originalWidth  /  width) + 1) * width;
        }
        //System.out.println("width "+ resizedWidth);

        int[][] resizedImage= new int[resizedHeight][resizedWidth];

        for (int i = 0; i < resizedHeight; i++) {
            int x = i;
            if(x >= originalHeight) {
                x = originalHeight - 1;
            }
            else {
                x = i;
            }
            for (int j = 0; j < resizedWidth; j++) {
                int y = j;
                if(y >= originalWidth ) {
                    y = originalWidth- 1;
                }
                else {
                    y = j;
                }
                resizedImage[i][j] = image[x][y];
            }
        }


        return resizedImage;
    }


    //Divide resized photo to smaller arrays given the deminisions of the smaller arrays
    public static void splitToChunks(int h, int w, String Path){
        int pixels [][]=changeImage(h,w,Path);
        for (int row = 0; row < (pixels.length); row += h) {
            for (int column = 0; column < pixels[0].length; column += w) {
                int[][] temp = new int[h][w];
                for (int k = 0; k < h; k++) {
                    for (int l = 0; l < w; l++) {
                        temp[k][l] = pixels[row + k][column + l] ;
                        //System.out.print(temp[k][l] + " ");
                    }
                    //System.out.println(" ");
                }
                /*
                System.out.println("row "+ row);
                System.out.println();
                System.out.println();*/
                allVectors.add(temp);
            }
        }
    }


    //Get Average of the original photo arrays
    public static int[][] getAVG(List<int[] []> arrays){
        System.out.println("average");
        int h=arrays.get(0).length;
        int w=arrays.get(0)[0].length;
        int [][] avg = new int[h][w];
        for (int i = 0; i < arrays.size(); i++){
            for (int k = 0; k < h; k++) {
                for (int l = 0; l < w; l++) {
                    avg[k][l] += arrays.get(i)[k][l];
                }
            }
        }
        for (int k = 0; k < h; k++) {
            for (int l = 0; l < w; l++) {
                avg[k][l]=avg[k][l]/arrays.size();
            }
        }
        return avg;
    }

    private static void Quantize(int Level, List<int[] []> Vectors, List<int[] []> Quantized)
    {
        if(Level == 1 || Vectors.size() == 0)
        {
            if(Vectors.size() > 0)
                Quantized.add(getAVG(Vectors));
            return;
        }
        //Split
        List<int[] []> leftVectors = new ArrayList<>();
        List<int[] []> rightVectors=new ArrayList<>();
        int [] [] mean=getAVG(Vectors);

        for (int [][] vec : Vectors ) {
            int eDistance1 = EuclidDistance(vec, mean,  1);
            int eDistance2 = EuclidDistance(vec, mean, -1);
            if(eDistance1 >= eDistance2)
                leftVectors.add(vec);
            else
                rightVectors.add(vec);
        }

        Quantize(Level / 2, leftVectors, Quantized);
        Quantize(Level / 2, rightVectors, Quantized);
    }

    public static int EuclidDistance(int[][] x, int[][] y)
    {
        return EuclidDistance(x, y, 0);
    }

    public static int EuclidDistance(int [] [] x, int [] [] y, int Factor)
    {
        int distance = 0;
        int h=x.length;
        int w=x[0].length;
        for (int k = 0; k < h; k++) {
            for (int l = 0; l < w; l++) {
                distance += Math.pow(x[k][l] - y[k][l] + Factor, 2);
            }
        }

        return (int) Math.sqrt(distance);
    }


    private static Vector<Integer> Optimize(List<int [] []> Vectors, List<int [] []>Quantized)
    {
        Vector<Integer> VectorsToQuantizedIndices = new Vector<>();

        for (int[] [] vector : Vectors ) {
            int smallestDistance = EuclidDistance(vector, Quantized.get(0),0);
            int smallestIndex = 0;

            //Find the minimum Distance
            for (int i = 1; i < Quantized.size(); i++) {
                int tempDistance = EuclidDistance(vector, Quantized.get(i),0);
                if(tempDistance < smallestDistance)
                {
                    smallestDistance = tempDistance;
                    smallestIndex = i;
                }
            }

            //Map the i'th Vector to the [i] in Quantized
            VectorsToQuantizedIndices.add(smallestIndex);
        }
        return VectorsToQuantizedIndices;
    }

    public static void main(String[] args) {
        Compress(8, 8, 64, "F://m.jpg");

    }
}
